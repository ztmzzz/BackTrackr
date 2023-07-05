package org.ztmzzz.backtrackr.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.ztmzzz.backtrackr.entity.ScreenshotEntity;
import org.ztmzzz.backtrackr.service.ScreenshotService;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.ztmzzz.backtrackr.ScreenshotProcessor.screenshotPath;

@RestController
public class TimeController {
    private final ScreenshotService screenshotService;

    @Autowired
    public TimeController(ScreenshotService screenshotService) {
        this.screenshotService = screenshotService;
    }


    @GetMapping("/frameTime")
    public Map<Integer, Timestamp> frameTime() {
        return screenshotService.getFrameToTime();
    }

    @PostMapping("/videoInfo")
    public Map<String, String> videoInfo(@RequestBody Map<String, Integer> params) throws Exception {
        int id = params.get("id");
        ScreenshotEntity now = screenshotService.findById(id);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now.getTime().getTime());
        String year = String.valueOf(cal.get(Calendar.YEAR));
        String month = String.format("%02d", cal.get(Calendar.MONTH) + 1);
        String day = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));
        String path = "video/" + year + "-" + month + "-" + day + ".mp4";
        int time = -1;
        List<ScreenshotEntity> all = screenshotService.getAllByDate(now.getTime());
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId() == id) {
                time = i;
                break;
            }
        }
        if (time == -1) {
            throw new Exception("id not found");
        }
        return Map.of("path", path, "time", String.valueOf(time));
    }

}
