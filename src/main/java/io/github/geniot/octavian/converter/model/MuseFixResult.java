package io.github.geniot.octavian.converter.model;

import io.github.geniot.octavian.converter.model.musescore.MuseScoreRoot;
import lombok.Data;

@Data
public class MuseFixResult {

    String score;
    String wideScore;
    String leftScore;
    String rightScore;
    MuseScoreRoot rightRoot;
    MuseScoreRoot leftRoot;
}
