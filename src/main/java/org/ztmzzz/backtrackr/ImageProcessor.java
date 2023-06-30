package org.ztmzzz.backtrackr;

import java.awt.image.BufferedImage;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class ImageProcessor {
    public static int splitNumber = 4;


    public static void main(String[] args) {
        OpenCV.loadLocally();
        Mat img1 = Imgcodecs.imread("screenshot/2023/06/22/21/09-16.jpg");
        Mat img2 = Imgcodecs.imread("screenshot/2023/06/22/21/10-45.jpg");
        System.out.println(compareImage(img1, img2));
//        Rect rect = getDifferenceBoundingRect(img1, img2);
//        Mat subImg1 = img1.submat(rect);
//        Mat subImg2 = img2.submat(rect);
//        Imgcodecs.imwrite("screenshot/1.jpg", subImg1);
//        Imgcodecs.imwrite("screenshot/2.jpg", subImg2);
    }

    public static double compareImage(Mat img1, Mat img2) {
        Imgproc.cvtColor(img1, img1, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(img2, img2, Imgproc.COLOR_BGR2GRAY);
        Mat hist1 = new Mat();
        Mat hist2 = new Mat();
        Imgproc.calcHist(List.of(img1), new MatOfInt(0), new Mat(), hist1, new MatOfInt(256), new MatOfFloat(0, 256));
        Imgproc.calcHist(List.of(img2), new MatOfInt(0), new Mat(), hist2, new MatOfInt(256), new MatOfFloat(0, 256));

        // Normalize the histograms
        Core.normalize(hist1, hist1, 0, hist1.rows(), Core.NORM_MINMAX, -1, new Mat());
        Core.normalize(hist2, hist2, 0, hist2.rows(), Core.NORM_MINMAX, -1, new Mat());

        // Compare the histograms
        return Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CORREL);
    }

    public static String matToBase64(Mat mat) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        return Base64.getEncoder().encodeToString(byteArray);
    }

    public static Mat simpleDiff(Mat mat1, Mat mat2) {
        Mat diff = new Mat();
        Mat samePixels = new Mat();
        Core.compare(mat1, mat2, samePixels, Core.CMP_EQ);
        Core.subtract(mat1, samePixels, diff);
        return diff;
    }

    public static Rect getDifferenceBoundingRect(Mat img1, Mat img2) {
        Imgproc.cvtColor(img1, img1, Imgproc.COLOR_BGR2GRAY);  // 转换为灰度图像
        Imgproc.cvtColor(img2, img2, Imgproc.COLOR_BGR2GRAY);  // 转换为灰度图像
        double threshold = 40; //range 0-255
        Rect ignoreRect = new Rect(0, 1440 - 100, 2560, 100);

        Mat diff = new Mat();
//        Core.absdiff(img1, img2, diff);
//        Core.subtract(img1, img2, diff);


        Mat samePixels = new Mat();
        Core.compare(img1, img2, samePixels, Core.CMP_EQ);


        Core.subtract(img1, samePixels, diff);

        System.out.println(matToBase64(diff));
        Imgcodecs.imwrite("screenshot/diff.jpg", diff);
        //Imgproc.cvtColor(diff, diff, Imgproc.COLOR_BGR2GRAY);  // 转换为灰度图像
        Imgcodecs.imwrite("screenshot/diff2.jpg", diff);
        Mat mask = Mat.ones(diff.size(), CvType.CV_8U);
        mask.setTo(new Scalar(255));
        Imgproc.rectangle(mask, ignoreRect, new Scalar(0), -1);

        // 使用掩码
        Core.bitwise_and(diff, mask, diff);
        Imgcodecs.imwrite("screenshot/diff3.jpg", diff);
        Core.compare(diff, new Scalar(threshold), diff, Core.CMP_GT);
        Imgcodecs.imwrite("screenshot/diff4.jpg", diff);

        // Assuming 'diff' is your difference image
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(diff, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        int i = 0;
        List<Rect> boundingRects = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            if (rect.height < 10 || rect.width < 10) {
                continue;
            }
            Imgcodecs.imwrite("screenshot/rr" + i++ + ".jpg", img1.submat(rect));
            boundingRects.add(rect);
        }

// Now 'boundingRects' contains a bounding rectangle for each separate region in 'diff'


        MatOfPoint points = new MatOfPoint();
        if (Core.countNonZero(diff) == 0) {
            System.out.println("No differences found.");
            return null;
        }

        Core.findNonZero(diff, points);
        return Imgproc.boundingRect(points);
    }

    public static String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(byteArray);
    }

    public static double[] compareImage(Mat[] img1, Mat[] img2) {
        double[] result = new double[splitNumber * splitNumber];
        for (int i = 0; i < splitNumber * splitNumber; i++) {
            result[i] = compareImage(img1[i], img2[i]);
        }
        return result;
    }

    public static double compareImage(BufferedImage img1, BufferedImage img2) {
        Mat mat1 = bufferedImageToMat(img1);
        Mat mat2 = bufferedImageToMat(img2);
        return compareImage(mat1, mat2);
    }

    public static Mat[] split(BufferedImage image) {
        Mat[] result = new Mat[splitNumber * splitNumber];
        Mat src = bufferedImageToMat(image);
        int width = src.cols();
        int height = src.rows();
        int splitWidth = width / splitNumber;
        int splitHeight = height / splitNumber;
        for (int i = 0; i < splitNumber; i++) {
            for (int j = 0; j < splitNumber; j++) {
                Rect rect = new Rect(i * splitWidth, j * splitHeight, splitWidth, splitHeight);
                Mat segment = new Mat(src, rect);
                result[i * splitNumber + j] = segment;
            }
        }
        return result;
    }

    public static Mat bufferedImageToMat(BufferedImage image) {
        image = convertTo3ByteBGRType(image);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }

    private static BufferedImage convertTo3ByteBGRType(BufferedImage image) {
        BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        convertedImage.getGraphics().drawImage(image, 0, 0, null);
        return convertedImage;
    }

    public static BufferedImage matToBufferedImage(Mat mat) {
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] bytes = new byte[bufferSize];
        mat.get(0, 0, bytes);
        BufferedImage img = new BufferedImage(mat.cols(), mat.rows(), BufferedImage.TYPE_3BYTE_BGR);
        final byte[] targetPixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        System.arraycopy(bytes, 0, targetPixels, 0, bytes.length);
        return img;
    }


}
