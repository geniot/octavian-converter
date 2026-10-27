package io.github.geniot.octavian.converter.verovio;

import lombok.Data;

import javax.sound.midi.Instrument;

@Data
public class MuseConversionRequest {
    String museXml;
    int pngHeight;
    String author;
    String title;
    Instrument instrument;
}
