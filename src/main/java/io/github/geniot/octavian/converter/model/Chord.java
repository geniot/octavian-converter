package io.github.geniot.octavian.converter.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.valueOf;

public class Chord {
    public static final String C = "C";
    public static final String Cm = "Cm";
    public static final String C7 = "C7";
    public static final String Cd = "Cd";

    public static final String CD = "CD";
    public static final String CDm = "CDm";
    public static final String CD7 = "CD7";
    public static final String CDd = "CDd";

    public static final String D = "D";
    public static final String Dm = "Dm";
    public static final String D7 = "D7";
    public static final String Dd = "Dd";

    public static final String DE = "DE";
    public static final String DEm = "DEm";
    public static final String DE7 = "DE7";
    public static final String DEd = "DEd";

    public static final String E = "E";
    public static final String Em = "Em";
    public static final String E7 = "E7";
    public static final String Ed = "Ed";

    public static final String F = "F";
    public static final String Fm = "Fm";
    public static final String F7 = "F7";
    public static final String Fd = "Fd";

    public static final String FG = "FG";
    public static final String FGm = "FGm";
    public static final String FG7 = "FG7";
    public static final String FGd = "FGd";

    public static final String G = "G";
    public static final String Gm = "Gm";
    public static final String G7 = "G7";
    public static final String Gd = "Gd";

    public static final String GA = "GA";
    public static final String GAm = "GAm";
    public static final String GA7 = "GA7";
    public static final String GAd = "GAd";

    public static final String A = "A";
    public static final String Am = "Am";
    public static final String A7 = "A7";
    public static final String Ad = "Ad";

    public static final String AB = "AB";
    public static final String ABm = "ABm";
    public static final String AB7 = "AB7";
    public static final String ABd = "ABd";

    public static final String B = "B";
    public static final String Bm = "Bm";
    public static final String B7 = "B7";
    public static final String Bd = "Bd";

    //notes
    public static final String C3 = valueOf(48);
    public static final String CD3 = valueOf(49);
    public static final String D3 = valueOf(50);
    public static final String DE3 = valueOf(51);
    public static final String E3 = valueOf(52);
    public static final String F3 = valueOf(53);
    public static final String FG3 = valueOf(54);
    public static final String G3 = valueOf(55);
    public static final String GA3 = valueOf(56);
    public static final String A3 = valueOf(57);
    public static final String AB3 = valueOf(58);
    public static final String B3 = valueOf(59);

    public static final String C4 = valueOf(60);
    public static final String CD4 = valueOf(61);
    public static final String D4 = valueOf(62);
    public static final String DE4 = valueOf(63);
    public static final String E4 = valueOf(64);
    public static final String F4 = valueOf(65);
    public static final String FG4 = valueOf(66);
    public static final String G4 = valueOf(67);
    public static final String GA4 = valueOf(68);
    public static final String A4 = valueOf(69);
    public static final String AB4 = valueOf(70);
    public static final String B4 = valueOf(71);

    private static final Map<String[], String> CHORDS_MAP_INTERNAL = Collections.unmodifiableMap(
            new HashMap<>() {{

                //major
                put(new String[]{G3, C4, E4}, C);//https://www.basicmusictheory.com/c-major-triad-chord
                put(new String[]{CD3, F3, GA3}, CD);//https://www.basicmusictheory.com/c-sharp-major-triad-chord
                put(new String[]{FG3, A3, D4}, D);//https://www.basicmusictheory.com/d-major-triad-chord
                put(new String[]{G3, AB3, DE4}, DE);//https://www.basicmusictheory.com/d-sharp-major-triad-chord
                put(new String[]{E3, GA3, B3}, E);//https://www.basicmusictheory.com/e-major-triad-chord
                put(new String[]{A3, C4, F4}, F);//https://www.basicmusictheory.com/f-major-triad-chord
                put(new String[]{FG3, AB3, CD4}, FG);//https://www.basicmusictheory.com/f-sharp-major-triad-chord
                put(new String[]{G3, B3, D4}, G);//https://www.basicmusictheory.com/g-major-triad-chord
                put(new String[]{GA3, C4, DE4}, GA);//https://www.basicmusictheory.com/g-sharp-major-triad-chord
                put(new String[]{E3, A3, CD4}, A);//https://www.basicmusictheory.com/a-major-triad-chord
                put(new String[]{F3, AB3, D4}, AB);//https://www.basicmusictheory.com/a-sharp-major-triad-chord
                put(new String[]{FG3, B3, DE4}, B);//https://www.basicmusictheory.com/b-major-triad-chord

                //major -> last to first
                put(new String[]{E3, G3, C4}, C);
                put(new String[]{GA3, CD3, F3}, CD);
                put(new String[]{D3, FG3, A3}, D);
                put(new String[]{DE3, G3, AB3}, DE);
                put(new String[]{B3, E4, GA4}, E);
                put(new String[]{F3, A3, C4}, F);
                put(new String[]{CD3, FG3, AB3}, FG);
                put(new String[]{D3, G3, B3}, G);
                put(new String[]{DE3, GA3, C4}, GA);
                put(new String[]{CD3, E3, A3}, A);
                put(new String[]{D3, F3, AB3}, AB);
                put(new String[]{DE3, FG3, B3}, B);

                //major -> first to last
                put(new String[]{C3, E3, G3}, C);
                put(new String[]{F3, GA3, CD4}, CD);
                put(new String[]{A3, D4, FG4}, D);
                put(new String[]{AB3, DE4, G4}, DE);
                put(new String[]{GA3, B3, E4}, E);
                put(new String[]{C3, F3, A3}, F);
                put(new String[]{AB3, CD4, FG4}, FG);
                put(new String[]{B3, D4, G4}, G);
                put(new String[]{C3, DE3, GA3}, GA);
                put(new String[]{A3, CD4, E4}, A);
                put(new String[]{AB3, D4, F4}, AB);
                put(new String[]{B3, DE4, FG4}, B);

//                put(new String[]{GA3, B3, E4}, E);

                //minor
                put(new String[]{G3, C4, DE4}, Cm);//https://www.basicmusictheory.com/c-minor-triad-chord
                put(new String[]{E3, GA3, CD4}, CDm);//https://www.basicmusictheory.com/c-sharp-minor-triad-chord
                put(new String[]{F3, A3, D4}, Dm);//https://www.basicmusictheory.com/d-minor-triad-chord
                put(new String[]{FG3, AB3, DE4}, DEm);//https://www.basicmusictheory.com/d-sharp-minor-triad-chord
                put(new String[]{E3, G3, B3}, Em);//https://www.basicmusictheory.com/e-minor-triad-chord
                put(new String[]{GA3, C4, F4}, Fm);//https://www.basicmusictheory.com/f-minor-triad-chord
                put(new String[]{FG3, A3, CD4}, FGm);//https://www.basicmusictheory.com/f-sharp-minor-triad-chord
                put(new String[]{G3, AB3, D4}, Gm);//https://www.basicmusictheory.com/g-minor-triad-chord
                put(new String[]{GA3, B3, DE4}, GAm);//https://www.basicmusictheory.com/g-sharp-minor-triad-chord
                put(new String[]{E3, A3, C4}, Am);//https://www.basicmusictheory.com/a-minor-triad-chord
                put(new String[]{F3, AB3, CD4}, ABm);//https://www.basicmusictheory.com/a-sharp-minor-triad-chord
                put(new String[]{FG3, B3, D4}, Bm);//https://www.basicmusictheory.com/b-minor-triad-chord

                //minor -> put first to last, octave down for all if necessary
                put(new String[]{C3, DE3, G3}, Cm);//octave down
                put(new String[]{GA3, CD4, E4}, CDm);
                put(new String[]{A3, D4, F4}, Dm);
                put(new String[]{AB3, DE4, FG4}, DEm);
                put(new String[]{G3, B3, E4}, Em);
                put(new String[]{C3, F3, GA3}, Fm);//octave down
                put(new String[]{A3, CD4, FG4}, FGm);
                put(new String[]{AB3, D4, G4}, Gm);
                put(new String[]{B3, DE4, GA4}, GAm);
                put(new String[]{A3, C4, E4}, Am);
                put(new String[]{AB3, CD4, F4}, ABm);
                put(new String[]{B3, D4, FG4}, Bm);

                //minor -> put last to first, octave up if necessary
                put(new String[]{DE3, G3, C4}, Cm);
                put(new String[]{CD3, E3, GA3}, CDm);
                put(new String[]{D3, F3, A3}, Dm);
                put(new String[]{DE3, FG3, AB3}, DEm);
                put(new String[]{B3, E4, G4}, Em);
                put(new String[]{F3, GA3, C4}, Fm);
                put(new String[]{CD3, FG3, A3}, FGm);
                put(new String[]{D3, G3, AB3}, Gm);
                put(new String[]{DE3, GA3, B3}, GAm);
                put(new String[]{C3, E3, A3}, Am);
                put(new String[]{CD3, F3, AB3}, ABm);
                put(new String[]{D3, FG3, B3}, Bm);

                put(new String[]{C4, DE4, G4}, Cm);

                //7th-chord
                put(new String[]{G3, AB3, C4, E4}, C7);//https://www.basicmusictheory.com/c-dominant-7th-chord
                put(new String[]{F3, GA3, B3, CD4}, CD7);//https://www.basicmusictheory.com/c-sharp-dominant-7th-chord
                put(new String[]{FG3, A3, C4, D4}, D7);//https://www.basicmusictheory.com/d-dominant-7th-chord
                put(new String[]{G3, AB3, CD4, DE4}, DE7);//https://www.basicmusictheory.com/d-sharp-dominant-7th-chord
                put(new String[]{E3, GA3, B3, D4}, E7);//https://www.basicmusictheory.com/e-dominant-7th-chord
                put(new String[]{A3, C4, DE4, F4}, F7);//https://www.basicmusictheory.com/f-dominant-7th-chord
                put(new String[]{E3, FG3, AB3, CD4}, FG7);//https://www.basicmusictheory.com/f-sharp-dominant-7th-chord
                put(new String[]{F3, G3, B3, D4}, G7);//https://www.basicmusictheory.com/g-dominant-7th-chord
                put(new String[]{DE3, FG3, GA3, C4}, GA7);//https://www.basicmusictheory.com/g-sharp-dominant-7th-chord
                put(new String[]{E3, G3, A3, CD4}, A7);//https://www.basicmusictheory.com/a-dominant-7th-chord
                put(new String[]{D3, F3, GA3, AB3}, AB7);//https://www.basicmusictheory.com/a-sharp-dominant-7th-chord
                put(new String[]{FG3, A3, B3, DE4}, B7);//https://www.basicmusictheory.com/b-dominant-7th-chord

                //7th-chord: some rotations
                put(new String[]{AB3, C4, E4}, C7);
                put(new String[]{GA3, CD4, F4}, CD7);
                put(new String[]{C4, D4, FG4}, D7);
                put(new String[]{D3, FG3, A3, C4}, D7);
                put(new String[]{G3, CD4, DE4}, DE7);
                put(new String[]{GA3, D4, E4}, E7);
                put(new String[]{A3, DE4, F4}, F7);
                put(new String[]{A3, C4, DE4}, F7);
                put(new String[]{G3, B3, F4}, G7);
                put(new String[]{B3, F4, G4}, G7);
                put(new String[]{G3, A3, CD4}, A7);
                put(new String[]{A3, CD4, G4}, A7);
                put(new String[]{GA3, AB3, D4}, AB7);
                put(new String[]{A3, B3, DE4}, B7);
//                put(new String[]{AB3, C4, G4}, C7);
//                put(new String[]{GA3, B3, F4}, CD7);
//                put(new String[]{A3, C4, FG4}, D7);
//                put(new String[]{AB3, CD4, G4}, DE7);
//                put(new String[]{GA3, B3, E4}, E7);//E!
//                put(new String[]{C3, DE3, A3}, F7);//C4->C3
//                put(new String[]{FG3, AB3, E4}, FG7);
//                put(new String[]{G3, B3, F4}, G7);
//                put(new String[]{FG3, GA3, DE4}, GA7);
//                put(new String[]{G3, A3, E4}, A7);
//                put(new String[]{F3, GA3, D4}, AB7);
//                put(new String[]{A3, B3, FG4}, B7);

                //dim
                put(new String[]{DE3, A3, C4}, Ad);
//                put(new String[]{B3, D4, F4}, Dd);//https://www.basicmusictheory.com/d-diminished-7th-chord
//                put(new String[]{G3, AB3, E4}, Gd);
            }}
    );

    /**
     * Order does matter. This map comes _after CHORDS_MAP_INTERNAL map.
     */
    public static final Map<String, String> CHORDS_MAP = Collections.unmodifiableMap(initChordsMap());

    public static void main(String[] args) {
        System.out.println(CHORDS_MAP.size());
    }

    public static Map<String, String> initChordsMap() {
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String[], String> entry : CHORDS_MAP_INTERNAL.entrySet()) {
            putCheck(map, String.join("_", entry.getKey()), entry.getValue());
        }
        return map;
    }

    private static void putCheck(Map<String, String> map, String key, String value) {
        if (value == null || key == null) {
            throw new RuntimeException("Key/value cannot be null.");
        }
        if (map.containsKey(key)) {
            throw new RuntimeException("Chords map cannot contain duplicates: " + key + "; new Value: " + value);
        }
        map.put(key, value);
    }
}
