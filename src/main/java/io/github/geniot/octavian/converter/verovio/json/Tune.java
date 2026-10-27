package io.github.geniot.octavian.converter.verovio.json;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.io.Serializable;


@Data
@JsonPropertyOrder({
        "title",
        "author",
        "pngWidth",
        "pngHeight",
        "selectionRanges",
        "sheetWidth",
        "sheetHeight",
        "playHeadWidth",
        "barOffsets",
        "points"
})
public class Tune implements Serializable {
    private String title;
    private String author;
    int pngWidth;
    int pngHeight;
    private int[] selectionRanges;
    private int sheetWidth;
    private int sheetHeight;
    private int playHeadWidth;
    private int[] barOffsets;
    private Point[] points;
}
