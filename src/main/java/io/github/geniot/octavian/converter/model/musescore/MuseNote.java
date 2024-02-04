package io.github.geniot.octavian.converter.model.musescore;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.github.geniot.octavian.converter.model.Constants;
import io.github.geniot.octavian.converter.model.Note;
import io.github.geniot.octavian.converter.model.NoteType;
import io.github.geniot.octavian.converter.model.SvgNote;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MuseNote implements Comparable<MuseNote>, Serializable {
    @JacksonXmlProperty(localName = "pitch")
    int pitch;
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "Spanner")
    List<MuseSpanner> spanners;
    @JacksonXmlProperty(localName = "Fingering")
    Fingering fingering;

    Long tickOn;
    Long tickOff;
    SvgNote svgNote;

    int measure;

    /**
     * In rare cases we have a tied note
     *
     * @return
     */
    public boolean isAudible() {
        if (isTie() || !isDurable()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isTie() {
        if (spanners != null) {
            for (MuseSpanner museSpanner : spanners) {
                if (museSpanner.type != null && museSpanner.type.equals("Tie") && museSpanner.prev != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * durable here means that it has duration, in a rare case there is a silent midi note (same tickOn/tickOff)
     * it happens when the same note is played in another voice, musescore creates it to follow the graphical
     * notation
     *
     * @return
     */
    public boolean isDurable() {
        if (tickOn != null && tickOff != null && Math.abs(tickOn - tickOff) == 0) {
            return false;
        } else {
            return true;
        }
    }

    public Note toNote(boolean isOn, NoteType noteType, String fingeringLayoutName) {
        Note note = new Note();
        note.setNoteValue(pitch);
        if (isOn) {
            note.setDuration((int) Math.abs(tickOff - tickOn));
        }
//        if (fingering != null && fingeringLayoutName != null) {
//            Map<String, Finger> fingers = new HashMap<>();
//            fingers.put(fingeringLayoutName, new Finger(fingering.getText()));
//            note.setFingers(fingers);
//        }
        note.setNoteType(noteType);
        note.setNoteName(Constants.NOTE_NAME_MAP.get(String.valueOf(pitch)));
        return note;
    }

    @Override
    public int compareTo(MuseNote o) {
        if (!this.tickOn.equals(o.tickOn)) {
            return Long.compare(this.tickOn, o.tickOn);
        }
        if (this.pitch != o.pitch) {
            return Integer.compare(o.pitch, this.pitch);
        }
        if (!this.tickOff.equals(o.tickOff)) {
            return Long.compare(this.tickOff, o.tickOff);
        }
        return Integer.compare(this.hashCode(), o.hashCode());
        //throw new RuntimeException("MuseNotes cannot have the same tickOn, tickOff and pitch.");
    }
}
