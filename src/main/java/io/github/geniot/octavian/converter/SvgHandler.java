package io.github.geniot.octavian.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import java.util.*;

public class SvgHandler extends AbstractHandler {
    static Logger logger = LoggerFactory.getLogger(SvgHandler.class);

    public SvgHandler(String xml) throws Exception {
        super(xml);
        this.fix();
    }

    public Map<String, Integer> geNoteOffsets() throws Exception {
        long shift = getShiftX();
        Map<String, Integer> svgNoteOffsets = new HashMap<>();
        NodeList searchResultsList = (NodeList) xpath.compile("//g[@class='note bounding-box']/rect").evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < searchResultsList.getLength(); i++) {
            Node n = searchResultsList.item(i);
            String idContent = n.getParentNode().getAttributes().getNamedItem("id").getTextContent();
            String id = idContent.substring(idContent.indexOf('-') + 1);//removing the prefix 'bbox-'
            int x = Integer.parseInt(n.getAttributes().getNamedItem("x").getNodeValue());

            Node possibleChordParent = n.getParentNode().getParentNode().getParentNode();
            if (possibleChordParent.getAttributes().getNamedItem("class").getNodeValue().equals("chord")) {
                x = getChordOffset(possibleChordParent);
            }
            if (svgNoteOffsets.containsKey(id)) {
                throw new RuntimeException("Note id " + id + " already exists");
            }
            svgNoteOffsets.put(id, scale(x, shift));
        }
        return svgNoteOffsets;
    }

    /**
     * Some chords have notes that are visually not aligned vertically on the same y-axis.
     * Although these chord notes sound at the same time. This brings confusion for Octavian.
     * To fix this we set the x-offset for all chord notes to the same value: where most of them stick to.
     *
     * @param chordNode
     * @return
     */
    private int getChordOffset(Node chordNode) throws Exception {
        NodeList rects = (NodeList) xpath.compile(".//g[@class='note bounding-box']/rect")
                .evaluate(chordNode, XPathConstants.NODESET);

        Map<Integer, Integer> offset2count = new TreeMap<>();
        for (int i = 0; i < rects.getLength(); i++) {
            int x = Integer.parseInt(rects.item(i).getAttributes().getNamedItem("x").getNodeValue());
            offset2count.merge(x, 1, Integer::sum);
        }

        return Collections.max(offset2count.entrySet(), Map.Entry.comparingByValue()).getKey();
    }


    public SortedSet<Integer> getSelectionRanges() throws Exception {
        long shift = getShiftX();
        SortedSet<Integer> svgRehearsalNoteOffsets = new TreeSet<>();
        NodeList searchResultsList = (NodeList) xpath.compile("//g[@class='note' and starts-with(@id,'reh-')]/g[@class='note bounding-box']/rect").evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < searchResultsList.getLength(); i++) {
            Node n = searchResultsList.item(i);
            int value = Integer.parseInt(n.getAttributes().getNamedItem("x").getNodeValue());
            svgRehearsalNoteOffsets.add(scale(value, shift));
        }
        return svgRehearsalNoteOffsets;
    }

    public SortedSet<Integer> getBarOffsets() throws Exception {
        long shift = getShiftX();
        SortedSet<Integer> svgBarOffsets = new TreeSet<>();
        NodeList searchResultsList = (NodeList) xpath.compile("//g[@class='barLine bounding-box']/rect").evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < searchResultsList.getLength(); i++) {
            Node n = searchResultsList.item(i);
            int value = Integer.parseInt(n.getAttributes().getNamedItem("x").getNodeValue());
            svgBarOffsets.add(scale(value, shift));
        }
        svgBarOffsets.removeLast();//octavian does not need it
        //insert first
        Node pageMarginNode = (Node) xpath.compile("//g[@class='system']/path").evaluate(document, XPathConstants.NODE);
        String d = pageMarginNode.getAttributes().getNamedItem("d").getNodeValue();
        //for d="M13 540 L13 3060" parse "13"
        int value = Integer.parseInt(d.substring(0, d.indexOf(' ')).substring(1));
        svgBarOffsets.add(scale(value, shift));

        return svgBarOffsets;
    }

    private int scale(int offset, long shift) throws Exception {
        return (int) ((offset + shift) * 0.1);
    }

    /**
     * transform='translate(260, 0)'
     *
     * @return 260
     * @throws Exception
     */
    private long getShiftX() throws Exception {
        Node pageMarginNode = (Node) xpath.compile("//g[@class='page-margin']").evaluate(document, XPathConstants.NODE);
        String translate = pageMarginNode.getAttributes().getNamedItem("transform").getNodeValue();
        return Integer.parseInt(translate.split("\\(")[1].split(",")[0]);
    }

    public void fix() throws Exception {
        Node styleNode = (Node) xpath.compile("//style").evaluate(document, XPathConstants.NODE);
        String styles = styleNode.getTextContent();
        styleNode.setTextContent(styles.replace("font-style:italic;", "font-style:normal;"));

        removeByXPath("//g[@class='pgFoot autogenerated']", document, xpath);
        removeByXPath("//g[@class='section placeholder systemMilestone']", document, xpath);
        removeByXPath("//g[@class='sb placeholder']", document, xpath);
        removeByXPath("//g[@class='pb placeholder']", document, xpath);
        removeByXPath("//g[@class='pgHead']", document, xpath);
        removeByXPath("//g[@class='mdiv pageMilestone']", document, xpath);
        removeByXPath("//g[@class='score pageMilestone']", document, xpath);
        removeByXPath("//g[contains(@class, 'pageMilestoneEnd')]", document, xpath);
        removeByXPath("//g[contains(@class, 'systemMilestoneEnd')]", document, xpath);
        NodeList searchResultsList = (NodeList) xpath.compile("//tspan").evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < searchResultsList.getLength(); i++) {
            Node n = searchResultsList.item(i);
            ((Element) n).setAttribute("font-size", "250px");
        }
    }

    public int getPlayHeadWidth() throws Exception {
        int playHeadWidth = 0;
        NodeList searchResultsList = (NodeList) xpath.compile("//g[@class='note bounding-box']/rect").evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < searchResultsList.getLength(); i++) {
            Node n = searchResultsList.item(i);
            int width = Integer.parseInt(n.getAttributes().getNamedItem("width").getNodeValue());
            playHeadWidth = Math.max(playHeadWidth, width);
        }
        return (int) (playHeadWidth * 0.1);
    }
}
