package org.ztmzzz.backtrackr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ztmzzz.backtrackr.ScheduledTask;

@RestController
public class ScreenshotController {

    private final ScheduledTask scheduledTask;

    @Autowired
    public ScreenshotController(ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    @PostMapping("/startScreenshot")
    public void startScreenshot() {
        scheduledTask.runScreenshot = true;
    }

    @PostMapping("/stopScreenshot")
    public void stopScreenshot() {
        scheduledTask.runScreenshot = false;
    }

}
