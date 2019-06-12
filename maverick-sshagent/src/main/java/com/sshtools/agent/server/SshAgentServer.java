package com.maverick.agent.server;

import java.io.File;
import java.io.IOException;

import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import com.maverick.agent.InMemoryKeyStore;
import com.maverick.agent.KeyStore;
import com.maverick.agent.openssh.OpenSSHConnectionFactory;

public class SshAgentServer {

	KeyStore keystore;
	SshAgentConnectionFactory connectionFactory;
	SshAgentAcceptor acceptor;
	
	public SshAgentServer(SshAgentConnectionFactory connectionFactory) {
		this(connectionFactory, new InMemoryKeyStore());
	}
	
	public SshAgentServer(SshAgentConnectionFactory connectionFactory, KeyStore keystore) {
		this.connectionFactory = connectionFactory;
		this.keystore = keystore;
	}
	
	public void startListener(SshAgentAcceptor acceptor) throws IOException {
		
		this.acceptor = acceptor;
		ServerThread t = new ServerThread(acceptor);
		t.start();
	}
	
	public void startUnixSocketListener(String location) throws IOException{
		
		File socketFile = new File(location);
		AFUNIXServerSocket server = AFUNIXServerSocket.newInstance(); 
		server.bind(new AFUNIXSocketAddress(socketFile));
		
		ServerThread t = new ServerThread(acceptor = new UnixSocketAdapter(server));
		t.start();
	}
	
	public void close() throws IOException {
		if(acceptor!=null) {
			acceptor.close();
		}
	}
	
	class ServerThread extends Thread {

		SshAgentAcceptor socket;
		public ServerThread(SshAgentAcceptor socket) {
			super("Agent-Server-Thread");
			setDaemon(true);
			this.socket = socket;
		}
		
		public void run() {
			SshAgentTransport sock;
			try {
				while((sock = socket.accept())!=null) {
					
					SshAgentConnection c = connectionFactory.createConnection(keystore, 
							sock.getInputStream(), sock.getOutputStream(), sock);
					Thread t = new Thread(c);
					t.start();
				}
			} catch (IOException e) {
				try {
					socket.close();
				} catch (IOException e1) {
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		new SshAgentServer(new OpenSSHConnectionFactory()).startUnixSocketListener("/private/tmp/com.sshtools.agent");
	}
 }