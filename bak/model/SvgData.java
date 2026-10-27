package io.github.geniot.octavian.converter.model;

import io.github.geniot.indexedtreemap.IndexedTreeSet;
import lombok.Data;

@Data
public class SvgData {
    float width;
    float height;
    float playHeadWidth;

    IndexedTreeSet<Float> barOffsets;
    IndexedTreeSet<SvgNote> rightHandNotes;
    IndexedTreeSet<SvgNote> leftHandNotes;

}
