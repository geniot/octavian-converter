package io.github.geniot.octavian.converter;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MusicXmlHandler extends AbstractHandler {

    public MusicXmlHandler(String xml) throws Exception {
        super(xml);
        this.fix();
    }

    void fix() throws Exception {
        NodeList partsList = (NodeList) xpath.compile("//part").evaluate(document, XPathConstants.NODESET);
        for (int p = 0; p < partsList.getLength(); p++) {
            Node partNode = partsList.item(p);
            //add id to each note, prefix rehearsal starting notes with 'reh-', suffix '-rendN' will be added later by verovio when necessary
            NodeList notesList = (NodeList) xpath.compile(".//note").evaluate(partNode, XPathConstants.NODESET);
            for (int i = 0; i < notesList.getLength(); i++) {
                Node n = notesList.item(i);
                String prefix = isRehearsalMarkStart(n) ? "reh-" : "";
                int suffix = getNumericNoteValue(n);
                Node idNode = document.createAttribute("id");
                idNode.setNodeValue(prefix + "id" + (i + 1) + "-" + suffix + "-" + (p + 1));
                n.getAttributes().setNamedItem(idNode);
            }
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
}
