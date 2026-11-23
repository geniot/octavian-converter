package io.github.geniot.octavian.converter;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Constants {
    public static final List<String> NOTES = Arrays.asList("C", "CD", "D", "DE", "E", "F", "FG", "G", "GA", "A", "AB", "B");
    public static final Map<Integer, String> NOTES_MAP =
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

    public static final Map<String, Chord> CHORD_VALUES_MAP =
            Collections.unmodifiableMap(new HashMap<>() {{
                try {
                    try (InputStream inputStream = Constants.class.getClassLoader().getResourceAsStream("chords.musicxml")) {
                        String xml = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        MusicXmlHandler musicXmlHandler = new MusicXmlHandler(xml);
                        this.putAll(musicXmlHandler.getChords());
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }});

    public static final Map<Chord, String> CHORDS_MAP =
            Map.ofEntries(
                    Map.entry(Chord.C, "G3 C4 E4"),
                    Map.entry(Chord.Cm, "C3 DE3 G3"),
                    Map.entry(Chord.C7, "C3 E3 G3 AB3"),
                    Map.entry(Chord.Cd, "C3 DE3 FG3 A3"),
                    Map.entry(Chord.CD, "CD3 F3 GA3"),
                    Map.entry(Chord.CDm, "CD3 E3 GA3"),
                    Map.entry(Chord.CD7, "CD3 F3 GA3 B3"),
                    Map.entry(Chord.CDd, "CD3 E3 G3 AB3"),
                    Map.entry(Chord.D, "D3 FG3 A3"),
                    Map.entry(Chord.Dm, "F3 A3 D4"),
                    Map.entry(Chord.D7, "D2 C3 FG3 A3"),
                    Map.entry(Chord.Dd, "D3 F3 GA3 B3"),
                    Map.entry(Chord.DE, "DE3 G3 AB3"),
                    Map.entry(Chord.DE7, "DE2 CD3 G3 AB3"),
                    Map.entry(Chord.DEd, "DE2 C3 FG3 A3"),
                    Map.entry(Chord.DEm, "DE3 FG3 AB3"),
                    Map.entry(Chord.E, "E3 GA3 B3"),
                    Map.entry(Chord.Em, "E3 G3 B3"),
                    Map.entry(Chord.E7, "GA3 D4 E4"),
                    Map.entry(Chord.Ed, "E2 CD3 G3 AB3"),
                    Map.entry(Chord.F, "F3 A3 C4"),
                    Map.entry(Chord.Fm, "F2 C3 GA3"),
                    Map.entry(Chord.F7, "F2 C3 DE3 A3"),
                    Map.entry(Chord.Fd, "F2 D3 GA3 B3"),
                    Map.entry(Chord.FG, "FG2 CD3 AB3"),
                    Map.entry(Chord.FG7, "FG2 CD3 E3 AB3"),
                    Map.entry(Chord.FGm, "FG2 CD3 A3"),
                    Map.entry(Chord.FGd, "FG2 C3 DE3 A3"),
                    Map.entry(Chord.G, "G2 D3 B3"),
                    Map.entry(Chord.Gm, "G2 D3 AB3"),
                    Map.entry(Chord.G7, "G2 D3 F3 B3"),
                    Map.entry(Chord.Gd, "G2 CD3 E3 AB3"),
                    Map.entry(Chord.GA, "GA2 C3 DE3"),
                    Map.entry(Chord.GA7, "GA2 C3 DE3 FG3"),
                    Map.entry(Chord.GAm, "GA2 DE3 B3"),
                    Map.entry(Chord.GAd, "GA2 D3 F3 B3"),
                    Map.entry(Chord.A, "A2 CD3 E3"),
                    Map.entry(Chord.Am, "A3 C4 E4"),
                    Map.entry(Chord.A7, "A2 CD3 E3 G3"),
                    Map.entry(Chord.Ad, "A2 C3 DE3 FG3"),
                    Map.entry(Chord.AB, "AB2 D3 F3"),
                    Map.entry(Chord.ABm, "AB2 CD3 F3"),
                    Map.entry(Chord.AB7, "AB2 D3 F3 GA3"),
                    Map.entry(Chord.ABd, "AB2 CD3 E3 G3"),
                    Map.entry(Chord.B, "B2 DE3 FG3"),
                    Map.entry(Chord.B7, "B2 DE3 A3 FG3"),
                    Map.entry(Chord.Bm, "B2 D3 FG3"),
                    Map.entry(Chord.Bd, "B2 D3 F3 GA3")
            );
}