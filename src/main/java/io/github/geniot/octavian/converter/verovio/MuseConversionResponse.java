package io.github.geniot.octavian.converter.verovio;

import io.github.geniot.octavian.converter.verovio.json.Tune;
import lombok.Data;

@Data
public class MuseConversionResponse {
    byte[] pngSheet;
    byte[] mp3;
    Tune tune;
    int pngWidth;
    int pngHeight;
}
