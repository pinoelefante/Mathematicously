package it.pinoelefante.mathematicously.server;

import java.util.Map;

public abstract class Server extends Thread {
	protected PartialListenerContainer list;
	public Server(){
		list = new PartialListenerContainer();
	}
	public abstract void avviaServer();
	public abstract void stopServer();
	public abstract boolean isClosed();
	public abstract ServerCommunication getCommunication();
	public void addConnectionListener(PartialListenerContainer l){
		list = l;
	}
	public void addListeners(Map<String, Listener> l){
		list.addListener(l);
	}
}
