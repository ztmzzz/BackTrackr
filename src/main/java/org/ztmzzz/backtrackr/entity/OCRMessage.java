package org.ztmzzz.backtrackr.entity;

import lombok.Data;

import java.util.List;
@Data
public class OCRMessage {
    private List<OCRData> data;
    private String save_path;
}
