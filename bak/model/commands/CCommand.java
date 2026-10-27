package io.github.geniot.octavian.converter.model.commands;

import lombok.Setter;

/**
 * Draw a cubic Bézier curve from the current point to the end point specified by x,y.
 * The start control point is specified by x1,y1 and the end control point is specified by x2,y2.
 */
@Setter
public class CCommand extends DCommand {
    float x1;
    float y1;

    float x2;
    float y2;

    float x3;
    float y3;

    @Override
    public float getX() {
        return x3;
    }

    @Override
    public float getY() {
        return y3;
    }
}
