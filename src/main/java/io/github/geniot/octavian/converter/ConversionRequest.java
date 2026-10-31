package io.github.geniot.octavian.converter;

import lombok.Data;

import javax.sound.midi.Instrument;

@Data
public class ConversionRequest {
    String museXml;
    int pngHeight;
    String author;
    String title;
    Instrument instrument;
}
