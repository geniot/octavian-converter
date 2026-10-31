package io.github.geniot.octavian.converter;

import io.github.geniot.indexedtreemap.IndexedTreeMap;
import io.github.geniot.octavian.converter.json.Note;
import io.github.geniot.octavian.converter.json.Point;
import io.github.geniot.octavian.converter.midi.MidiNote;
import io.github.geniot.octavian.converter.midi.MidiRoot;
import io.github.geniot.octavian.converter.midi.MidiTrack;

import java.util.ArrayList;
import java.util.List;

public class MidiHandler extends AbstractHandler {

    public byte[] xml2midi(byte[] xmlBbs) throws Exception {
        ArrayList<String> args = new ArrayList<>(List.of("/opt/musescore/musescore"));
        return runProcess(args, xmlBbs, ".xml", ".mid", "-o", true, true);
    }

    public byte[] midi2json(byte[] midiBbs) throws Exception {
        ArrayList<String> args = new ArrayList<>(List.of("./midi2json"));
        return runProcess(args, midiBbs, ".mid", ".json", "", false, true);
    }

    public byte[] xml2mp3(byte[] xmlBbs) throws Exception {
        ArrayList<String> args = new ArrayList<>(List.of("/opt/musescore/musescore"));
        return runProcess(args, xmlBbs, ".xml", ".mp3", "-o", true, false);
    }

    public void addPoints(IndexedTreeMap<Integer, Point> points, MidiRoot midiRoot) {
        for (MidiTrack track : midiRoot.getTracks()) {
            for (MidiNote note : track.getNotes()) {
                int duration = (int) (note.getDuration() * 1000);
                int onTime = (int) (note.getTime() * 1000);
                int offTime = onTime + duration;
                int value = note.getMidi();

                Point onPoint = points.getOrDefault(onTime, new Point());
                onPoint.setTimestamp(onTime);
                Note onNote = new Note();
                onNote.setDuration(duration);
                onNote.setNoteValue(value);
                onPoint.getNotesOn().add(onNote);

                Point offPoint = points.getOrDefault(offTime, new Point());
                offPoint.setTimestamp(offTime);
                Note offNote = new Note();
                offNote.setNoteValue(value);
                offPoint.getNotesOff().add(offNote);

                points.put(onTime, onPoint);
                points.put(offTime, offPoint);
            }
        }
    }
}
