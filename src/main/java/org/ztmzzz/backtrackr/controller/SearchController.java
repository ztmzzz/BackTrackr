package org.ztmzzz.backtrackr.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.ztmzzz.backtrackr.entity.OCRData;
import org.ztmzzz.backtrackr.entity.OCRMessage;

import org.ztmzzz.backtrackr.entity.ScreenshotEntity;
import org.ztmzzz.backtrackr.service.ScreenshotService;

import java.sql.Timestamp;
import java.util.*;

@RestController
public class SearchController {
    private final ScreenshotService screenshotService;

    @Autowired
    public SearchController(ScreenshotService screenshotService) {
        this.screenshotService = screenshotService;
    }

    @Data
    @AllArgsConstructor
    static
    class SearchResult {
        List<Timestamp[]> periodTime;
        List<int[]> periodId;
        HashMap<Integer, List<int[]>> timeToTextBox;
    }

    @PostMapping("/search")
    public String search(@RequestBody Map<String, String> params) throws JsonProcessingException {
        String text = params.get("text");
        String[] textArray = text.split(" ");
        ArrayList<ScreenshotEntity> allFind = new ArrayList<>();
        for (String s : textArray) {
            allFind.addAll(screenshotService.findByTextContaining(s));
        }
        allFind.sort(Comparator.comparing(ScreenshotEntity::getId));
        //寻找关键词可能存在的时间段
        List<Timestamp[]> periodTime = new ArrayList<>();
        List<int[]> periodId = new ArrayList<>();
        int end = 0;
        for (ScreenshotEntity entity : allFind) {
            if (end >= entity.getId()) {
                continue;
            }
            int start = entity.getId();
            ScreenshotEntity nextFullOCR = screenshotService.findNextFullOCR(entity.getId());
            if (nextFullOCR == null) {
                end = screenshotService.findTopByOrderByIdDesc().getId();
            } else {
                end = nextFullOCR.getId() - 1;
            }
            periodTime.add(new Timestamp[]{screenshotService.getTimeById(start), screenshotService.getTimeById(end)});
            periodId.add(new int[]{start, end});
        }
        //得到时间对应文本框的列表
        HashMap<Integer, List<int[]>> timeToTextBox = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        for (ScreenshotEntity entity : allFind) {
            //一个时间点
            List<int[]> textBoxList = new ArrayList<>();
            OCRMessage ocrResult = objectMapper.readValue(entity.getText(), OCRMessage.class);
            for (OCRData data : ocrResult.getData()) {
                //一个文本框
                String dataText = data.getText();
                double confidence = data.getConfidence();
                int[] textBox = new int[4];
                for (String s : textArray) {
                    if (dataText.contains(s)) {
                        List<List<Integer>> points = data.getText_box_position();
                        textBox[0] = points.get(0).get(0);
                        textBox[1] = points.get(0).get(1);
                        textBox[2] = points.get(1).get(0) - points.get(0).get(0);
                        textBox[3] = points.get(2).get(1) - points.get(0).get(1);
                        textBoxList.add(textBox);
                        break;
                    }
                }
            }
            timeToTextBox.put(entity.getId(), textBoxList);
        }
        //按照json返回数据
        SearchResult result = new SearchResult(periodTime, periodId, timeToTextBox);
        return objectMapper.writeValueAsString(result);
    }
}
