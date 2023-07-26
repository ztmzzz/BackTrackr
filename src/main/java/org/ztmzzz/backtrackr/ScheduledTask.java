package org.ztmzzz.backtrackr;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.ztmzzz.backtrackr.entity.ScreenshotEntity;
import org.ztmzzz.backtrackr.service.ScreenshotService;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

import static org.ztmzzz.backtrackr.ImageProcessor.*;
import static org.ztmzzz.backtrackr.ScreenshotProcessor.screenshotPath;
import static org.ztmzzz.backtrackr.ScreenshotProcessor.timestampToFullPath;

@Component
public class ScheduledTask {

    private final ScreenshotProcessor screenshotProcessor;
    private final OCRProcessor ocrProcessor;
    private final ScreenshotService screenshotService;
    private final VideoProcessor videoProcessor;
    public boolean runOCR = false;
    public boolean runScreenshot = false;
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTask.class);

    @Autowired
    public ScheduledTask(ScreenshotProcessor screenshotProcessor, OCRProcessor ocrProcessor, ScreenshotService screenshotService, VideoProcessor videoProcessor) {
        this.screenshotProcessor = screenshotProcessor;
        this.ocrProcessor = ocrProcessor;
        this.screenshotService = screenshotService;
        this.videoProcessor = videoProcessor;
    }

    @Scheduled(fixedRate = 2000)
    public void screenshotTask() throws IOException, AWTException {
        if (!runScreenshot) {
            return;
        }
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

    @Scheduled(fixedDelay = 4000)
    //@Async("taskExecutor")
    public void ocrTask() throws IOException {
        if (!runOCR) {
            return;
        }
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
            Mat diff = simpleDiff(mat2, mat1);
            String base64 = matToBase64(diff);
            String diffText = ocrProcessor.ocr(base64);
            now.setText(diffText);
            now.setAppend(true);
            screenshotService.save(now);
        }
    }

    enum Flag {
        BASE, YEAR, MONTH, DAY
    }

    private void explore(File file, Flag flag) {
        if (flag == Flag.BASE) {
            for (File subFile : Objects.requireNonNull(file.listFiles())) {
                if (subFile.isDirectory()) {
                    explore(subFile, Flag.YEAR);
                }
            }
            return;
        }
        if (flag == Flag.YEAR) {
            for (File subFile : Objects.requireNonNull(file.listFiles())) {
                if (subFile.isDirectory()) {
                    explore(subFile, Flag.MONTH);
                }
            }
            return;
        }
        if (flag == Flag.MONTH) {
            for (File subFile : Objects.requireNonNull(file.listFiles())) {
                if (subFile.isDirectory()) {
                    explore(subFile, Flag.DAY);
                }
            }
            return;
        }
        if (flag == Flag.DAY) {
            Path path = Paths.get(file.getPath());
            String year = path.getName(path.getNameCount() - 3).toString();
            String month = path.getName(path.getNameCount() - 2).toString();
            String day = path.getName(path.getNameCount() - 1).toString();
            LocalDate dateFromPath = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
            LocalDate currentDate = LocalDate.now();
            if (dateFromPath.isBefore(currentDate)) {
                if (videoProcessor.generateVideo(year, month, day)) {
                    try {
                        deleteDir(file.toPath());
                        deleteIfEmpty(file.toPath().getParent());
                    } catch (IOException e) {
                        logger.error("delete dir error");
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void deleteIfEmpty(Path path) throws IOException {
        if (Paths.get(screenshotPath).toRealPath().equals(path.toRealPath())) {
            return;
        }
        if (Files.isDirectory(path) && isDirectoryEmpty(path)) {
            Files.deleteIfExists(path);
            deleteIfEmpty(path.getParent());
        }
    }

    public static boolean isDirectoryEmpty(Path path) throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(path)) {
            return !dirStream.iterator().hasNext();
        }
    }

    private void deleteDir(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });

    }

    @Scheduled(fixedRate = 1000 * 60 * 60 * 8)
    public void generateVideo() {
        explore(new File(screenshotPath), Flag.BASE);
    }

}
