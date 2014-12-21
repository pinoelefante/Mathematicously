package it.pinoelefante.mathematicously.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerWiFi extends Server {
	private static ServerWiFi instance;
	
	private ServerSocket ssocket;
	public final static int SERVER_PORT = 8695;
	private boolean closed;
	private ServerCommunication comm;
	
	public static ServerWiFi getInstance(){
		if(instance == null)
			instance = new ServerWiFi();
		return instance;
	}
	
	private ServerWiFi(){
		super();
		closed = true;
	}
	
	public void run() {
		try {
			ssocket = new ServerSocket(SERVER_PORT);
			Socket clientSocket = ssocket.accept();
			ServerCommunication.instance(clientSocket, list);
			comm = ServerCommunication.getInstance();
			comm.setServer(true);
			ssocket.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		closed = true;
	}
	@Override
	public void avviaServer() {
		start();
		closed = false;
	}

	@Override
	public void stopServer() {
		try {
			if(ssocket!=null)
				ssocket.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		if(instance!=null){
			interrupt();
			closed = true;
		}
		instance = null;
	}

	@Override
	public boolean isClosed() {
		return closed;
	}
	public ServerCommunication getCommunication(){
		return comm;
	}
}
