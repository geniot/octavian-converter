package io.github.geniot.octavian.converter.model.commands;

import lombok.Setter;

/**
 * MoveTo
 * https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/d#path_commands
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
