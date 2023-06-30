package org.ztmzzz.backtrackr;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.ztmzzz.backtrackr.entity.ScreenshotEntity;
import org.ztmzzz.backtrackr.service.ScreenshotService;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.ztmzzz.backtrackr.ImageProcessor.*;
import static org.ztmzzz.backtrackr.ScreenshotProcessor.timestampToFullPath;

@Component
public class ScheduledTask {

    private final ScreenshotProcessor screenshotProcessor;
    private final OCRProcessor ocrProcessor;
    private final ScreenshotService screenshotService;

    @Autowired
    public ScheduledTask(ScreenshotProcessor screenshotProcessor, OCRProcessor ocrProcessor, ScreenshotService screenshotService) {
        this.screenshotProcessor = screenshotProcessor;
        this.ocrProcessor = ocrProcessor;
        this.screenshotService = screenshotService;
    }

    @Scheduled(fixedRate = 2000)
    public void screenshotTask() throws IOException, AWTException {
        ScreenshotProcessor.ScreenshotInfo info = screenshotProcessor.screenshot();
        ScreenshotEntity screenshotEntity = new ScreenshotEntity();
        screenshotEntity.setTime(info.timestamp);
        screenshotEntity.setProcess(info.processName);
        screenshotEntity.setTitle(info.frontWindowTitle);
        screenshotService.save(screenshotEntity);
    }


    private void fullOCR(ScreenshotEntity now) throws IOException {
        String path = timestampToFullPath(now.getTime());
        Mat mat = Imgcodecs.imread(path);
        String text = ocrProcessor.ocr(matToBase64(mat));
        now.setText(text);
        now.setAppend(false);
        screenshotService.save(now);
    }

    @Scheduled(fixedRate = 4000)
    //@Async("taskExecutor")
    public void ocrTask() throws IOException {
        ArrayList<ScreenshotEntity> needOCR = (ArrayList<ScreenshotEntity>) screenshotService.findByTextIsNull();
        for (ScreenshotEntity now : needOCR) {
            int nowId = now.getId();
            ScreenshotEntity before = screenshotService.findById(nowId - 1);
//            if (before == null) {
//                fullOCR(now);
//                continue;
//            }
            ScreenshotEntity previousFullOCR = screenshotService.findPreviousFullOCR(nowId);
            if (previousFullOCR == null || previousFullOCR.getTime().getTime() < now.getTime().getTime() - 1000 * 60) {
                fullOCR(now);
                continue;
            }
            String path1 = timestampToFullPath(before.getTime());
            String path2 = timestampToFullPath(now.getTime());
            if (!(new File(path1).exists() && new File(path2).exists())) {
                continue;
            }
            Mat mat1 = Imgcodecs.imread(timestampToFullPath(before.getTime()));
            Mat mat2 = Imgcodecs.imread(timestampToFullPath(now.getTime()));
            if (compareImage(mat1, mat2) < 0.4) {
                fullOCR(now);
                continue;
            }
            Mat diff = simpleDiff(mat1, mat2);
            String base64 = matToBase64(diff);
            String diffText = ocrProcessor.ocr(base64);
            now.setText(diffText);
            now.setAppend(true);
            screenshotService.save(now);
        }
    }

}
