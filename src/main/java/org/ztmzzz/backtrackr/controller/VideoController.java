package org.ztmzzz.backtrackr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.ztmzzz.backtrackr.VideoProcessor;
import org.ztmzzz.backtrackr.service.ScreenshotService;

import java.util.Map;

@RestController
public class VideoController {
    private final VideoProcessor videoProcessor;
    private final ScreenshotService screenshotService;

    @Autowired
    public VideoController(VideoProcessor videoProcessor, ScreenshotService screenshotService) {
        this.videoProcessor = videoProcessor;
        this.screenshotService = screenshotService;
    }

    @PostMapping("/generateVideo")
    public String videoInfo(String year, String month, String day) {
        videoProcessor.generateVideo(year, month, day);
        return "ok";
    }

    @PostMapping("/videoToStartId")
    public int videoToStartId(@RequestBody Map<String, String> params) {
        String path = params.get("videoPath");
        String[] split = path.split("-");
        int year = Integer.parseInt(split[0]);
        int month = Integer.parseInt(split[1]);
        int day = Integer.parseInt(split[2]);
        return screenshotService.getFirstRecordOfTheDay(year, month, day).getId();
    }
}
