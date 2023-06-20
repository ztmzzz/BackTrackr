package org.ztmzzz.backtrackr.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
@Table(name = "frame")
public class Frame {
    @Id
    @Column(name = "TIME")
    private Timestamp time;

    @Column(name = "window_name")
    private String windowName;

    @Lob
    @Column(name = "TEXT", columnDefinition = "CLOB")
    private String text;


}
