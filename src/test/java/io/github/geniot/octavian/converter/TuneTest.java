package io.github.geniot.octavian.converter;

public class TuneTest {
    static void main(String[] args) {
        try {
//            PngHandler pngHandler = new PngHandler();
//            MusicXmlHandler handler = new MusicXmlHandler();
//            SvgHandler svgHandler = new SvgHandler();
//            JsonMapper jsonMapper = new JsonMapper();
//            PointsHandler pointsHandler = new PointsHandler();
//            ObjectMapper objectMapper = new ObjectMapper();
//            XmlMapper xmlMapper = new XmlMapper();
//            xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//            IndexedTreeMap<Integer, Point> points = new IndexedTreeMap<>();
//            Tune tune = new Tune();
//
//            String musicXmlStr = Files.readString(Path.of("tmp/out.xml"), StandardCharsets.UTF_8);
//            String meiXmlStr = Files.readString(Path.of("tmp/out.mei"), StandardCharsets.UTF_8);
//            String svgStr = Files.readString(Path.of("tmp/out.svg"), StandardCharsets.UTF_8);
//            String jsonStr = Files.readString(Path.of("tmp/out.json"), StandardCharsets.UTF_8);
//            byte[] pngBbs = Files.readAllBytes(Path.of("tmp/out.png"));
//
//            //title, author
//            String[] titleAndAuthor = handler.getTitleAndAuthor(musicXmlStr);
//            tune.setTitle(titleAndAuthor[0]);
//            tune.setAuthor(titleAndAuthor[1]);
//
//            //png height, width
//            Dimension dimension = pngHandler.getPngDimension(pngBbs);
//            tune.setPngHeight(dimension.height);
//            tune.setPngWidth(dimension.width);
//            //sheet height, width
//            tune.setSheetHeight(dimension.height);
//            tune.setSheetWidth(dimension.width);
//
//            //selection ranges
//            tune.setSelectionRanges(new int[]{});

            //bar offsets
//            long shift = 260;
//            SortedSet<Integer> barOffsets = svgHandler.getBarOffsets(svgStr);
//            tune.setBarOffsets(toInts(svgHandler.scale(barOffsets, 0.1, shift)));


            //play head width
//            tune.setPlayHeadWidth(svgHandler.getPlayHeadWidth(svgRoot));

            //points

//            MidiRoot midiRoot = jsonMapper.readValue(jsonStr, MidiRoot.class);
//            pointsHandler.identifyNotes(meiXmlStr, midiRoot);
//            pointsHandler.addPoints(points, midiRoot, svgRoot.definitionScale.pageMargin.svgSystem.geNoteOffsets());
//            pointsHandler.setOffsetsToOffPoints(new ArrayList<>(points.values()), tune.getSheetWidth());
//            pointsHandler.setBars(points, barOffsets);
//            tune.setPoints(points.sequencedValues().toArray(new Point[0]));
//            float scaleFactor = (float) svgRoot.definitionScale.getViewBox().width / tune.getSheetWidth();
//            pointsHandler.scale(tune, scaleFactor);
//            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tune);
//            Files.writeString(Path.of("tmp/actual.json"), json);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
