package io.github.geniot.octavian.converter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeiHandler extends AbstractXmlHandler {
    public byte[] xml2mei(byte[] xmlBbs) throws Exception {
        List<String> args = new ArrayList<>(Arrays.asList("./verovio", "-r", "data", "--expand-always", "--breaks", "none", "--output-to", "mei", "--input-from", "xml"));
        String mei = new String(runProcess(args, xmlBbs, ".xml", ".mei", "--outfile", true, true), StandardCharsets.UTF_8);
        mei = fixMei(mei);
        return mei.getBytes(StandardCharsets.UTF_8);
    }

    private String fixMei(String museXml) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(museXml.getBytes(StandardCharsets.UTF_8));
        Document document = docBuilder.parse(inputStream);
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
        return document2string(document);
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
}
