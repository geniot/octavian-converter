package io.github.geniot.octavian.converter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

@Slf4j
public abstract class AbstractHandler {

    XmlMapper xmlMapper = new XmlMapper();
    DocumentBuilder docBuilder;
    XPath xpath;
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer;

    public final Document document;

    public AbstractHandler(String xml) throws Exception {
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(false);
        docBuilder = documentBuilderFactory.newDocumentBuilder();
        //ignore dtd https://stackoverflow.com/a/155353/488635
        docBuilder.setEntityResolver((_, _) -> new InputSource(new StringReader("")));
        xpath = XPathFactory.newInstance().newXPath();
        transformer = transformerFactory.newTransformer();
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        this.document = docBuilder.parse(inputStream);
    }

    public String document2string() throws Exception {
        DOMSource source = new DOMSource(document);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(byteArrayOutputStream);
        transformer.transform(source, result);
        return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
    }


    protected void removeAttributeByXPath(String expression, Node node, String attr) throws Exception {
        NodeList searchResultsList = (NodeList) xpath.compile(expression).evaluate(node, XPathConstants.NODESET);
        for (int i = 0; i < searchResultsList.getLength(); i++) {
            Node n = searchResultsList.item(i);
            if (n.getAttributes().getNamedItem(attr) != null) {
                n.getAttributes().removeNamedItem(attr);
            }
        }
    }

    protected String getByXPath(String expression, Node node) throws Exception {
        NodeList searchResultsList = (NodeList) xpath.compile(expression).evaluate(node, XPathConstants.NODESET);
        if (searchResultsList.getLength() > 0) {
            return searchResultsList.item(0).getTextContent();
        } else {
            return null;
        }
    }

    protected void removeByXPath(String expression, Node node) throws Exception {
        NodeList searchResultsList = (NodeList) xpath.compile(expression).evaluate(node, XPathConstants.NODESET);
        for (int i = 0; i < searchResultsList.getLength(); i++) {
            Node n = searchResultsList.item(i);
            n.getParentNode().removeChild(n);
        }
    }
}
