package io.github.geniot.octavian.converter;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import java.util.ArrayList;
import java.util.List;

public class MusicXmlHandler extends AbstractHandler {

    public MusicXmlHandler(String xml) throws Exception {
        super(xml);
        this.fix();
    }

    void fix() throws Exception {
        //add id to each note, prefix rehearsal starting notes with 'reh-', suffix '-rendN' will be added later by verovio when necessary
        NodeList searchResultsList = (NodeList) xpath.compile("//note").evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < searchResultsList.getLength(); i++) {
            Node n = searchResultsList.item(i);
            String prefix = isRehearsalMarkStart(n) ? "reh-" : "";
            Node idNode = document.createAttribute("id");
            idNode.setNodeValue(prefix + "id" + (i + 1));
            n.getAttributes().setNamedItem(idNode);
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
            logger.error("Title and Author are not correct");
        }
        return titleAndAuthor.toArray(new String[0]);
    }
}
