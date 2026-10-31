package io.github.geniot.octavian.converter.svg.commands;

import lombok.Setter;

/**
 * MoveTo
 * <a href="https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/d#path_commands">...</a>
 */
@Setter
public class MCommand extends DCommand {
    float x;
    float y;

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }
}
