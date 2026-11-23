package io.github.geniot.octavian.converter;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Chord {
    C("C"), Cm("Cm"), C7("C7"), Cd("Cd"),
    CD("CD"), CDm("CDm"), CD7("CD7"), CDd("CDd"),
    D("D"), Dm("Dm"), D7("D7"), Dd("Dd"),
    DE("DE"), DEm("DEm"), DE7("DE7"), DEd("DEd"),
    E("E"), Em("Em"), E7("E7"), Ed("Ed"),
    F("F"), Fm("Fm"), F7("F7"), Fd("Fd"),
    FG("FG"), FGm("FGm"), FG7("FG7"), FGd("FGd"),
    G("G"), Gm("Gm"), G7("G7"), Gd("Gd"),
    GA("GA"), GAm("GAm"), GA7("GA7"), GAd("GAd"),
    A("A"), Am("Am"), A7("A7"), Ad("Ad"),
    AB("AB"), ABm("ABm"), AB7("AB7"), ABd("ABd"),
    B("B"), Bm("Bm"), B7("B7"), Bd("Bd");

    private final String chordName;

    Chord(String roleName) {
        this.chordName = roleName;
    }

    public static Chord getByValue(String value) {
        return Arrays.stream(Chord.values()).filter(enumRole -> enumRole.chordName.equals(value)).findFirst().orElse(null);
    }
}
