package it.pinoelefante.mathematicously.server;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

public class ServerBluetooth extends Server {
	private static ServerBluetooth instance;
	public final static UUID ITU_GENERATED_UUID = UUID.fromString("1e4e2820-5d21-11e4-a2b3-0002a5d5c51b");
	
	private BluetoothServerSocket ssocket;
	private boolean closed;
	private ServerCommunication comm;
	
	public static ServerBluetooth getInstance(){
		if(instance == null)
			instance = new ServerBluetooth();
		return instance;
	}
	
	private ServerBluetooth(){
		super();
		closed = true;
	}
	
	public void run() {
		try {
			BluetoothSocket clientSocket = ssocket.accept();
			ServerCommunication.instance(clientSocket, list);
			comm = ServerCommunication.getInstance();
			comm.setServer(true);
			//ssocket.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void avviaServer() {
		start();
		closed = false;
	}

	@Override
	public void stopServer() {
		getCommunication().disconnect(false);
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
	public void setServerSocket(BluetoothServerSocket s){
		ssocket = s;
	}
}
