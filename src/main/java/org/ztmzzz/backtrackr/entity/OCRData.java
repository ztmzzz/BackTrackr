package org.ztmzzz.backtrackr.entity;

import lombok.Data;

import java.util.List;
@Data
public class OCRData {
    private double confidence;
    private String text;
    private List<List<Integer>> text_box_position;
}
