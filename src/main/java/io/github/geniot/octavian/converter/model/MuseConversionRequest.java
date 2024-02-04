package io.github.geniot.octavian.converter.model;

import lombok.Data;

@Data
public class MuseConversionRequest {
    String museXml;
    int pngHeight;
    String author;
    String title;
    Instrument instrument;
}
