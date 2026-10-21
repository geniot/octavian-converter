package io.github.geniot.octavian.converter.verovio;

import java.util.ArrayList;
import java.util.List;

public class MidiHandler extends AbstractXmlHandler {

    public byte[] xml2midi(byte[] xmlBbs) throws Exception {
        ArrayList<String> args = new ArrayList<>(List.of("/opt/musescore/musescore"));
        return runProcess(args, xmlBbs, ".xml", ".mid", "-o", true, true);
    }

    public byte[] midi2json(byte[] midiBbs) throws Exception {
        ArrayList<String> args = new ArrayList<>(List.of("./midi2json"));
        return runProcess(args, midiBbs, ".mid", ".json", "", false, true);
    }

    public byte[] xml2mp3(byte[] xmlBbs) throws Exception {
        ArrayList<String> args = new ArrayList<>(List.of("/opt/musescore/musescore"));
        return runProcess(args, xmlBbs, ".xml", ".mp3", "-o", true, false);
    }
}
