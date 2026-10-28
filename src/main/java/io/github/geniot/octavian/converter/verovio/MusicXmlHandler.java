package io.github.geniot.octavian.converter.verovio;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MusicXmlHandler extends AbstractXmlHandler {
    public String[] getTitleAndAuthor(String musicXml) throws Exception {
        List<String> titleAndAuthor = new ArrayList<>();
        InputStream inputStream = new ByteArrayInputStream(musicXml.getBytes(StandardCharsets.UTF_8));
        Document document = docBuilder.parse(inputStream);
        NodeList searchResultsList = (NodeList) xpath.compile("//credit-words").evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < searchResultsList.getLength(); i++) {
            Node n = searchResultsList.item(i);
            titleAndAuthor.add(n.getTextContent());
        }
        return titleAndAuthor.toArray(new String[0]);
    }
}
