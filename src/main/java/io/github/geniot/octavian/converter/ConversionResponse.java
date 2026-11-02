package io.github.geniot.octavian.converter;

import io.github.geniot.octavian.converter.json.Tune;
import lombok.Data;

@Data
public class ConversionResponse {
    byte[] pngSheet;
    Tune tune;
}
