package io.github.geniot.octavian.converter.tools;

import com.zaxxer.nuprocess.NuAbstractProcessHandler;
import com.zaxxer.nuprocess.NuProcess;

import java.nio.ByteBuffer;

class VerovioProcessHandler extends NuAbstractProcessHandler {
    private NuProcess nuProcess;

    @Override
    public void onStart(NuProcess nuProcess) {
        this.nuProcess = nuProcess;
    }

    @Override
    public void onStdout(ByteBuffer buffer, boolean closed) {
        if (!closed) {
            byte[] bytes = new byte[buffer.remaining()];
            // You must update buffer.position() before returning (either implicitly,
            // like this, or explicitly) to indicate how many bytes your handler has consumed.
            buffer.get(bytes);
            System.out.println(new String(bytes));

            // For this example, we're done, so closing STDIN will cause the "cat" process to exit
            nuProcess.closeStdin(true);
        }
    }

    @Override
    public void onStderr(ByteBuffer buffer, boolean closed) {
        if (!closed) {
            byte[] bytes = new byte[buffer.remaining()];
            // You must update buffer.position() before returning (either implicitly,
            // like this, or explicitly) to indicate how many bytes your handler has consumed.
            buffer.get(bytes);
            System.out.println(new String(bytes));

            // For this example, we're done, so closing STDIN will cause the "cat" process to exit
            nuProcess.closeStdin(true);
        }
    }
}
