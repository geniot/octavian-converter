package io.github.geniot.octavian.converter.model;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.io.Serializable;


@Data
@JsonPropertyOrder({"title", "author", "sheetWidth", "sheetHeight", "playHeadWidth", "barOffsets", "points"})
public class Tune implements Serializable {
    private String author;
    private String title;
    private int[] barOffsets;
    private Point[] points;
    private int sheetWidth;
    private int sheetHeight;
    private int playHeadWidth;
    private String free = "N";
    private long lastUpdatedOn;

}
