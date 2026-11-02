package io.github.geniot.octavian.converter;

import io.github.geniot.octavian.converter.midi.MidiNote;
import io.github.geniot.octavian.converter.midi.MidiRoot;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

public class MeiHandler extends AbstractHandler {

    public MeiHandler(String xml) throws Exception {
        super(xml);
    }

    void fix() throws Exception {
        removeByXPath("//fing", document, xpath);//fingering
        removeByXPath("//reh", document, xpath);//rehearsal marks
        removeByXPath("//pgHead/rend/text()", document, xpath);
        removeByXPath("//label/text()", document, xpath);
        removeByXPath("//labelAbbr/text()", document, xpath);
        removeByXPath("//tempo/text()", document, xpath);
        removeByXPath("//tempo/rend/text()", document, xpath);
        removeByXPath("//dir/text()", document, xpath);
        removeByXPath("//measure/mNum", document, xpath);//we will add our own measure numbers
        removeAttributeByXPath("//measure", document, xpath, "left");
        removeAttributeByXPath("//measure", document, xpath, "right");
        replaceEndingsWithSections(document, xpath);
        addRightEndToLastMeasure(document, xpath);
        addMeasureNumbers(document, xpath);
    }

    private void replaceEndingsWithSections(Document node, XPath xpath) throws Exception {
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

    private void addRightEndToLastMeasure(Document document, XPath xpath) throws Exception {
        NodeList searchResultsList = (NodeList) xpath.compile("//measure").evaluate(document, XPathConstants.NODESET);
        Node lastMeasure = searchResultsList.item(searchResultsList.getLength() - 1);
        ((Element) lastMeasure).setAttribute("right", "end");
    }

    private void addMeasureNumbers(Document document, XPath xpath) throws Exception {
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

    public void setIdsToMidiNotes(MidiRoot midiRoot) throws Exception {
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
