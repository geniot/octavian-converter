package io.github.geniot.octavian.converter.tools;

import io.github.geniot.octavian.converter.model.Jump;
import io.github.geniot.octavian.converter.model.Volta;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class UnrollContext {

    private static final int RECURSION_CATCH_LIMIT = 1000;

    /**
     * Input
     */
    XPath xpath;
    NodeList nodeList;
    Node parentNode;

    int currentMeasure = 1;
    Jump currentJump = null;
    int currentRepeat = 1;

    Set<Integer> startRepeats = new HashSet<>();//new start repeat triggers currentRepeat reset
    Map<Integer, Integer> endRepeats = new HashMap<>();//measure to number of hits (decrementing)
    Map<Integer, Set<Volta>> voltas = new HashMap<>();//measure to volta
    Map<Integer, Jump> jumps = new HashMap<>();//measure to jump

    /**
     * Output
     */
    List<Integer> measures = new ArrayList<>();

    public UnrollContext(NodeList nl, Node pn, XPath x) {
        this.xpath = x;
        this.nodeList = nl;
        this.parentNode = pn;
    }

    public void resetRepeats() {
        currentRepeat = 1;
        startRepeats.clear();
        endRepeats.clear();
        voltas.clear();
    }

    public void registerStartRepeat() {
        startRepeats.add(currentMeasure);
    }

    public void registerEndRepeat() throws Exception {
        Integer counts = endRepeats.get(currentMeasure);
        if (counts == null) {
            counts = getEndRepeatCount();
        }
        --counts;
        endRepeats.put(currentMeasure, counts);
    }


    public boolean shouldRepeat() {
        Integer counts = endRepeats.get(currentMeasure);
        if (counts == null || counts >= 2) {
            return true;
        } else {
            return false;
        }
    }

    public void goToStartRepeat() throws Exception {
        for (int i = currentMeasure; i >= 1; i--) {
            currentMeasure = i;
            if (isStartRepeat(currentMeasure)) {
                return;
            }
        }
    }

    private Integer getEndRepeatCount() throws Exception {
        Node currentMeasureNode = nodeList.item(currentMeasure - 1).cloneNode(true);
        String searchResult = (String) xpath.compile(".//endRepeat/text()").evaluate(currentMeasureNode, XPathConstants.STRING);
        return Integer.parseInt(searchResult);
    }

    public boolean isNewStartRepeat() {
        return !startRepeats.contains(currentMeasure);
    }

    public boolean isStartRepeat(int measure) throws Exception {
        Node currentMeasureNode = nodeList.item(measure - 1).cloneNode(true);
        return hasChildNode(currentMeasureNode, "startRepeat", xpath);
    }

    public boolean isEndRepeat(int measure) throws Exception {
        Node currentMeasureNode = nodeList.item(measure - 1).cloneNode(true);
        return hasChildNode(currentMeasureNode, "endRepeat", xpath);
    }

    private boolean hasChildNode(Node parentNode, String expression, XPath xpath) throws Exception {
        NodeList searchResult = (NodeList) xpath.compile(expression).evaluate(parentNode, XPathConstants.NODESET);
        return searchResult.getLength() > 0;
    }

    public void add() {
        measures.add(currentMeasure);
    }

    public boolean isEndReached() {
        return currentMeasure > nodeList.getLength();
    }

    public void checkRecursion() throws Exception {
        if (measures.size() > RECURSION_CATCH_LIMIT) {
            throw new Exception("Could not unroll the score. Are all repeats correct?");
        }
    }

    public void next() {
        ++currentMeasure;
    }

    public boolean isVolta(int measure) throws Exception {
        Node currentMeasureNode = nodeList.item(measure - 1).cloneNode(true);
        return hasChildNode(currentMeasureNode, ".//Spanner[@type='Volta']/next", xpath);
    }

    public boolean shouldSkipVolta() {
        Set<Volta> voltas = this.voltas.get(currentMeasure);
        if (voltas == null || voltas.isEmpty()) {
            return false;
        } else {
            for (Volta volta : voltas) {
                if (volta.getRepeatList().contains(currentRepeat)) {
                    return false;
                }
            }
            return true;
        }
    }

    public void registerVolta() throws Exception {
        if (!voltas.containsKey(currentMeasure)) {

            Node currentMeasureNode = nodeList.item(currentMeasure - 1).cloneNode(true);
            NodeList voltaSpanners = (NodeList) xpath.compile(".//Spanner[@type='Volta']").evaluate(currentMeasureNode, XPathConstants.NODESET);

            Set<Volta> voltaSet = new HashSet<>();
            for (int i = 0; i < voltaSpanners.getLength(); i++) {

                Node currentVoltaSpannerNode = voltaSpanners.item(i).cloneNode(true);

                //filtering out closing voltas
                NodeList nextLocations = (NodeList) xpath.compile(".//next").evaluate(currentVoltaSpannerNode, XPathConstants.NODESET);
                if (nextLocations.getLength() == 0) {
                    continue;
                }

                String nextLocationMeasures = (String) xpath.compile(".//next/location/measures/text()").evaluate(currentVoltaSpannerNode, XPathConstants.STRING);
                String nextLocationFractions = (String) xpath.compile(".//next/location/fractions/text()").evaluate(currentVoltaSpannerNode, XPathConstants.STRING);
                String voltaEndings = (String) xpath.compile(".//Volta/endings/text()").evaluate(currentVoltaSpannerNode, XPathConstants.STRING);
                String endHookType = (String) xpath.compile(".//Volta/endHookType/text()").evaluate(currentVoltaSpannerNode, XPathConstants.STRING);


                Volta volta = new Volta();
                //#40 open Volta should end at next end repeat
                if (StringUtils.isEmpty(endHookType)) {
                    volta.setNextLocationMeasures(getOpenMeasuresCount());
                } else {
                    try {
                        volta.setNextLocationMeasures(Integer.parseInt(nextLocationMeasures));
                    } catch (Exception ex) {
                        volta.setNextLocationMeasures(1);//can be /next/location/fractions 1/1
                    }
                }
                if (StringUtils.isNotEmpty(nextLocationFractions)) {
                    volta.setNextLocationMeasures(volta.getNextLocationMeasures() + 1);
                }

                TreeSet<Integer> voltaEndingsSet = new TreeSet<>();
                for (String ending : voltaEndings.split(",")) {
                    if (StringUtils.isNotEmpty(ending)) {
                        voltaEndingsSet.add(Integer.parseInt(ending.trim()));
                    }
                }
                volta.setRepeatList(voltaEndingsSet);

                voltaSet.add(volta);
            }

            voltas.put(currentMeasure, voltaSet);
        }
    }

    private int getOpenMeasuresCount() throws Exception {
        int count = 0;
        for (int i = currentMeasure; i <= nodeList.getLength(); i++) {
            if (i != currentMeasure && isVolta(i)) {
                return count;
            }
            ++count;
            if (isStartRepeat(i) || isEndRepeat(i)) {
                return count;
            }
        }
        return count;
    }

    public void skipVolta() throws Exception {
        int maxSkip = 0;
        for (Volta v : voltas.get(currentMeasure)) {
            maxSkip = Math.max(maxSkip, v.getNextLocationMeasures());
            if (v.getRepeatList().contains(currentRepeat)) {
                int skip = v.getNextLocationMeasures();
                currentMeasure += skip;
                return;
            }
        }
        currentMeasure += maxSkip;
    }

    @Override
    public String toString() {
        return Arrays.stream(measures.toArray()).map(String::valueOf).collect(Collectors.joining("_"));
    }

    public boolean hasJump() throws Exception {
        Node currentMeasureNode = nodeList.item(currentMeasure - 1).cloneNode(true);
        boolean hasJump = hasChildNode(currentMeasureNode, "Jump", xpath);
        if (hasJump) {
            //validate #26
            String jumpTo = (String) xpath.compile(".//Jump/jumpTo/text()").evaluate(currentMeasureNode, XPathConstants.STRING);
            String playUntil = (String) xpath.compile(".//Jump/playUntil/text()").evaluate(currentMeasureNode, XPathConstants.STRING);
            String continueAt = (String) xpath.compile(".//Jump/continueAt/text()").evaluate(currentMeasureNode, XPathConstants.STRING);

            if (!jumpTo.equals("start")) {
                if (StringUtils.isEmpty(jumpTo) || !hasChildNode(parentNode, ".//Marker/label[text()='" + jumpTo + "']", xpath)) {
                    return false;
                }
            }

            if (!playUntil.equals("end") && !playUntil.equals("coda")) {
                if (!StringUtils.isEmpty(playUntil) && !hasChildNode(parentNode, ".//Marker/label[text()='" + playUntil + "']", xpath)) {
                    return false;
                }
            }

            if (!continueAt.equals("codab")) {
                if (!StringUtils.isEmpty(continueAt) && !hasChildNode(parentNode, ".//Marker/label[text()='" + continueAt + "']", xpath)) {
                    return false;
                }
            }

            return true;

        } else {
            return false;
        }

    }

    public boolean shouldJump() throws Exception {
        //we do not reuse jumps, vicious circle
        Jump jump = jumps.get(currentMeasure);

        if (isJumpInFirstRepeat()) {
            return false;
        } else {
            return !jump.isJumped;
        }
    }

    /**
     * see use case 24
     */
    public boolean isJumpInFirstRepeat() throws Exception {
        return isJumpInRepeat() && currentRepeat == 1;
    }

    public boolean isJumpInRepeat() throws Exception {
        for (int i = currentMeasure; i <= nodeList.getLength(); i++) {
            if (i != currentMeasure && isStartRepeat(i)) {
                return false;
            }
            if (isEndRepeat(i)) {
                return true;
            }
        }
        return false;
    }

    public void registerJump() throws Exception {
        if (!jumps.containsKey(currentMeasure)) {
            Node currentMeasureNode = nodeList.item(currentMeasure - 1).cloneNode(true);

            String jumpTo = (String) xpath.compile(".//Jump/jumpTo/text()").evaluate(currentMeasureNode, XPathConstants.STRING);
            String playUntil = (String) xpath.compile(".//Jump/playUntil/text()").evaluate(currentMeasureNode, XPathConstants.STRING);
            String continueAt = (String) xpath.compile(".//Jump/continueAt/text()").evaluate(currentMeasureNode, XPathConstants.STRING);
            String playRepeats = (String) xpath.compile(".//Jump/playRepeats/text()").evaluate(currentMeasureNode, XPathConstants.STRING);

            Jump jump = new Jump();
            jump.setJumpTo(jumpTo);
            jump.setPlayUntil(playUntil);
            jump.setContinueAt(continueAt);
            if (StringUtils.isNotEmpty(playRepeats)) {
                jump.setShouldPlayRepeats(true);
            }

            jumps.put(currentMeasure, jump);
        }

        currentJump = jumps.get(currentMeasure);

    }

    public void jump() throws Exception {
        Jump jump = jumps.get(currentMeasure);
        jump.setJumped(true);

        if (jump.getJumpTo().equals("start")) {
            currentMeasure = 1;
        } else {
            for (int i = currentMeasure; i >= 1; i--) {
                currentMeasure = i;
                Node currentMeasureNode = nodeList.item(currentMeasure - 1).cloneNode(true);
                Set<String> measureLabels = getMeasureLabels(currentMeasureNode);
                if (measureLabels.contains(currentJump.jumpTo)) {
                    return;
                }
            }
        }
    }

    public boolean isPlayUntilReached() throws Exception {
        if (currentJump != null) {
            Node currentMeasureNode = nodeList.item(currentMeasure - 1).cloneNode(true);
            Set<String> measureLabels = getMeasureLabels(currentMeasureNode);
            if (measureLabels.contains(currentJump.playUntil)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> getMeasureLabels(Node currentMeasureNode) throws Exception {
        Set<String> measureLabels = new HashSet<>();
        NodeList labelsList = (NodeList) xpath.compile(".//Marker/label/text()").evaluate(currentMeasureNode, XPathConstants.NODESET);
        for (int i = 0; i < labelsList.getLength(); i++) {
            Node labelNode = labelsList.item(i).cloneNode(true);
            if (!StringUtils.isEmpty(labelNode.getNodeValue())) {
                measureLabels.add(labelNode.getNodeValue());
            }
        }
        return measureLabels;
    }

    public boolean shouldContinueAt() throws Exception {
        boolean isContinueAtValid = !StringUtils.isEmpty(currentJump.continueAt);
        boolean measureContainsContinueAtLabel = hasChildNode(parentNode, ".//Marker/label[text()='" + currentJump.continueAt + "']", xpath);

        if (isContinueAtValid && measureContainsContinueAtLabel) {
            return true;
        } else {
            return false;
        }
    }

    public void goToContinueAt() throws Exception {
        for (int i = currentMeasure; i <= nodeList.getLength(); i++) {
            currentMeasure = i;
            Node currentMeasureNode = nodeList.item(currentMeasure - 1).cloneNode(true);
            Set<String> measureLabels = getMeasureLabels(currentMeasureNode);
            if (measureLabels.contains(currentJump.continueAt)) {
                return;
            }
        }
    }
}
