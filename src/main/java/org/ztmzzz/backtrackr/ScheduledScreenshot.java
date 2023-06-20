package org.ztmzzz.backtrackr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.ztmzzz.backtrackr.entity.Frame;
import org.ztmzzz.backtrackr.service.FrameService;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;

@Component
public class ScheduledScreenshot {

    private final Screenshot screenshot;
    private final OCR ocr;
    private final FrameService frameService;

    @Autowired
    public ScheduledScreenshot(Screenshot screenshot, OCR ocr, FrameService frameService) {
        this.screenshot = screenshot;
        this.ocr = ocr;
        this.frameService = frameService;
    }

    @Scheduled(fixedRate = 2000)
    @Async("taskExecutor")
    public void performTask() throws IOException, AWTException {
        if (!ocr.isServiceStarted()) {
            return;
        }
        Screenshot.ScreenshotInfo info = screenshot.screenshot();
        BufferedImage image = info.image;
        String text = ocr.getAllText(image);
        Frame frame = new Frame();
        Timestamp timestamp = Timestamp.from(Instant.now());
        frame.setTime(timestamp);
        frame.setText(text);
        frame.setWindowName(info.windowName);
        frameService.save(frame);
    }
}
