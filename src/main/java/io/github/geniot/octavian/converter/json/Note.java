package io.github.geniot.octavian.converter.json;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
public class Note implements Serializable {
    Integer duration;
    NoteType noteType;
    Integer noteValue;
    String noteName;
    String chordName;
    Map<String, Finger> fingers;
}
