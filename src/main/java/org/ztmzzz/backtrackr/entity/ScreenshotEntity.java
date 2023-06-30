package org.ztmzzz.backtrackr.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
@Table(name = "screenshot")
public class ScreenshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "time")
    private Timestamp time;

    @Column(name = "title")
    private String title;

    @Column(name = "process")
    private String process;

    @Lob
    @Column(name = "text", columnDefinition = "CLOB")
    private String text;

    @Column(name = "append")
    private boolean append;


}
