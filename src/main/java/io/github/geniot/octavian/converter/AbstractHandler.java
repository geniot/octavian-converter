package io.github.geniot.octavian.converter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AbstractHandler {
    static Logger logger = LoggerFactory.getLogger(AbstractHandler.class);

    XmlMapper xmlMapper = new XmlMapper();
    DocumentBuilder docBuilder;
    XPath xpath;
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer;

    public AbstractHandler() {
        try {
            xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(false);
            docBuilder = documentBuilderFactory.newDocumentBuilder();
            //ignore dtd https://stackoverflow.com/a/155353/488635
            docBuilder.setEntityResolver((_, _) -> new InputSource(new StringReader("")));
            xpath = XPathFactory.newInstance().newXPath();
            transformer = transformerFactory.newTransformer();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    protected String document2string(Node document) throws Exception {
        DOMSource source = new DOMSource(document);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(byteArrayOutputStream);
        transformer.transform(source, result);
        return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
    }

    protected byte[] runProcess(
            List<String> commandLine,
            byte[] input,
            String inExtension,
            String outExtension,
            String outArgName,
            boolean isOutputFirst,
            boolean verbose
    ) throws Exception {

        Path tempInputFile = Files.createTempFile("octavian-" + System.currentTimeMillis() + "-input-", String.valueOf(input.hashCode() + inExtension));
        Path tempOutputFile = Files.createTempFile("octavian-" + System.currentTimeMillis() + "-output-", input.hashCode() + outExtension);
        tempInputFile.toFile().deleteOnExit();
        tempOutputFile.toFile().deleteOnExit();
        Files.write(tempInputFile, input);

        if (isOutputFirst) {
            if (!StringUtils.isEmpty(outArgName)) {
                commandLine.add(outArgName);
            }
            commandLine.add(tempOutputFile.toString());
            commandLine.add(tempInputFile.toString());
        } else {
            commandLine.add(tempInputFile.toString());
            if (!StringUtils.isEmpty(outArgName)) {
                commandLine.add(outArgName);
            }
            commandLine.add(tempOutputFile.toString());
        }

        NuProcessBuilder pb = new NuProcessBuilder(commandLine);
        ProcessHandler handler = new ProcessHandler(verbose);
        pb.setProcessListener(handler);
        NuProcess process = pb.start();
        process.waitFor(0, TimeUnit.SECONDS); // when 0 is used for waitFor() the wait is infinite

        byte[] result = Files.readAllBytes(tempOutputFile);

        Files.delete(tempInputFile);
        Files.delete(tempOutputFile);

        return result;
    }

    protected void removeAttributeByXPath(String expression, Node node, XPath xpath, String attr) throws Exception {
        NodeList searchResultsList = (NodeList) xpath.compile(expression).evaluate(node, XPathConstants.NODESET);
        for (int i = 0; i < searchResultsList.getLength(); i++) {
            Node n = searchResultsList.item(i);
            if (n.getAttributes().getNamedItem(attr) != null) {
                n.getAttributes().removeNamedItem(attr);
            }
        }
    }

    protected void removeByXPath(String expression, Node node, XPath xpath) throws Exception {
        NodeList searchResultsList = (NodeList) xpath.compile(expression).evaluate(node, XPathConstants.NODESET);
        for (int i = 0; i < searchResultsList.getLength(); i++) {
            Node n = searchResultsList.item(i);
            n.getParentNode().removeChild(n);
        }
    }
}
