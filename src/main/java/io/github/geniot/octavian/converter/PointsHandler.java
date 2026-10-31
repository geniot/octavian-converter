package io.github.geniot.octavian.converter;

import io.github.geniot.indexedtreemap.IndexedTreeMap;
import io.github.geniot.octavian.converter.json.Note;
import io.github.geniot.octavian.converter.json.Point;
import io.github.geniot.octavian.converter.midi.MidiNote;
import io.github.geniot.octavian.converter.midi.MidiRoot;
import io.github.geniot.octavian.converter.midi.MidiTrack;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PointsHandler extends AbstractHandler {

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

    public void identifyNotes(String meiXmlStr, MidiRoot midiRoot) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(meiXmlStr.getBytes(StandardCharsets.UTF_8));
        Document document = docBuilder.parse(inputStream);
        for (int i = 0; i < midiRoot.getTracks().size(); i++) {
            NodeList searchResultsList = (NodeList) xpath.compile("//staff[@n='" + (i + 1) + "']//note").evaluate(document, XPathConstants.NODESET);
            for (int j = 0; j < midiRoot.getTracks().get(i).getNotes().size(); j++) {
                MidiNote midiNote = midiRoot.getTracks().get(i).getNotes().get(j);
                Node meiNode = searchResultsList.item(j);
                String meiOct = meiNode.getAttributes().getNamedItem("oct").getNodeValue();
                String meiPName = meiNode.getAttributes().getNamedItem("pname").getNodeValue();
                String meiId = meiNode.getAttributes().getNamedItem("xml:id").getNodeValue();
                String midiNoteName = simplifyNoteName(midiNote);
                if (!midiNoteName.equals(meiPName + meiOct)) {
                    throw new Exception("Invalid note name: " + midiNoteName);
                } else {
                    midiNote.setId(meiId);
                }
            }
        }
    }

    private String simplifyNoteName(MidiNote midiNote) {
        String name = midiNote.getName();
        int length = name.length();
        return (name.charAt(0) + String.valueOf(name.charAt(length - 1))).toLowerCase();
    }
}
