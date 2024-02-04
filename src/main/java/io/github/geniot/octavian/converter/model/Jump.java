package io.github.geniot.octavian.converter.model;

import lombok.Data;

@Data
public class Jump {
    public String jumpTo;
    public String playUntil;
    public String continueAt;
    public boolean shouldPlayRepeats = false;
    public boolean isJumped = false;
}
