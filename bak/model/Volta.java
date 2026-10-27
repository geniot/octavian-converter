package io.github.geniot.octavian.converter.model;

import lombok.Data;

import java.util.TreeSet;

@Data
public class Volta {
    int nextLocationMeasures = 0;
    TreeSet<Integer> repeatList = new TreeSet<>();
}
