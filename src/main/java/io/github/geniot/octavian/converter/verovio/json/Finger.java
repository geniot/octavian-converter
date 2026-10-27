package io.github.geniot.octavian.converter.verovio.json;

import lombok.Data;

import java.io.Serializable;

@Data
public class Finger implements Serializable {
    String finger;
    Integer button;
}
