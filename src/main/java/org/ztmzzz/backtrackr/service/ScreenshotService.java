package org.ztmzzz.backtrackr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ztmzzz.backtrackr.dao.ScreenshotRepository;
import org.ztmzzz.backtrackr.entity.ScreenshotEntity;

import java.util.List;

@Service
public class ScreenshotService {

    private final ScreenshotRepository screenshotRepository;

    @Autowired
    public ScreenshotService(ScreenshotRepository screenshotRepository) {
        this.screenshotRepository = screenshotRepository;
    }

    public ScreenshotEntity save(ScreenshotEntity screenshotEntity) {
        return screenshotRepository.save(screenshotEntity);
    }

    public ScreenshotEntity findById(Integer id) {
        return screenshotRepository.findById(id).orElse(null);
    }

    public List<ScreenshotEntity> findAll() {
        return screenshotRepository.findAll();
    }

    public List<ScreenshotEntity> findByTextIsNull() {
        return screenshotRepository.findByTextIsNull();
    }

    public boolean existsByTime(java.sql.Timestamp time) {
        return screenshotRepository.existsByTime(time);
    }

    public ScreenshotEntity findPreviousFullOCR(Integer id) {
        return screenshotRepository.findFirstByAppendIsFalseAndIdLessThanOrderByIdDesc(id);
    }

    public List<ScreenshotEntity> findByTextContainingOrderByIDDesc(String text) {
        return screenshotRepository.findByTextContainingOrderByIDDesc(text);
    }
}
