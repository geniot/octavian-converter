package io.github.geniot.octavian.converter.model;

import lombok.Data;

@Data
public class MuseConversionResponse {
    byte[] pngSheet;
    byte[] mp3;
    Tune tune;
}
