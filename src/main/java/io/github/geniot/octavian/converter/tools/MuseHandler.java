package io.github.geniot.octavian.converter.tools;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.github.geniot.octavian.converter.model.MuseFixResult;
import io.github.geniot.octavian.converter.model.musescore.MuseScoreRoot;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MuseHandler {

    static Logger logger = LoggerFactory.getLogger(MuseHandler.class);

    XmlMapper xmlMapper = new XmlMapper();
    UnrollHandler unrollHandler = new UnrollHandler();
    DocumentBuilder docBuilder;
    XPath xpath;
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer;

    public MuseHandler() {
        try {
            xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(false);
            docBuilder = documentBuilderFactory.newDocumentBuilder();
            xpath = XPathFactory.newInstance().newXPath();
            transformer = transformerFactory.newTransformer();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public String getPanorama(String museXml, boolean shouldWiden, boolean shouldUnroll) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(museXml.getBytes(StandardCharsets.UTF_8));
        Document document = docBuilder.parse(inputStream);
        panoramize(document);
        if (shouldUnroll) {
            unroll(document);
            cleanify(document);
        }
        return shouldWiden ? document2string(widen(document, xpath)) : document2string(document);
    }

    public MuseFixResult fixMuseXml(String museXml) throws Exception {
        MuseFixResult museFixResult = new MuseFixResult();

        InputStream inputStream = new ByteArrayInputStream(museXml.getBytes(StandardCharsets.UTF_8));
        Document document = docBuilder.parse(inputStream);
        panoramize(document);

        cleanifyGeneric(document);

        //repeats
        museFixResult.setRepScore(document2string(document));
        museFixResult.setRepWideScore(document2string(widen(document, xpath)));

        Node[] repeatsRightLeft = getRightLeft(document);
        cleanify(repeatsRightLeft[0]);
        cleanify(repeatsRightLeft[1]);

        String repeatsRightDocStr = document2string(repeatsRightLeft[0]);
        String repeatsLeftDocStr = document2string(repeatsRightLeft[1]);

        museFixResult.setRepRightScore(repeatsRightDocStr);
        museFixResult.setRepLeftScore(repeatsLeftDocStr);

        museFixResult.setRepRightRoot(xmlMapper.readValue(repeatsRightDocStr, MuseScoreRoot.class));
        museFixResult.setRepLeftRoot(xmlMapper.readValue(repeatsLeftDocStr, MuseScoreRoot.class));

        //unrolling
        List<Integer> unrolledOrder = unroll(document);
        museFixResult.setRepeats(ArrayUtils.toPrimitive(unrolledOrder.toArray(new Integer[0])));

        cleanify(document);

        Node[] rightLeft = getRightLeft(document);
        String rightDocStr = document2string(rightLeft[0]);
        String leftDocStr = document2string(rightLeft[1]);

        museFixResult.setRightScore(rightDocStr);
        museFixResult.setLeftScore(leftDocStr);

        museFixResult.setRightRoot(xmlMapper.readValue(rightDocStr, MuseScoreRoot.class));
        museFixResult.setLeftRoot(xmlMapper.readValue(leftDocStr, MuseScoreRoot.class));

        cleanifyTempo(document);

        museFixResult.setScore(document2string(document));
        museFixResult.setWideScore(document2string(widen(document, xpath)));

        return museFixResult;
    }

    private Node[] getRightLeft(Document document) throws Exception {
        Node rightHandDocument = document.cloneNode(true);
        removeByXPath("//Score/Staff[@id='2']", rightHandDocument, xpath);
        removeByXPath("//Part/Staff[@id='2']", rightHandDocument, xpath);

        Node leftHandDocument = document.cloneNode(true);
        removeByXPath("//Score/Staff[@id='1']", leftHandDocument, xpath);
        removeByXPath("//Part/Staff[@id='1']", leftHandDocument, xpath);

        Node partStaffNode = (Node) xpath.compile("//Part/Staff[@id='2']").evaluate(leftHandDocument, XPathConstants.NODE);
        partStaffNode.getAttributes().getNamedItem("id").setNodeValue("1");
        Node scoreStaffNode = (Node) xpath.compile("//Score/Staff[@id='2']").evaluate(leftHandDocument, XPathConstants.NODE);
        scoreStaffNode.getAttributes().getNamedItem("id").setNodeValue("1");

        return new Node[]{rightHandDocument, leftHandDocument};
    }

    private void cleanifyGeneric(Document document) throws Exception {
        removeByXPath("//Staff/VBox", document, xpath);
        removeByXPath("//stretch", document, xpath);
        removeByXPath("//LayoutBreak", document, xpath);
        //adding minimum stretch to all measures
        NodeList measuresList = (NodeList) xpath.compile("//Measure").evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < measuresList.getLength(); i++) {
            Node n = measuresList.item(i);
            Element newStretchElement = document.createElement("stretch");
            newStretchElement.appendChild(document.createTextNode("0.1"));
            n.appendChild(newStretchElement);
        }
    }

    private void cleanify(Node node) throws Exception {
        //remove title, author, startRepeat, endRepeat, volta spanners
        removeByXPath("//startRepeat", node, xpath);
        removeByXPath("//endRepeat", node, xpath);
        removeByXPath("//Spanner[@type='Volta']", node, xpath);
        removeByXPath("//Jump", node, xpath);

        removeByXPath("//Instrument/longName", node, xpath);
        removeByXPath("//StaffText", node, xpath);
        removeByXPath("//Marker", node, xpath);
    }

    private void cleanifyTempo(Node node) throws Exception {
        removeByXPath("//Tempo", node, xpath);
    }

    private List<Integer> unroll(Document document) throws Exception {
        //unroll repeats
        //collect measures in unrolled way
        Map<Integer, ArrayList<Node>> newMeasuresMap = new HashMap<>();
        NodeList staffsList = (NodeList) xpath.compile("//Score/Staff").evaluate(document, XPathConstants.NODESET);

        Node rightHandStaffNode = staffsList.item(0).cloneNode(true);
        Node leftHandStaffNode = staffsList.item(1).cloneNode(true);

        newMeasuresMap.put(0, new ArrayList<>());
        newMeasuresMap.put(1, new ArrayList<>());

        NodeList rightHandMeasuresList = (NodeList) xpath.compile("//Measure").evaluate(rightHandStaffNode, XPathConstants.NODESET);
        NodeList leftHandMeasuresList = (NodeList) xpath.compile("//Measure").evaluate(leftHandStaffNode, XPathConstants.NODESET);

        UnrollContext ctx = new UnrollContext(rightHandMeasuresList, document, xpath);
        unrollHandler.unroll(ctx);
        List<Integer> unrolledOrder = ctx.getMeasures();

        for (int i = 0; i < unrolledOrder.size(); i++) {
            Integer measureIndex = unrolledOrder.get(i);
            Node rightHandMeasureNode = rightHandMeasuresList.item(measureIndex - 1).cloneNode(true);
            Node leftHandMeasureNode = leftHandMeasuresList.item(measureIndex - 1).cloneNode(true);

            if (i != 0 && measureIndex == 1) {
                removeByXPath("//TimeSig", rightHandMeasureNode, xpath);
                removeByXPath("//TimeSig", leftHandMeasureNode, xpath);

                removeByXPath("//Clef", rightHandMeasureNode, xpath);
                removeByXPath("//Clef", leftHandMeasureNode, xpath);
            }

            newMeasuresMap.get(0).add(rightHandMeasureNode);
            newMeasuresMap.get(1).add(leftHandMeasureNode);
        }

        //replace old measures with unrolled
        for (int i = 0; i < staffsList.getLength(); i++) {
            Node node = staffsList.item(i);
            removeChildren(node);
            ArrayList<Node> newNodesList = newMeasuresMap.get(i);
            for (Node n : newNodesList) {
                node.appendChild(n);
            }
        }

        return unrolledOrder;
    }

    private void panoramize(Document document) throws Exception {
        //set width to 1000 to create panoramic layout
        setElementValue("Style", "pagePrintableWidth", "1000", document);
        setElementValue("Style", "pageWidth", "1000", document);

        setElementValue("Style", "showMeasureNumberOne", "1", document);
        setElementValue("Style", "measureNumberInterval", "1", document);
        setElementValue("Style", "measureNumberSystem", "0", document);
        setElementValue("Style", "measureNumberFontSpatiumDependent", "1", document);
        setElementValue("Style", "measureNumberFontStyle", "0", document);
        setElementValue("Style", "measureNumberAlign", "center,baseline", document);

        setElementValue("Score", "showInvisible", "0", document);
        setElementValue("Score", "showUnprintable", "0", document);
        setElementValue("Score", "showFrames", "0", document);
        setElementValue("Score", "showMargins", "0", document);
        setElementValue("Score", "showHeader", "0", document);
        setElementValue("Score", "showFooter", "0", document);
    }

    /**
     * To later find what staff a svg note belongs to we increase the vertical space between staves.
     */
    private Node widen(Document document, XPath xpath) throws Exception {
        Document widenedDocument = (Document) document.cloneNode(true);
        setElementValue("Style", "akkoladeDistance", "20", widenedDocument);
        setElementValue("Style", "staffDistance", "20", widenedDocument);
        setElementValue("Style", "enableVerticalSpread", "0", widenedDocument);
        return widenedDocument;
    }

    private String document2string(Node document) throws Exception {
        DOMSource source = new DOMSource(document);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(byteArrayOutputStream);
        transformer.transform(source, result);
        return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
    }

    private void setElementValue(String parentNode, String tagName, String value, Document document) throws Exception {
        removeByXPath("//" + parentNode + "/" + tagName, document, xpath);
        Element styleElement = (Element) document.getElementsByTagName(parentNode).item(0);
        Element newElement = document.createElement(tagName);
        newElement.appendChild(document.createTextNode(value));
        styleElement.appendChild(newElement);
        styleElement.appendChild(document.createTextNode("\n"));
    }

    private void removeByXPath(String expression, Node node, XPath xpath) throws Exception {
        NodeList searchResultsList = (NodeList) xpath.compile(expression).evaluate(node, XPathConstants.NODESET);
        for (int i = 0; i < searchResultsList.getLength(); i++) {
            Node n = searchResultsList.item(i);
            n.getParentNode().removeChild(n);
        }
    }

    private void removeChildren(Node node) {
        while (node.hasChildNodes()) {
            node.removeChild(node.getFirstChild());
        }
    }

    public static String debugNode(Node node, boolean omitXmlDeclaration, boolean prettyPrint) {
        if (node == null) {
            throw new IllegalArgumentException("node is null.");
        }

        try {
            // Remove unwanted whitespaces
            node.normalize();
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile("//text()[normalize-space()='']");
            NodeList nodeList = (NodeList) expr.evaluate(node, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node nd = nodeList.item(i);
                nd.getParentNode().removeChild(nd);
            }

            // Create and setup transformer
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            if (omitXmlDeclaration == true) {
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            }

            if (prettyPrint == true) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            }

            // Turn the node into a string
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(node), new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }
}

