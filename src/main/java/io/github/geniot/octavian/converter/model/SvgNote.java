package io.github.geniot.octavian.converter.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class SvgNote implements Comparable<SvgNote>, Serializable {

    float x;
    float y;

    float width;
    float height;

    int measure;//used in debugging
    boolean isUpperStaff;

    public float getCenter() {
        return y - height / 2;
    }

    @Override
    public int compareTo(SvgNote o) {
        if (this.x != o.x) {
            return Float.compare(this.x, o.x);
        }
        if (this.y != o.y) {
            return Float.compare(o.y, this.y);//musescore notes start from lower pitch
        }
        throw new RuntimeException("SvgNotes cannot have the same coordinates.");
    }
}
