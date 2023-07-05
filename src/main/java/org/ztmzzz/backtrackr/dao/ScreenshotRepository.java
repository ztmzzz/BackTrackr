package org.ztmzzz.backtrackr.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.ztmzzz.backtrackr.entity.ScreenshotEntity;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface ScreenshotRepository extends JpaRepository<ScreenshotEntity, Integer> {
    List<ScreenshotEntity> findByTextIsNull();

    boolean existsByTime(Timestamp time);

    ScreenshotEntity findFirstByAppendIsFalseAndIdLessThanOrderByIdDesc(Integer id);

    List<ScreenshotEntity> findByTextContaining(String text);

    ScreenshotEntity findFirstByAppendIsFalseAndIdGreaterThanOrderByIdAsc(Integer id);

    ScreenshotEntity findTopByOrderByIdDesc();

    @Query("SELECT s.id as id, s.time as time FROM ScreenshotEntity s")
    List<Map<String, Object>> findAllIdAndTime();

    @Query("SELECT e FROM ScreenshotEntity e WHERE CAST(e.time AS date) = CAST(:timestamp AS date)")
    List<ScreenshotEntity> findAllByDate(@Param("timestamp") Timestamp timestamp);
    @Query("SELECT e FROM ScreenshotEntity e WHERE e.time >= :start AND e.time < :end ORDER BY e.time")
    List<ScreenshotEntity> findFirstRecordOfTheDay(@Param("start") Timestamp start, @Param("end") Timestamp end);

}
