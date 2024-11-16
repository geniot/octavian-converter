package io.github.geniot.octavian.converter.tools;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import io.github.geniot.indexedtreemap.IndexedTreeSet;
import io.github.geniot.octavian.converter.model.SvgData;
import io.github.geniot.octavian.converter.model.SvgNote;
import io.github.geniot.octavian.converter.model.commands.CCommand;
import io.github.geniot.octavian.converter.model.commands.DCommand;
import io.github.geniot.octavian.converter.model.commands.MCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SvgHandler {
    static Logger logger = LoggerFactory.getLogger(SvgHandler.class);

    private DocumentBuilder docBuilder;
    private XPath xpath;

    public SvgHandler() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(false);
            docBuilder = documentBuilderFactory.newDocumentBuilder();
            xpath = XPathFactory.newInstance().newXPath();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public float[] getDimensions(byte[] svgBytes) throws Exception {
        Document document = docBuilder.parse(new ByteArrayInputStream(svgBytes));
        return getDimensions(document);
    }

    private float[] getDimensions(Document document) {
        String svgStr = document.getDocumentElement().getAttributeNode("viewBox").getValue();
        String[] result = svgStr.split(" ");
        float width = Float.parseFloat(result[2]);
        float height = Float.parseFloat(result[3]);
        return new float[]{width, height};
    }

    public SvgData getSvgData(byte[] svgBytes, byte[] wideSvgBytes) throws Exception {

        Document document = docBuilder.parse(new ByteArrayInputStream(svgBytes));
        Document wideDocument = docBuilder.parse(new ByteArrayInputStream(wideSvgBytes));

        float[] dimensions = getDimensions(document);

        IndexedTreeSet<Float> barOffsets = getBarOffsets(document, xpath);

        List<SvgNote> svgNotes = getSvgNotes(document, xpath, barOffsets);
        List<SvgNote> wideSvgNotes = getSvgNotes(wideDocument, xpath, barOffsets);

        if (svgNotes.size() != wideSvgNotes.size()) {
            throw new Exception("SVG notes in documents are not equal.");
        }

        float center = getCenter(wideDocument, xpath);
        for (int i = 0; i < svgNotes.size(); i++) {
            SvgNote wideSvgNote = wideSvgNotes.get(i);
            boolean isUpperStaff = wideSvgNote.getCenter() < center;
            svgNotes.get(i).setUpperStaff(isUpperStaff);
        }

        SvgData svgData = new SvgData();
        svgData.setWidth(dimensions[0]);
        svgData.setHeight(dimensions[1]);
        svgData.setPlayHeadWidth(getPlayHeadWidth(svgNotes));
        svgData.setBarOffsets(barOffsets);

        svgData.setRightHandNotes(new IndexedTreeSet<>(svgNotes.stream().filter(SvgNote::isUpperStaff).collect(Collectors.toList())));
        svgData.setLeftHandNotes(new IndexedTreeSet<>(svgNotes.stream().filter(svgNote -> !svgNote.isUpperStaff()).collect(Collectors.toList())));

        return svgData;
    }

    private float getPlayHeadWidth(List<SvgNote> svgNotes) {
        float maxWidth = 0;
        for (SvgNote svgNote : svgNotes) {
            maxWidth = Math.max(maxWidth, svgNote.getWidth());
        }
        return maxWidth;
    }

    private DCommand[] getCommands(String dStr) {
        String[] splits = dStr.split("M|C");
        List<DCommand> commands = new ArrayList<>();
        for (String split : splits) {
            if (split.trim().isEmpty()) {
                continue;
            }
            String[] moreSplits = split.trim().split(",|\\s");
            if (moreSplits.length == 2) {
                MCommand mCommand = new MCommand();
                mCommand.setX(Float.parseFloat(moreSplits[0]));
                mCommand.setY(Float.parseFloat(moreSplits[1]));
                commands.add(mCommand);
            } else {
                CCommand cCommand = new CCommand();
                cCommand.setX1(Float.parseFloat(moreSplits[0]));
                cCommand.setY1(Float.parseFloat(moreSplits[1]));
                cCommand.setX2(Float.parseFloat(moreSplits[2]));
                cCommand.setY2(Float.parseFloat(moreSplits[3]));
                cCommand.setX3(Float.parseFloat(moreSplits[4]));
                cCommand.setY3(Float.parseFloat(moreSplits[5]));
                commands.add(cCommand);
            }
        }
        return commands.toArray(new DCommand[0]);
    }

    private float[] getMatrix(String matrixStr) {
        if (matrixStr == null) {
            return new float[]{1, 1, 1, 1, 0, 0};
        }
        matrixStr = matrixStr.replaceAll("matrix", "").replaceAll("[()]", "");
        String[] splits = matrixStr.split(",");
        float[] matrix = new float[6];
        for (int i = 0; i < 6; i++) {
            matrix[i] = Float.parseFloat(splits[i]);
        }
        return matrix;
    }

    public byte[] svg2png(SVGDocument svgDocument) throws Exception {
        FloatSize size = svgDocument.size();
        BufferedImage image = new BufferedImage((int) size.width, (int) size.height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        svgDocument.render(null, g);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", byteArrayOutputStream);
        g.dispose();
        byteArrayOutputStream.flush();
        byteArrayOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }


    float getCenter(Document doc, XPath xpath) throws Exception {
        NodeList list = (NodeList) xpath.compile("//polyline[@class='StaffLines']").evaluate(doc, XPathConstants.NODESET);
        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            String points = node.getAttributes().getNamedItem("points").getNodeValue();
            String[] splits = points.split(" ");
            for (String split : splits) {
                float y = Float.parseFloat(split.split(",")[1]);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }
        return minY + Math.abs(maxY - minY) / 2;
    }

    List<SvgNote> getSvgNotes(Document doc, XPath xpath, IndexedTreeSet<Float> barOffsets) throws Exception {
        List<SvgNote> svgNotes = new ArrayList<>();
        Set<StringKey> notesSet = new HashSet<>();
        NodeList list = (NodeList) xpath.compile("//path[@class='Note']").evaluate(doc, XPathConstants.NODESET);
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);

            String matrixStr = node.getAttributes().getNamedItem("transform") == null ? null : node.getAttributes().getNamedItem("transform").getNodeValue();
            String dStr = node.getAttributes().getNamedItem("d").getNodeValue();

            float[] matrix = getMatrix(matrixStr);
            DCommand[] commands = getCommands(dStr);

            float minX = -1;
            float maxX = -1;

            float minY = -1;
            float maxY = -1;

            /**
             * https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/transform
             * newX = a * oldX + c * oldY + e
             * newY = b * oldX + d * oldY + f
             */
            for (DCommand command : commands) {

                float newX = matrix[0] * command.getX() + matrix[2] * command.getY() + matrix[4];
                float newY = matrix[1] * command.getX() + matrix[3] * command.getY() + matrix[5];

                if (matrixStr == null) {
                    newX = command.getX();
                    newY = command.getY();
                }

                if (maxX == -1) {
                    maxX = newX;
                }
                if (minX == -1) {
                    minX = newX;
                }

                if (maxY == -1) {
                    maxY = newY;
                }
                if (minY == -1) {
                    minY = newY;
                }

                maxX = Math.max(maxX, newX);
                minX = Math.min(minX, newX);

                maxY = Math.max(maxY, newY);
                minY = Math.min(minY, newY);

            }

            StringKey stringKey = new StringKey(matrixStr, dStr);
            if (notesSet.contains(stringKey)) {
                logger.info("Found note with same x/y: " + matrixStr + "; " + dStr);
                maxY -= 0.1;
            } else {
                notesSet.add(stringKey);
            }

            SvgNote svgNote = new SvgNote();

            svgNote.setX(minX);
            svgNote.setY(maxY);

            svgNote.setMeasure(barOffsets.entryIndex(barOffsets.floor(minX)) + 1);

            svgNote.setWidth(Math.abs(minX - maxX));
            svgNote.setHeight(Math.abs(minY - maxY));

            svgNotes.add(svgNote);
        }
        return svgNotes;
    }

    IndexedTreeSet<Float> getBarOffsets(Document doc, XPath xpath) throws Exception {
        IndexedTreeSet<Float> barOffsets = new IndexedTreeSet<>();
        NodeList list = (NodeList) xpath.compile("//polyline[@class='BarLine']").evaluate(doc, XPathConstants.NODESET);
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            String points = node.getAttributes().getNamedItem("points").getNodeValue();
            String[] splits = points.split(" ");
            if (splits.length != 2) {
                throw new Exception(String.valueOf(splits.length));
            }
            float x1 = Float.parseFloat(splits[0].split(",")[0]);
            float x2 = Float.parseFloat(splits[1].split(",")[0]);
            if (x1 != x2) {
                //horizontal line?
                continue;
            } else {
                Float lower = barOffsets.lower(x1);
                if (lower == null) {
                    barOffsets.add(x1);
                } else {
                    if (Math.abs(lower - x1) > 20) {//20 seems like a good value for svg vertical repeat bars and end bars
                        barOffsets.add(x1);
                    } else {
                        //updating lower to correctly catch next possible repeat bar (see double repeats)
                        barOffsets.remove(lower);
                        barOffsets.add(x1);
                    }
                }
            }

        }
        return barOffsets;
    }

    private static class StringKey {
        private final String str1;
        private final String str2;

        public StringKey(String s1, String s2) {
            this.str1 = s1;
            this.str2 = s2;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof StringKey) {
                StringKey s = (StringKey) obj;
                return str1.equals(s.str1) && str2.equals(s.str2);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (str1 + str2).hashCode();
        }
    }
}
