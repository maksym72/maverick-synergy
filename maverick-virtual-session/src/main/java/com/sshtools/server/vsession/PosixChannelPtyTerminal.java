/*
 * Copyright (c) 2002-2018, the original author or authors.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * https://opensource.org/licenses/BSD-3-Clause
 */
package com.sshtools.server.vsession;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jline.terminal.impl.AbstractPosixTerminal;
import org.jline.terminal.spi.Pty;
import org.jline.utils.ClosedException;
import org.jline.utils.Log;
import org.jline.utils.NonBlocking;
import org.jline.utils.NonBlockingInputStream;
import org.jline.utils.NonBlockingReader;

import com.sshtools.common.ssh.ChannelNG;

public class PosixChannelPtyTerminal extends AbstractPosixTerminal {

    private final ChannelNG<?> out;
    private final InputStream masterInput;
    private final OutputStream masterOutput;
    private final NonBlockingInputStream input;
    private final OutputStream output;
    private final NonBlockingReader reader;
    private final PrintWriter writer;

    private final Object lock = new Object();
    private Thread outputPumpThread;
    private boolean paused = true;
    private ByteArrayOutputStream pauseBuffer = new ByteArrayOutputStream();

    public PosixChannelPtyTerminal(String name, String type, Pty pty, ChannelNG<?> out, Charset encoding) throws IOException {
        this(name, type, pty, out, encoding, SignalHandler.SIG_DFL);
    }

    public PosixChannelPtyTerminal(String name, String type, Pty pty, ChannelNG<?> out, Charset encoding, SignalHandler signalHandler) throws IOException {
        this(name, type, pty, out, encoding, signalHandler, false);
    }

    public PosixChannelPtyTerminal(String name, String type, Pty pty, ChannelNG<?> out, Charset encoding, SignalHandler signalHandler, boolean paused) throws IOException {
        super(name, type, pty, encoding, signalHandler);
        this.out = Objects.requireNonNull(out);
        this.masterInput = pty.getMasterInput();
        this.masterOutput = pty.getMasterOutput();
        this.input = new InputStreamWrapper(NonBlocking.nonBlocking(name, pty.getSlaveInput()));
        this.output = pty.getSlaveOutput();
        this.reader = NonBlocking.nonBlocking(name, input, encoding());
        this.writer = new PrintWriter(new OutputStreamWriter(output, encoding()));
        parseInfoCmp();
        if (!paused) {
            resume();
        }
    }

	public void in(byte[] buf, int off, int len) throws IOException {
        synchronized (lock) {
        	if(paused) {
        		pauseBuffer.write(buf, off, len);
        		return;
        	}
        }
        masterOutput.write(buf,off,len);
        masterOutput.flush();
	}

    public InputStream input() {
        return input;
    }

    public NonBlockingReader reader() {
        return reader;
    }

    public OutputStream output() {
        return output;
    }

    public PrintWriter writer() {
        return writer;
    }

    @Override
    public void close() throws IOException {
        super.close();
        reader.close();
    }

    @Override
    public boolean canPauseResume() {
        return true;
    }

    @Override
    public void pause() {
        synchronized (lock) {
            paused = true;
        }
    }

    @Override
    public void pause(boolean wait) throws InterruptedException {
        Thread p2;
        synchronized (lock) {
            paused = true;
            p2 = outputPumpThread;
        }
        if (p2 != null) {
            p2.interrupt();
        }
        if (p2 !=null) {
            p2.join();
        }
    }

    @Override
    public void resume() {
        synchronized (lock) {
            paused = false;
            if (outputPumpThread == null) {
                outputPumpThread = new Thread(this::pumpOut, toString() + " output pump thread");
                outputPumpThread.setDaemon(true);
                outputPumpThread.start();
            }
            if(pauseBuffer.size() > 0) {
            	try {
	                masterOutput.write(pauseBuffer.toByteArray());
	                pauseBuffer.reset();
	                masterOutput.flush();
            	}
            	catch(IOException ioe) {
            		Log.error("Failed to flush pause buffer.", ioe);
            	}
            }
        }
    }

    @Override
    public boolean paused() {
        synchronized (lock) {
            return paused;
        }
    }

    private class InputStreamWrapper extends NonBlockingInputStream {

        private final NonBlockingInputStream in;
        private final AtomicBoolean closed = new AtomicBoolean();

        protected InputStreamWrapper(NonBlockingInputStream in) {
            this.in = in;
        }

        @Override
        public int read(long timeout, boolean isPeek) throws IOException {
            if (closed.get()) {
                throw new ClosedException();
            }
            return in.read(timeout, isPeek);
        }

        @Override
        public void close() throws IOException {
            closed.set(true);
        }
    }

    private void pumpOut() {
        try {

            byte[] buf = new byte[65536];
            for (;;) {
                synchronized (lock) {
                    if (paused) {
                        outputPumpThread = null;
                        return;
                    }
                }
                int b = masterInput.read(buf);
                if (b < 0) {
                    input.close();
                    break;
                }
                out.sendChannelDataAndBlock(buf, 0, b, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            synchronized (lock) {
                outputPumpThread = null;
            }
        }
        try {
            close();
        } catch (Throwable t) {
            // Ignore
        }
    }

}