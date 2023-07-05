package org.ztmzzz.backtrackr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ztmzzz.backtrackr.ScheduledTask;

@RestController
public class OCRController {
    private final ScheduledTask scheduledTask;

    @Autowired
    public OCRController(ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    @PostMapping("/startOCR")
    public void startOCR() {
        scheduledTask.runOCR = true;
    }

    @PostMapping("/stopOCR")
    public void stopOCR() {
        scheduledTask.runOCR = false;
    }
}
