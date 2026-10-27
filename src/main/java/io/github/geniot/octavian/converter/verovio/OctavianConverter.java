package io.github.geniot.octavian.converter.verovio;

import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;


/**
 * Produces files necessary for the Octavian player.
 * Input: MusicXML or MuseScore (.mscz, .mscx)
 * Output: MP3, PNG, json
 */
@Data
@Component
public class OctavianConverter {
    static Logger logger = LoggerFactory.getLogger(OctavianConverter.class);

    MidiHandler midiHandler = new MidiHandler();
    SvgHandler svgHandler = new SvgHandler();
    PngHandler pngHandler = new PngHandler();
    MeiHandler meiHandler = new MeiHandler();


    public MuseConversionResponse convert(String xml) throws Exception {
        MuseConversionResponse response = new MuseConversionResponse();
        //XML
        IOUtils.write(xml, new FileOutputStream("tmp/out.xml"), StandardCharsets.UTF_8);

        //XML->MIDI
        byte[] midi = midiHandler.xml2midi(xml.getBytes(StandardCharsets.UTF_8));
        IOUtils.write(midi, new FileOutputStream("tmp/out.mid"));

        //MIDI->JSON
        byte[] json = midiHandler.midi2json(midi);
        IOUtils.write(json, new FileOutputStream("tmp/out.json"));

        //XML->MP3
        byte[] mp3 = midiHandler.xml2mp3(xml.getBytes(StandardCharsets.UTF_8));
        IOUtils.write(mp3, new FileOutputStream("tmp/out.mp3"));

        //XML->MEI
        byte[] mei = meiHandler.xml2mei(xml.getBytes(StandardCharsets.UTF_8));
        IOUtils.write(mei, new FileOutputStream("tmp/out.mei"));

        //MEI->SVG
        byte[] svg = svgHandler.mei2svg(mei);
        IOUtils.write(svg, new FileOutputStream("tmp/out.svg"));

        //SVG->SVG_XML
        byte[] svgXml = svgHandler.svg2xml(svg);
        IOUtils.write(svgXml, new FileOutputStream("tmp/out_svg.xml"));

        //SVG->PNG
        byte[] png = pngHandler.svg2png(svg);
        IOUtils.write(png, new FileOutputStream("tmp/out.png"));

        return response;
    }


}
