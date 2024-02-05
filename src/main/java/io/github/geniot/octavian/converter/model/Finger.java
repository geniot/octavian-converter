package io.github.geniot.octavian.converter.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Finger implements Serializable {
    String finger;
    Integer button;

    public Finger() {
    }

    public Finger(String f) {
        this.finger = f;
    }
}
