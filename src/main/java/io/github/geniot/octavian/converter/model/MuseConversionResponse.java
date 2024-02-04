package io.github.geniot.octavian.converter.model;

import lombok.Data;

@Data
public class MuseConversionResponse {
    byte[] pngSheet;
    Tune tune;
    byte[] repeatsPngSheet;
    Tune repeatsTune;
    int[] repeats;
}
