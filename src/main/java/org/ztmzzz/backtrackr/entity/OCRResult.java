package org.ztmzzz.backtrackr.entity;

import jakarta.persistence.Entity;
import lombok.Data;

import java.util.List;

@Data
public class OCRResult {
    private List<OCRMessage> msg;
    private String results;
    private String status;
}


