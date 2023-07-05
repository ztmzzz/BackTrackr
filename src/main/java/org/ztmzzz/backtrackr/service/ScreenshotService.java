package org.ztmzzz.backtrackr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ztmzzz.backtrackr.dao.ScreenshotRepository;
import org.ztmzzz.backtrackr.entity.ScreenshotEntity;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public List<ScreenshotEntity> findByTextContaining(String text) {
        return screenshotRepository.findByTextContaining(text);
    }

    public ScreenshotEntity findNextFullOCR(Integer id) {
        return screenshotRepository.findFirstByAppendIsFalseAndIdGreaterThanOrderByIdAsc(id);
    }

    public ScreenshotEntity findTopByOrderByIdDesc() {
        return screenshotRepository.findTopByOrderByIdDesc();
    }

    public Timestamp getTimeById(Integer id) {
        return Objects.requireNonNull(screenshotRepository.findById(id).orElse(null)).getTime();
    }

    public Map<Integer, Timestamp> getFrameToTime() {
        List<Map<String, Object>> idAndTimeList = screenshotRepository.findAllIdAndTime();
        return idAndTimeList.stream()
                .collect(Collectors.toMap(
                        map -> (Integer) map.get("id"),
                        map -> (Timestamp) map.get("time")
                ));
    }

    public List<ScreenshotEntity> getAllByDate(Timestamp timestamp) {
        return screenshotRepository.findAllByDate(timestamp);
    }

    public ScreenshotEntity getFirstRecordOfTheDay(int year, int month, int day) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(year, month - 1, day, 0, 0, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(year, month - 1, day, 23, 59, 59);
        endCalendar.set(Calendar.MILLISECOND, 999);

        Timestamp start = new Timestamp(startCalendar.getTimeInMillis());
        Timestamp end = new Timestamp(endCalendar.getTimeInMillis());

        List<ScreenshotEntity> records = screenshotRepository.findFirstRecordOfTheDay(start, end);
        if (!records.isEmpty()) {
            return records.get(0);
        } else {
            return null;
        }
    }
}