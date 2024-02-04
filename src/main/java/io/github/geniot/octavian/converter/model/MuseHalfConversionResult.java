package io.github.geniot.octavian.converter.model;

import lombok.Data;

@Data
public class MuseHalfConversionResult {

    byte[] svgBytes;
    byte[] wideSvgBytes;
    byte[] leftMidiBytes;
    byte[] rightMidiBytes;

    byte[] repeatsSvgBytes;
    byte[] repeatsWideSvgBytes;
    byte[] repeatsLeftMidiBytes;
    byte[] repeatsRightMidiBytes;

}
