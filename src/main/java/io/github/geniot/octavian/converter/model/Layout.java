package io.github.geniot.octavian.converter.model;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class Layout {

    private String name;
    private List<String> lines;

    public final static String C_GRIFF_EUROPE = "c_griff_europe";
    public final static String C_GRIFF_2 = "c_griff_2";
    public final static String B_GRIFF_BAJAN = "b_griff_bajan";
    public final static String C_GRIFF_FINN = "c_griff_finn";
    public final static String D_GRIFF_1 = "d_griff_1";
    public final static String D_GRIFF_2 = "d_griff_2";

    public final static String _2_BS_ROWS = "2_bs_rows";
    public final static String _3_BS_ROWS_A_7TH = "3_bs_rows_a_7th";
    public final static String _3_BS_ROWS_A_5_DIM = "3_bs_rows_a_5_d";
    public final static String _3_BS_ROWS_B_7TH = "3_bs_rows_b_7th";
    public final static String _3_BS_ROWS_B_5_DIM = "3_bs_rows_b_5_d";
    public final static String _3_BS_ROWS_BX_7TH_A = "3_bs_rows_bx_7th_a";
    public final static String _3_BS_ROWS_BX_7TH_B = "3_bs_rows_bx_7th_b";
    public final static String _3_BS_ROWS_BELGIUM = "3_bs_rows_belgium";

    public final static String PIANO_RIGHT_HAND = "piano_right_hand";
    public final static String PIANO_LEFT_HAND = "piano_left_hand";

    public final static Set<String> RIGHT_HAND_LAYOUTS = ImmutableSet.of(
            C_GRIFF_EUROPE,
            C_GRIFF_2,
            B_GRIFF_BAJAN,
            C_GRIFF_FINN,
            D_GRIFF_1,
            D_GRIFF_2,
            PIANO_RIGHT_HAND
    );

    public final static Set<String> LEFT_HAND_LAYOUTS = ImmutableSet.of(
            _2_BS_ROWS,
            _3_BS_ROWS_A_7TH,
            _3_BS_ROWS_A_5_DIM,
            _3_BS_ROWS_B_7TH,
            _3_BS_ROWS_B_5_DIM,
            _3_BS_ROWS_BX_7TH_A,
            _3_BS_ROWS_BX_7TH_B,
            _3_BS_ROWS_BELGIUM,
            PIANO_LEFT_HAND
    );

    public final static Set<String> ALL = ImmutableSet.<String>builder()
            .addAll(RIGHT_HAND_LAYOUTS)
            .addAll(LEFT_HAND_LAYOUTS)
            .build();

    public static Set<NoteType> handByLayout(String layoutName) {
        Set<NoteType> set = new HashSet<>();
        if (RIGHT_HAND_LAYOUTS.contains(layoutName)) {
            set.add(NoteType.RIGHT_HAND_NOTE);
        } else {
            set.add(NoteType.LEFT_HAND_BASS);
            set.add(NoteType.LEFT_HAND_CHORD);
        }
        return set;
    }
}
