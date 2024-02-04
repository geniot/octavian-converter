package io.github.geniot.octavian.converter.model;


import lombok.Data;

import java.io.Serializable;


@Data
public class Tune implements Serializable {
    private String author;
    private String title;
    private int[] barOffsets;
    private Point[] points;
    private int sheetWidth;
    private int sheetHeight;
    private int playHeadWidth;


}
