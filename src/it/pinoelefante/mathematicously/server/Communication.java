package it.pinoelefante.mathematicously.server;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.util.Log;

public class Communication extends Thread {
	private static Communication instance;
	private InputStream input;
	private BufferedReader	 in_buffered;
	private OutputStream output;
	private Closeable socket;
	private PartialListenerContainer listeners;
	private long lastWrite = 0, lastPingReceived = 0;
	private final static int   TIMEOUT_READ = 30000;
	private boolean isServer;
	private Communication(Closeable sock, InputStream i, OutputStream o, PartialListenerContainer list){
		socket = sock;
		input = i;
		in_buffered = new BufferedReader(new InputStreamReader(input));
		output = o;
		listeners = list;
		instance = this;
	}
	public static Communication getInstance(){
		return instance;
	}
	public static Communication instance(Closeable sock, InputStream i, OutputStream o, PartialListenerContainer list){
		instance = new Communication(sock, i, o, list);
		return instance;
	}
	public void run(){
		listeners.eseguiListener("clientConnected");
		while(in_buffered!=null){
			try {
				String line = read();
				if(line!=null){
					lastPingReceived=0;
					gestisciComando(line.trim());
				}
				else
					lastPingReceived+=100;
				Thread.sleep(100L);
				lastWrite+=100;
				if(lastWrite>=10000){
					write("ping");
				}
				if(lastPingReceived>=TIMEOUT_READ){
					forceDisconnect();
					break;
				}
			}
			catch (IOException | InterruptedException e) {
				e.printStackTrace();
				disconnect(true);
				return;
			}
		}
		forceDisconnect();
		listeners.clear();
		listeners = null;
	}
	public synchronized void write(String s){
		try {
			if(output == null)
				throw new IOException("socket chiuso");
			output.write((s + "\n").getBytes());
			output.flush();
			lastWrite=0;
			Log.d("CommunicationW", s);
		}
		catch (IOException e) {
			e.printStackTrace();
			disconnect(true);
		}
	}
	private synchronized String read() {
		String r = null;
		try{
			if(in_buffered==null || input==null)
				throw new IOException("socket chiuso");
			if (input.available() > 0) {
				r = in_buffered.readLine();
				Log.d("CommunicationR", r);
			}
		}
		catch(IOException e){
			e.printStackTrace();
			forceDisconnect();
		}
		return r;
	}
	public void forceDisconnect(){
		disconnect(false);
	}
	public void disconnect(boolean eseguiListener) {
		System.err.println("ESEGUO DISCONNESSIONE SOCKET");
		if(eseguiListener){
			if(socket!=null)
				write("disconnect");
			if(listeners!=null)
				listeners.eseguiListener("clientDisconnected");
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		
		interrupt();
		
		if (in_buffered != null){
			try {
				in_buffered.close();
				in_buffered = null;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (input != null){
			try {
				input.close();
				input = null;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (output != null){
			try {
				output.close();
				output = null;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (socket != null){
			try {
				socket.close();
				socket = null;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		instance = null;
	}
	public boolean isServer() {
		return isServer;
	}
	public void setServer(boolean isServer) {
		this.isServer = isServer;
	}
	private void gestisciComando(String l) throws IOException{
		listeners.eseguiListener("commandArrived", l);
		
		String[] cmd = l.split(" ");
		switch(cmd[0]){
			case "ping":
				lastPingReceived = 0;
				break;
			case "sceltaGioco":{
				listeners.eseguiListener("sceltaGioco", cmd[1], cmd[2]);
				break;
			}
			case "getDomanda":{
				listeners.eseguiListener("getDomanda", cmd[1]);
				break;
			}
			case "domandaResponse":{
				listeners.eseguiListener("domandaResponse", l);
				break;
			}
			case "wait":{
				listeners.eseguiListener("wait");
				break;
			}
			case "finishRequest":{
				listeners.eseguiListener("finishRequest", l);
				break;
			}
			case "finishResponse":{
				listeners.eseguiListener("finishResponse", l);
				break;
			}
			case "requestIsOver":{
				listeners.eseguiListener("requestIsOver");
				break;
			}
			case "responseIsOver": {
				listeners.eseguiListener("responseIsOver", l);
				break;
			}
			case "disconnect":
				forceDisconnect();
				listeners.eseguiListener("clientDisconnected");
				break;
			case "aftergameRequestIsIn": {
				listeners.eseguiListener("aftergameRequestIsIn");
				break;
			}
			case "aftergameResponseIsIn": {
				listeners.eseguiListener("aftergameResponseIsIn", cmd[1]);
				break;
			}
			case "continue":{
				listeners.eseguiListener("aftergameOptionSelected", cmd[1]);
				break;
			}
		}
	}
}
