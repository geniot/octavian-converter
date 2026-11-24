package io.github.geniot.octavian.converter;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MusicXmlHandler extends AbstractHandler {

    public MusicXmlHandler(String xml, Hand hand) throws Exception {
        super(xml);
        if (hand.equals(Hand.BOTH)) {
            this.fix();
        } else if (hand.equals(Hand.LEFT)) {
            removeByXPath("//note[staff[text()='2']]", document);
        } else if (hand.equals(Hand.RIGHT)) {
            removeByXPath("//note[staff[text()='1']]", document);
        }
    }

    private void fix() throws Exception {
        //add id to each note, prefix rehearsal starting notes with 'reh-', suffix '-rendN' will be added later by verovio when necessary
        NodeList notesList = (NodeList) xpath.compile(".//note").evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < notesList.getLength(); i++) {
            Node n = notesList.item(i);
            String prefix = isRehearsalMarkStart(n) ? "reh-" : "";
            int suffix = getNumericNoteValue(n);
            int staff = Integer.parseInt(getByXPath(".//staff/text()", n));
            Node idNode = document.createAttribute("id");
            idNode.setNodeValue(prefix + "id" + (i + 1) + "-" + suffix + "-" + staff);
            n.getAttributes().setNamedItem(idNode);
        }
        removeByXPath("//lyric", document);
    }

    private int getNumericNoteValue(Node n) throws Exception {
        boolean isRest = getByXPath(".//rest", n) != null;
        if (isRest) {
            return 0;
        } else {
            String step = getByXPath(".//pitch/step/text()", n);
            String alterStr = getByXPath(".//pitch/alter/text()", n);
            String octaveStr = getByXPath(".//pitch/octave/text()", n);
            int octave = Integer.parseInt(octaveStr) + 1;
            int alter = alterStr == null ? 0 : Integer.parseInt(alterStr);
            return octave * Constants.NOTES.size() + Constants.NOTES.indexOf(step) + alter;
        }
    }

    private boolean isRehearsalMarkStart(Node prev) throws Exception {
        prev = prev.getPreviousSibling();
        while (prev != null && !prev.getNodeName().equals("note")) {
            if (prev.getNodeType() == Node.ELEMENT_NODE) {
                NodeList searchResultsList = (NodeList) xpath.compile(".//rehearsal").evaluate(prev, XPathConstants.NODESET);
                if (searchResultsList.getLength() > 0) {
                    return true;
                }
            }
            prev = prev.getPreviousSibling();
        }
        return false;
    }

    public String[] getTitleAndAuthor() throws Exception {
        List<String> titleAndAuthor = new ArrayList<>();
        NodeList searchResultsList = (NodeList) xpath.compile("//credit-words").evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < searchResultsList.getLength(); i++) {
            Node n = searchResultsList.item(i);
            titleAndAuthor.add(n.getTextContent());
        }
        if (titleAndAuthor.size() != 2) {
            log.error("Title and Author are not correct");
        }
        return titleAndAuthor.toArray(new String[0]);
    }

    public Instrument getInstrument() throws Exception {
        NodeList searchResultsList = (NodeList) xpath.compile("//instrument-name/text()").evaluate(document, XPathConstants.NODESET);
        if (searchResultsList.getLength() > 0) {
            Node instrumentNode = searchResultsList.item(0);
            if (instrumentNode.getTextContent().toLowerCase().contains("accordion")) {
                return Instrument.Accordion;
            } else {
                return Instrument.Piano;
            }
        }
        return Instrument.Piano;
    }

    public Map<String, Chord> getChords() throws Exception {
        Map<String, Chord> result = new HashMap<>();
        NodeList measuresResultsList = (NodeList) xpath.compile("//measure").evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < measuresResultsList.getLength(); i++) {
            Node measureNode = measuresResultsList.item(i);
            String chordName = (String) xpath.compile(".//words/text()").evaluate(measureNode, XPathConstants.STRING);
            Chord chord = Chord.getByValue(chordName);
            if (chord == null) {
                throw new RuntimeException("Couldn't get chord by value");
            }
            NodeList notesResultsList = (NodeList) xpath.compile(".//note[staff[text()='2']]").evaluate(measureNode, XPathConstants.NODESET);
            StringBuilder stringBuilder = new StringBuilder();
            for (int n = notesResultsList.getLength() - 1; n >= 0; n--) {
                int value = this.getNumericNoteValue(notesResultsList.item(n));
                stringBuilder.append(value);
                stringBuilder.append(' ');
            }
            result.put(stringBuilder.toString().trim(), chord);
        }
        return result;
    }
}
