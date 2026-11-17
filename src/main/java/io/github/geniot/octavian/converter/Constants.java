package io.github.geniot.octavian.converter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final String[] NOTES = new String[]{"C", "CD", "D", "DE", "E", "F", "FG", "G", "GA", "A", "AB", "B"};
    public static final Map<Integer, String> NOTE_NAME_MAP =
            Collections.unmodifiableMap(new HashMap<>() {{
                put(21, "A0");
                put(22, "AB0");
                put(23, "B0");

                int counter = 24;
                for (int i = 1; i <= 7; i++) {
                    for (String note : NOTES) {
                        put(counter++, note + i);
                    }
                }
                put(108, "C8");
            }});

    public static final Map<String, String> NOTE_NAME_CHORD_MAP =
            Map.ofEntries(
                    Map.entry("C", "G3 C4 E4"),
                    Map.entry("Cm", "C3 DE3 G3"),
                    Map.entry("C7", "C3 E3 G3 AB3"),
                    Map.entry("Cd", "C3 DE3 FG3 A3"),
                    Map.entry("CD", "CD3 F3 GA3"),
                    Map.entry("CDm", "CD3 E3 GA3"),
                    Map.entry("CD7", "CD3 F3 GA3 B3"),
                    Map.entry("CDd", "CD3 E3 G3 AB3"),
                    Map.entry("D", "D3 FG3 A3"),
                    Map.entry("Dm", "F3 A3 D4"),
                    Map.entry("D7", "D2 C3 FG3 A3"),
                    Map.entry("Dd", "D3 F3 GA3 B3"),
                    Map.entry("DE", "DE3 G3 AB3"),
                    Map.entry("DE7", "DE2 CD3 G3 AB3"),
                    Map.entry("DEd", "DE2 C3 FG3 A3"),
                    Map.entry("DEm", "DE3 FG3 AB3"),
                    Map.entry("E", "E3 GA3 B3"),
                    Map.entry("Em", "E3 G3 B3"),
                    Map.entry("E7", "GA3 D4 E4"),
                    Map.entry("Ed", "E2 CD3 G3 AB3"),
                    Map.entry("F", "F3 A3 C4"),
                    Map.entry("Fm", "F2 C3 GA3"),
                    Map.entry("F7", "F2 C3 DE3 A3"),
                    Map.entry("Fd", "F2 D3 GA3 B3"),
                    Map.entry("FG", "FG2 CD3 AB3"),
                    Map.entry("FG7", "FG2 CD3 E3 AB3"),
                    Map.entry("FGm", "FG2 CD3 A3"),
                    Map.entry("FGd", "FG2 C3 DE3 A3"),
                    Map.entry("G", "G2 D3 B3"),
                    Map.entry("Gm", "G2 D3 AB3"),
                    Map.entry("G7", "G2 D3 F3 B3"),
                    Map.entry("Gd", "G2 CD3 E3 AB3"),
                    Map.entry("GA", "GA2 C3 DE3"),
                    Map.entry("GA7", "GA2 C3 DE3 FG3"),
                    Map.entry("GAm", "GA2 DE3 B3"),
                    Map.entry("GAd", "GA2 D3 F3 B3"),
                    Map.entry("A", "A2 CD3 E3"),
                    Map.entry("Am", "A3 C4 E4"),
                    Map.entry("A7", "A2 CD3 E3 G3"),
                    Map.entry("Ad", "A2 C3 DE3 FG3"),
                    Map.entry("AB", "AB2 D3 F3"),
                    Map.entry("ABm", "AB2 CD3 F3"),
                    Map.entry("AB7", "AB2 D3 F3 GA3"),
                    Map.entry("ABd", "AB2 CD3 E3 G3"),
                    Map.entry("B", "B2 DE3 FG3"),
                    Map.entry("B7", "B2 DE3 A3 FG3"),
                    Map.entry("Bm", "B2 D3 FG3"),
                    Map.entry("Bd", "B2 D3 F3 GA3")
            );
}