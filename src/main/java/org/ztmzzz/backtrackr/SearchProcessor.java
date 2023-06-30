package org.ztmzzz.backtrackr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ztmzzz.backtrackr.entity.ScreenshotEntity;
import org.ztmzzz.backtrackr.service.ScreenshotService;

import java.util.List;

@Component
public class SearchProcessor {
    private final ScreenshotService screenshotService;

    @Autowired
    public SearchProcessor(ScreenshotService screenshotService) {
        this.screenshotService = screenshotService;
    }

    public List<String> search(String text) {
        List<ScreenshotEntity> allFind = screenshotService.findByTextContainingOrderByIDDesc(text);
        return null;
    }
}
