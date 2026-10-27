package io.github.geniot.octavian.converter.model;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Note implements Serializable {
    Integer duration;
    NoteType noteType;
    Integer noteValue;
    String noteName;
    String chordName;
}
