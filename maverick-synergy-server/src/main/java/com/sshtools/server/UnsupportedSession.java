
package com.sshtools.server;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.sshtools.common.logger.Log;
import com.sshtools.common.ssh.ConnectionAwareTask;
import com.sshtools.common.ssh.SshConnection;

/**
 * This is a basic session that provides a message to the user to inform them
 * that a shell or command cannot be executed because the server does not
 * support an interactive session.
 * 
 * 
 */
public class UnsupportedSession extends SessionChannelNG {

    String message = "This server does not support an interactive session.\r\nGoodbye.\r\n";

    
    public UnsupportedSession(SshConnection con, SshServerContext context) {
    	super(context, con);
    }
    
    protected void processStdinData(ByteBuffer data) {
        // Do nothing
    }

    protected void processStderrData(ByteBuffer data) {
        // Do nothing
    }

    protected void onChannelClosed() {
        // Do nothing
    }

    protected boolean executeCommand(String cmd) {
        return false;
    }

    protected void changeWindowDimensions(int cols, int rows, int width, int height) {
        // Do nothing
    }

    public void onSessionOpen() {
        // Do nothing
    }

    protected void onLocalEOF() {
        // The local side is EOF no more data can be sent
    }

    protected boolean startShell() {
    	

		con.executeTask(new ConnectionAwareTask(con) {
			
			@Override
			protected void doTask() {
				try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
                try {
					sendChannelDataAndBlock(message.getBytes());
					 close();
				} catch (IOException e) {
					Log.error("Channel I/O error", e);
				}
			}
		});
    	
 
        return true;
    }

    protected boolean allocatePseudoTerminal(String parm1, int parm2, int parm3, int parm4, int parm5, byte[] parm6) {
        return true;
    }

    protected void processSignal(String signal) {
        // Do Nothing
    }

    protected void onRemoteEOF() {
        // The remote side is EOF no more data will be received
    }

    protected boolean setEnvironmentVariable(String name, String value) {
        return true;
    }
}
