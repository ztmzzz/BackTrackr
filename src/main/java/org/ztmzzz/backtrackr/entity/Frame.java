package org.ztmzzz.backtrackr.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
@Table(name = "frame")
public class Frame {
    @Id
    @Column(name = "time")
    private Timestamp time;

    @Column(name = "windowName")
    private String windowName;

    @Lob
    @Column(name = "text", columnDefinition = "CLOB")
    private String text;


}
