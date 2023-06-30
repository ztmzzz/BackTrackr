package org.ztmzzz.backtrackr.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ztmzzz.backtrackr.entity.ScreenshotEntity;

import java.sql.Timestamp;
import java.util.List;

public interface ScreenshotRepository extends JpaRepository<ScreenshotEntity, Integer> {
    List<ScreenshotEntity> findByTextIsNull();
    boolean existsByTime(Timestamp time);
    ScreenshotEntity findFirstByAppendIsFalseAndIdLessThanOrderByIdDesc(Integer id);
    List<ScreenshotEntity> findByTextContainingOrderByIDDesc(String text);
}
