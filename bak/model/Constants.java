package io.github.geniot.octavian.converter.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final byte SET_TEMPO_META_TYPE = 0x51;
    public static final Map<String, String> NOTE_NAME_MAP =
            Collections.unmodifiableMap(new HashMap<>() {{
                put("21", "A0");
                put("22", "AB0");
                put("23", "B0");
                put("24", "C1");
                put("25", "CD1");
                put("26", "D1");
                put("27", "DE1");
                put("28", "E1");
                put("29", "F1");
                put("30", "FG1");
                put("31", "G1");
                put("32", "GA1");
                put("33", "A1");
                put("34", "AB1");
                put("35", "B1");
                put("36", "C2");
                put("37", "CD2");
                put("38", "D2");
                put("39", "DE2");
                put("40", "E2");
                put("41", "F2");
                put("42", "FG2");
                put("43", "G2");
                put("44", "GA2");
                put("45", "A2");
                put("46", "AB2");
                put("47", "B2");
                put("48", "C3");
                put("49", "CD3");
                put("50", "D3");
                put("51", "DE3");
                put("52", "E3");
                put("53", "F3");
                put("54", "FG3");
                put("55", "G3");
                put("56", "GA3");
                put("57", "A3");
                put("58", "AB3");
                put("59", "B3");
                put("60", "C4");
                put("61", "CD4");
                put("62", "D4");
                put("63", "DE4");
                put("64", "E4");
                put("65", "F4");
                put("66", "FG4");
                put("67", "G4");
                put("68", "GA4");
                put("69", "A4");
                put("70", "AB4");
                put("71", "B4");
                put("72", "C5");
                put("73", "CD5");
                put("74", "D5");
                put("75", "DE5");
                put("76", "E5");
                put("77", "F5");
                put("78", "FG5");
                put("79", "G5");
                put("80", "GA5");
                put("81", "A5");
                put("82", "AB5");
                put("83", "B5");
                put("84", "C6");
                put("85", "CD6");
                put("86", "D6");
                put("87", "DE6");
                put("88", "E6");
                put("89", "F6");
                put("90", "FG6");
                put("91", "G6");
                put("92", "GA6");
                put("93", "A6");
                put("94", "AB6");
                put("95", "B6");
                put("96", "C7");
                put("97", "CD7");
                put("98", "D7");
                put("99", "DE7");
                put("100", "E7");
                put("101", "F7");
                put("102", "FG7");
                put("103", "G7");
                put("104", "GA7");
                put("105", "A7");
                put("106", "AB7");
                put("107", "B7");
                put("108", "C8");
                //chords
                put("A", "A2 CD3 E3");
                put("Am", "A3 C4 E4");
                put("A7", "A2 CD3 E3 G3");//E3 - 3
                put("Ad", "A2 C3 DE3 FG3");

                put("AB", "AB2 D3 F3");
                put("ABm", "AB2 CD3 F3");
                put("AB7", "AB2 D3 F3 GA3");
                put("ABd", "AB2 CD3 E3 G3");

                put("C", "G3 C4 E4");
                put("Cm", "C3 DE3 G3");
                put("C7", "C3 E3 G3 AB3");
                put("Cd", "C3 DE3 FG3 A3");

                put("B", "B2 DE3 FG3");
                put("B7", "B2 DE3 A3 FG3");
                put("Bm", "B2 D3 FG3");
                put("Bd", "B2 D3 F3 GA3");

                put("CD", "CD3 F3 GA3");
                put("CDm", "CD3 E3 GA3");
                put("CD7", "CD3 F3 GA3 B3");
                put("CDd", "CD3 E3 G3 AB3");

                put("D", "D3 FG3 A3");
                put("Dm", "F3 A3 D4");
                put("D7", "D2 C3 FG3 A3");
                put("Dd", "D3 F3 GA3 B3");

                put("DE", "DE3 G3 AB3");
                put("DE7", "DE2 CD3 G3 AB3");
                put("DEd", "DE2 C3 FG3 A3");
                put("DEm", "DE3 FG3 AB3");

                put("E", "E3 GA3 B3");
                put("Em", "E3 G3 B3");
                put("E7", "GA3 D4 E4");
                put("Ed", "E2 CD3 G3 AB3");

                put("F", "F3 A3 C4");
                put("Fm", "F2 C3 GA3");
                put("F7", "F2 C3 DE3 A3");
                put("Fd", "F2 D3 GA3 B3");

                put("FG", "FG2 CD3 AB3");
                put("FG7", "FG2 CD3 E3 AB3");
                put("FGm", "FG2 CD3 A3");
                put("FGd", "FG2 C3 DE3 A3");

                put("G", "G2 D3 B3");
                put("Gm", "G2 D3 AB3");
                put("G7", "G2 D3 F3 B3");
                put("Gd", "G2 CD3 E3 AB3");

                put("GA", "GA2 C3 DE3");
                put("GA7", "GA2 C3 DE3 FG3");
                put("GAm", "GA2 DE3 B3");
                put("GAd", "GA2 D3 F3 B3");
            }});

    public static final Map<String, String> NOTE_NAME_CHORD_MAP =
            Collections.unmodifiableMap(new HashMap<String, String>() {{
                //chords
                put("C", "48,52,55");
                put("Cm", "48,51,55");
                put("C7", "48,52,55,58");
                put("Cd", "48,51,54,57");

                put("CD", "49,53,56");
                put("CDm", "49,52,56");
                put("CD7", "49,53,56,59");
                put("CDd", "49,52,55,58");

                put("D", "50,54,57");
                put("Dm", "50,53,57");
                put("D7", "38,48,54,57");
                put("Dd", "50,53,56,59");

                put("DE", "51,55,58");
                put("DEm", "51,54,58");
                put("DE7", "39,49,55,58");
                put("DEd", "39,48,54,57");

                put("E", "52,56,59");
                put("Em", "52,55,59");
                put("E7", "40,50,56,59");
                put("Ed", "40,49,55,58");

                put("F", "41,48,57");
                put("Fm", "41,48,56");
                put("F7", "41,48,51,57");
                put("Fd", "41,50,56,59");

                put("FG", "42,49,58");
                put("FGm", "42,49,57");
                put("FG7", "42,49,52,58");
                put("FGd", "42,48,51,57");

                put("G", "43,50,59");
                put("Gm", "43,50,58");
                put("G7", "43,50,53,59");
                put("Gd", "43,49,52,58");

                put("GA", "44,48,51");
                put("GAm", "44,51,59");
                put("GA7", "44,48,51,54");
                put("GAd", "44,50,53,59");

                put("A", "45,49,52");
                put("Am", "45,48,52");
                put("A7", "45,49,52,55");
                put("Ad", "45,48,51,54");

                put("AB", "46,50,53");
                put("ABm", "46,49,53");
                put("AB7", "46,50,53,56");
                put("ABd", "46,49,52,55");

                put("B", "47,51,54");
                put("Bm", "47,50,54");
                put("B7", "47,51,57,54");
                put("Bd", "47,50,53,56");
            }});

    public static final Map<String, Double> DURATIONS_MAP =
            Collections.unmodifiableMap(new HashMap<String, Double>() {{
                put("whole", 1000.0);
                put("half", 1000.0 / 2);
                put("quarter", 1000.0 / 4);
                put("eighth", 1000.0 / 8);
                put("16th", 1000.0 / 16);
                put("32nd", 1000.0 / 32);
                put("64th", 1000.0 / 64);
                put("128th", 1000.0 / 128);
            }});
}
