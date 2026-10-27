package io.github.geniot.octavian.converter.verovio.json;

import lombok.Data;

import java.io.Serializable;

@Data
public class Point implements Serializable {
    int timestamp;
    int offsetX;
    int bar;

    Note[] notesOn;
    Note[] notesOff;
}
