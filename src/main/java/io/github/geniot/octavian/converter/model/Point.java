package io.github.geniot.octavian.converter.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Data
public class Point implements Serializable {
    int timestamp;
    int offsetX;
    int bar;

    Note[] notesOn;
    Note[] notesOff;
}
