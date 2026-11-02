package io.github.geniot.octavian.converter.json;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.io.Serializable;


@Data
@JsonPropertyOrder({
        "title",
        "author",
        "pngWidth",
        "pngHeight",
        "sheetWidth",
        "sheetHeight",
        "playHeadWidth",
        "selectionRanges",
        "barOffsets",
        "points"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tune implements Serializable {
    private String title;
    private String author;
    int pngWidth;
    int pngHeight;
    private int sheetWidth;
    private int sheetHeight;
    private int playHeadWidth;
    private int[] selectionRanges;
    private int[] barOffsets;
    private Point[] points;
}
