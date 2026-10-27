package io.github.geniot.octavian.converter.model;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.io.Serializable;


@Data
@JsonPropertyOrder({"id", "ownerId", "title", "author", "instrument",
        "sheetWidth", "sheetHeight", "pngWidth", "pngHeight", "playHeadWidth",
        "barOffsets", "selectionRanges", "points"})
public class Tune implements Serializable {
    private String author;
    private String title;
    int pngWidth;
    int pngHeight;
    private int[] selectionRanges;
    private int[] barOffsets;
    private Point[] points;
    private int sheetWidth;
    private int sheetHeight;
    private int playHeadWidth;
}
