package io.github.geniot.octavian.converter.tools;

import io.github.geniot.octavian.converter.model.Constants;
import io.github.geniot.octavian.converter.model.musescore.MuseScoreRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.*;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.TreeMap;

public class MidiHandler {
    static Logger logger = LoggerFactory.getLogger(MidiHandler.class);

    public TreeMap<Long, Float> getTempoMap(ByteArrayInputStream byteArrayInputStream) throws Exception {
        TreeMap<Long, Float> ticksToTempo = new TreeMap<>();
        int bpm = 120;//by default, can be initialized once below
        Sequence sequence = MidiSystem.getSequence(byteArrayInputStream);
        float PPQ = sequence.getResolution();
        float oneTickMilliseconds = 60000 / (bpm * PPQ);

        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                if (message instanceof MetaMessage) {
                    MetaMessage metaMessage = (MetaMessage) message;
                    //https://www.recordingblogs.com/wiki/midi-set-tempo-meta-message
                    //https://www.recordingblogs.com/wiki/midi-meta-messages
                    if (metaMessage.getType() == Constants.SET_TEMPO_META_TYPE) {
                        byte[] bytes = {0, message.getMessage()[3], message.getMessage()[4], message.getMessage()[5]};
                        bpm = 60000000 / ByteBuffer.wrap(bytes).getInt();
                        oneTickMilliseconds = 60000 / (bpm * PPQ);
                    }
                }
                ticksToTempo.put(event.getTick(), oneTickMilliseconds);
            }
        }
        return ticksToTempo;
    }

    public void setTicksToNotes(MuseScoreRoot document, ByteArrayInputStream byteArrayInputStream) throws Exception {
        Sequence sequence = MidiSystem.getSequence(byteArrayInputStream);
        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;

                    int key = sm.getData1();
                    int velocity = sm.getData2();
                    int messageCommand = sm.getCommand();

                    if (messageCommand == ShortMessage.NOTE_ON && velocity == 0) {
                        messageCommand = ShortMessage.NOTE_OFF;
                    }

                    if (messageCommand == ShortMessage.NOTE_ON || messageCommand == ShortMessage.NOTE_OFF) {
                        document.registerTick(event.getTick(), key, messageCommand);
                    }
                }
            }
        }
    }
}
