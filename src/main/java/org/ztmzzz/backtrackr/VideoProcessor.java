package org.ztmzzz.backtrackr;

import org.aspectj.apache.bcel.util.ClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.ztmzzz.backtrackr.ScreenshotProcessor.screenshotPath;

@Component
public class VideoProcessor {
    private static final String ffmpegPath = "./ffmpeg.exe";
    private static final String outputPath = "./screenshot/video/";
    private static final String tempFilePath = outputPath + "temp.txt";
    private static final Logger logger = LoggerFactory.getLogger(VideoProcessor.class);

    private void generateImageListFile(String year, String month, String day) throws IOException {
        File tempFile = new File(tempFilePath);
        File dir = new File(tempFile.getParent());
        dir.mkdirs();
        PrintWriter writer = new PrintWriter(tempFile);

        Path dayPath = Paths.get(screenshotPath, year, month, day);
        try (Stream<Path> paths = Files.walk(dayPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".jpg"))
                    .sorted()
                    .forEach(path -> writer.println("file '" + path.toAbsolutePath() + "'"));
        }
        writer.close();
    }

    private void removeTempFile() {
        File tempFile = new File(tempFilePath);
        tempFile.delete();
    }

    public boolean generateVideo(String year, String month, String day) {
        try {
            generateImageListFile(year, month, day);
            List<String> command = new ArrayList<>();
            command.add(ffmpegPath);
            command.add("-y");
            command.add("-r");
            command.add("1");
            command.add("-loglevel");
            command.add("quiet");
            command.add("-f");
            command.add("concat");
            command.add("-safe");
            command.add("0");
            command.add("-i");
            command.add(tempFilePath);
            command.add("-c:v");
            command.add("libx264");
            command.add("-crf");
            command.add("20");
            command.add(outputPath + year + "-" + month + "-" + day + ".mp4");

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
//            InputStream inputStream = process.getInputStream();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//            String line ;
//            StringBuilder s = new StringBuilder();
//            while ((line = reader.readLine()) != null) {
//                s.append(line);
//            }
//            logger.info(s.toString());
            int exitCode = process.waitFor();
            logger.info("Exit code: " + exitCode);
            removeTempFile();
            return exitCode == 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
