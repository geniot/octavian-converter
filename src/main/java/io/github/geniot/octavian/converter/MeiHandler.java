package io.github.geniot.octavian.converter;

import io.github.geniot.octavian.converter.json.*;
import io.github.geniot.octavian.converter.midi.MidiNote;
import io.github.geniot.octavian.converter.midi.MidiRoot;
import io.github.geniot.octavian.converter.midi.MidiTrack;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import java.util.*;

public class MeiHandler extends AbstractHandler {
    private Map<String, String> tiesMap;

    public MeiHandler(String xml, boolean shouldFix) throws Exception {
        super(xml);
        if (shouldFix) {
            this.fix();
        }
        this.initTiesMap();
    }

    private void initTiesMap() throws Exception {
        NodeList tiesResultsList = (NodeList) xpath.compile("//tie").evaluate(document, XPathConstants.NODESET);
        this.tiesMap = new HashMap<>();
        for (int i = 0; i < tiesResultsList.getLength(); i++) {
            Node n = tiesResultsList.item(i);
            String startId = n.getAttributes().getNamedItem("startid").getTextContent().substring(1);
            String endId = n.getAttributes().getNamedItem("endid").getTextContent().substring(1);
            this.tiesMap.put(startId, endId);
        }
    }

    private void fix() throws Exception {
        removeByXPath("//fing", document);//fingering
        removeByXPath("//reh", document);//rehearsal marks
        removeByXPath("//pgHead/rend/text()", document);
        removeByXPath("//label/text()", document);
        removeByXPath("//labelAbbr/text()", document);
        removeByXPath("//tempo/text()", document);
        removeByXPath("//tempo/rend/text()", document);
        removeByXPath("//dir/text()", document);
        removeByXPath("//measure/mNum", document);//we will add our own measure numbers
        removeAttributeByXPath("//measure", document, "left");
        removeAttributeByXPath("//measure", document, "right");
        replaceEndingsWithSections(document);
        addRightEndToLastMeasure(document);
        addMeasureNumbers(document);
    }

    public void setFingeringToTune(Tune tune) throws Exception {
        NodeList searchResultsList = (NodeList) xpath.compile("//fing").evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < searchResultsList.getLength(); i++) {
            Node n = searchResultsList.item(i);
            String id = n.getAttributes().getNamedItem("startid").getTextContent().substring(1);
            String finger = n.getTextContent().trim();
            Note onNote = findNoteById(tune, id);
            Map<HandType, Finger> map = new HashMap<>();
            map.put(onNote.getNoteType().equals(NoteType.RIGHT_HAND_NOTE) ? HandType.PIANO_RIGHT_HAND : HandType.PIANO_LEFT_HAND, new Finger(finger, null));
            onNote.setFingers(map);
        }
    }

    private Note findNoteById(Tune tune, String noteId) throws Exception {
        for (Point point : tune.getPoints()) {
            for (Note note : point.getNotesOn()) {
                if (note.getId().equals(noteId)) {
                    return note;
                }
            }
        }
        throw new Exception("Could not find note for note id: " + noteId);
    }

    private void replaceEndingsWithSections(Document node) throws Exception {
        NodeList searchResultsList = (NodeList) xpath.compile("//ending").evaluate(node, XPathConstants.NODESET);
        for (int i = 0; i < searchResultsList.getLength(); i++) {
            Node n = searchResultsList.item(i);

            Element newSection = node.createElement("section");
            newSection.setAttribute("xml:id", n.getAttributes().getNamedItem("xml:id").getNodeValue());
            NodeList children = n.getChildNodes();
            Node[] childrenArray = new Node[children.getLength()];
            for (int j = 0; j < children.getLength(); j++) {
                childrenArray[j] = children.item(j);
            }
            for (Node child : childrenArray) {
                newSection.appendChild(child);
            }
            Node previousSectionOrMeasure = getPreviousSectionOrMeasure(n);
            previousSectionOrMeasure.appendChild(newSection);
            n.getParentNode().removeChild(n);
        }
    }

    private void addRightEndToLastMeasure(Node node) throws Exception {
        NodeList searchResultsList = (NodeList) xpath.compile("//measure").evaluate(node, XPathConstants.NODESET);
        Node lastMeasure = searchResultsList.item(searchResultsList.getLength() - 1);
        ((Element) lastMeasure).setAttribute("right", "end");
    }

    private void addMeasureNumbers(Document document) throws Exception {
        NodeList searchResultsList = (NodeList) xpath.compile("//measure").evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < searchResultsList.getLength(); i++) {
            Node n = searchResultsList.item(i);
            Node firstChild = n.getFirstChild();
            Element pageChild = document.createElement("mNum");
            pageChild.setTextContent(String.valueOf(i + 1));
            n.insertBefore(pageChild, firstChild);
        }
    }

    private Node getPreviousSectionOrMeasure(Node n) {
        Node prev = n.getPreviousSibling();
        while (prev != null &&
                !prev.getNodeName().equalsIgnoreCase("section") &&
                !prev.getNodeName().equalsIgnoreCase("measure")) {
            prev = prev.getPreviousSibling();
        }
        return prev;
    }

    public void setIdsToMidiNotes(MidiRoot midiRoot,
                                  SortedMap<Integer, Set<String>> noteOffsets) throws Exception {
        for (int i = 0; i < midiRoot.getTracks().size(); i++) {
            MidiTrack midiTrack = midiRoot.getTracks().get(i);
            if (midiTrack.getNotes().isEmpty()) {
                continue;
            }
            SortedMap<Integer, Set<MidiNote>> midiMap = genMap(midiTrack);

            Iterator<Map.Entry<Integer, Set<String>>> svgIterator = noteOffsets.entrySet().iterator();
            Map.Entry<Integer, Set<String>> svgEntry = svgIterator.next();
            for (Map.Entry<Integer, Set<MidiNote>> midiEntry : midiMap.entrySet()) {
                for (MidiNote midiNote : midiEntry.getValue()) {
                    String id = findIdByValue(svgEntry, midiNote.getMidi());
                    midiNote.setId(id);
//                    System.out.println("Set id to midi note: " + id + ":" + midiNote.getName());
                }
                svgEntry = svgIterator.hasNext() ? svgIterator.next() : svgEntry;
                boolean isSilent = isSilent(svgEntry);
                while (isSilent) {
                    if (svgIterator.hasNext()) {
                        svgEntry = svgIterator.next();
                        isSilent = isSilent(svgEntry);
                    } else {
                        break;
                    }
                }
            }
        }
    }

    private boolean isSilent(Map.Entry<Integer, Set<String>> svgEntry) {
        for (String id : svgEntry.getValue()) {
            if (this.tiesMap.containsValue(id)) {
//                if (svgEntry.getValue().size() > 1) {
//                    System.out.println("More than one note found for silent check at "+svgEntry.getKey());
//                }
                return true;
            }
        }
        return false;
    }

    private String findIdByValue(Map.Entry<Integer, Set<String>> entry, int value) throws Exception {
        for (String id : entry.getValue()) {
            String[] splits = id.split("-");
            String lastToken = splits[splits.length - 1];
            int offset = lastToken.startsWith("rend") ? 3 : 2;
            if (splits[splits.length - offset].equals(String.valueOf(value))) {
//                System.out.println(id + ":" + Constants.getNoteNameByValue(value));
                return id;
            }
        }
        throw new Exception("Couldn't find id by value: " + value + ":" + Constants.getNoteNameByValue(value) +
                " at " + entry.getKey() +
                " available ids: " + entry.getValue());
    }

    private SortedMap<Integer, Set<MidiNote>> genMap(MidiTrack midiTrack) {
        SortedMap<Integer, Set<MidiNote>> result = new TreeMap<>();
        for (int j = 0; j < midiTrack.getNotes().size(); j++) {
            MidiNote midiNote = midiTrack.getNotes().get(j);
            Set<MidiNote> notes = result.getOrDefault(midiNote.getTicks(), new HashSet<>());
            notes.add(midiNote);
            result.put(midiNote.getTicks(), notes);
        }
        return result;
    }
}
