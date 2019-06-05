package com.sshtools.client;

import java.io.IOException;

import com.sshtools.common.nio.WriteOperationRequest;
import com.sshtools.common.ssh.ChannelNG;
import com.sshtools.common.ssh.ChannelOpenException;
import com.sshtools.common.ssh.ChannelRequestFuture;
import com.sshtools.common.ssh.RequestFuture;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.util.ByteArrayReader;
import com.sshtools.common.util.ByteArrayWriter;

/**
 * Implements the client side of the SSH Connection protocol session channel
 */
public abstract  class AbstractSessionChannel extends ChannelNG<SshClientContext> {

	public static final int EXITCODE_NOT_RECEIVED = Integer.MIN_VALUE;
	public static final int SSH_EXTENDED_DATA_STDERR = 1;

	int exitcode = EXITCODE_NOT_RECEIVED;
	String exitsignalinfo;
	boolean flowControlEnabled;


	public AbstractSessionChannel(SshConnection con, int maximumPacketSize, int initialWindowSize, int maximumWindowSpace,
			int minimumWindowSpace) {
		super("session", con, maximumPacketSize, initialWindowSize, maximumWindowSpace, minimumWindowSpace,
				new ChannelRequestFuture());
	}

	public AbstractSessionChannel(SshConnection con, int maximumPacketSize, int initialWindowSize, int maximumWindowSpace,
			int minimumWindowSpace, ChannelRequestFuture closeFuture) {
		super("session", con, maximumPacketSize, initialWindowSize, maximumWindowSpace, minimumWindowSpace, closeFuture);
	}

	@Override
	protected void onChannelFree() {

	}

	@Override
	protected byte[] createChannel() throws IOException {
		return null;
	}

	@Override
	protected byte[] openChannel(byte[] requestdata) throws WriteOperationRequest, ChannelOpenException {
		return null;
	}

	@Override
	protected void onChannelOpenConfirmation() {

	}

	@Override
	protected void onChannelClosed() {

	}

	@Override
	protected void onChannelOpen() {

	}

	public RequestFuture allocatePseudoTerminal(String type, int cols, int rows) {
		return allocatePseudoTerminal(type, cols, rows, 0, 0, null);
	}

	public RequestFuture allocatePseudoTerminal(String type, int cols, int rows, PseudoTerminalModes modes) {
		return allocatePseudoTerminal(type, cols, rows, 0, 0, modes);
	}

	public void changeTerminalDimensions(int cols, int rows, int width, int height) {
		ByteArrayWriter request = new ByteArrayWriter();

		try {

			request.writeInt(cols);
			request.writeInt(rows);
			request.writeInt(width);
			request.writeInt(height);

			sendChannelRequest("window-change", false, request.toByteArray());

		} catch (IOException ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		} finally {
			try {
				request.close();
			} catch (IOException e) {
			}
		}
	}

	public RequestFuture allocatePseudoTerminal(String type, int cols, int rows, int width, int height,
			PseudoTerminalModes modes) {

		ByteArrayWriter request = new ByteArrayWriter();

		try {

			request.writeString(type);
			request.writeInt(cols);
			request.writeInt(rows);
			request.writeInt(width);
			request.writeInt(height);
			if (modes == null) {
				request.writeInt(0);
			} else {
				request.writeBinaryString(modes.toByteArray());
			}

			ChannelRequestFuture future = new ChannelRequestFuture();
			sendChannelRequest("pty-req", true, request.toByteArray(), future);
			return future;
		} catch (IOException ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		} finally {
			try {
				request.close();
			} catch (IOException e) {
			}
		}
	}

	public RequestFuture startShell() {
		ChannelRequestFuture future = new ChannelRequestFuture();
		sendChannelRequest("shell", true, null, future);
		return future;
	}

	public RequestFuture executeCommand(String cmd) throws SshException {

		ByteArrayWriter request = new ByteArrayWriter();

		try {
			request.writeString(cmd);
			ChannelRequestFuture future = new ChannelRequestFuture();
			sendChannelRequest("exec", true,  request.toByteArray(), future);
			return future;
		} catch (IOException ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		} finally {
			try {
				request.close();
			} catch (IOException e) {
			}
		}
	}

	public RequestFuture executeCommand(String cmd, String charset) {

		ByteArrayWriter request = new ByteArrayWriter();

		try {

			request.writeString(cmd, charset);
			ChannelRequestFuture future = new ChannelRequestFuture();
			sendChannelRequest("exec", true,  request.toByteArray(), future);
			return future;

		} catch (IOException ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		} finally {
			try {
				request.close();
			} catch (IOException e) {
			}
		}
	}

	public RequestFuture startSubsystem(String subsystem) {

		ByteArrayWriter request = new ByteArrayWriter();

		try {

			request.writeString(subsystem, "UTF-8");
			ChannelRequestFuture future = new ChannelRequestFuture();
			sendChannelRequest("subsystem", true,  request.toByteArray(), future);
			return future;

		} catch (IOException ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		} finally {
			try {
				request.close();
			} catch (IOException e) {
			}
		}

	}

	@Override
	protected void onChannelClosing() {

	}

	@Override
	protected void onChannelRequest(String requesttype, boolean wantreply, byte[] requestdata) {

		try {
			if (requesttype.equals("exit-status")) {
				if (requestdata != null) {
					exitcode = (int) ByteArrayReader.readInt(requestdata, 0);
				}
			}

			if (requesttype.equals("exit-signal")) {

				if (requestdata != null) {
					ByteArrayReader bar = new ByteArrayReader(requestdata, 0, requestdata.length);
					try {
						exitsignalinfo = "Signal=" + bar.readString() + " CoreDump=" + String.valueOf(bar.read() != 0)
								+ " Message=" + bar.readString();
					} finally {
						bar.close();
					}
				}

			}

			if (requesttype.equals("xon-xoff")) {
				flowControlEnabled = (requestdata != null && requestdata[0] != 0);
			}
		} catch (IOException ex) {
			throw new IllegalStateException("Unexpected I/O error reading channel request", ex);
		}
	}

	public int getExitCode() {
		return exitcode;
	}

	public boolean isFlowControlEnabled() {
		return flowControlEnabled;
	}

	public String getExitSignalInfo() {
		return exitsignalinfo;
	}

	@Override
	protected void onRemoteEOF() {

	}

	@Override
	protected void onLocalEOF() {

	}

	public int getMaximumRemotePacketLength() {
		return getRemotePacket();
	}

	public int getMaximumLocalPacketLength() {
		return getLocalPacket();
	}
}
