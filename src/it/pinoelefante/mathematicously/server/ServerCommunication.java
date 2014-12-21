package it.pinoelefante.mathematicously.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ServerCommunication extends Thread {
	private static ServerCommunication instance;
	private final static int   TIMEOUT_READ = 30000;
	private Socket			 wifi_socket;
	private BluetoothSocket bt_socket;
	private BufferedReader	 in;
	private InputStream		input;
	private OutputStream	   out;
	private PartialListenerContainer listeners;
	private boolean isServer;
	
	private String remoteAddress;
	private int remotePort;

	public static void instance(String address,int port, Map<String, Listener> list){
		if(instance==null){
			instance = new ServerCommunication(address, port, list);
			instance.start();
		}
	}
	public static void instance(Socket sock, PartialListenerContainer list) throws IOException {
		if(instance == null){
    		instance = new ServerCommunication(sock, list);
    		instance.start();
		}
	}
	public static void instance(BluetoothSocket sock, PartialListenerContainer list) throws IOException {
		if(instance == null){
			instance = new ServerCommunication(sock, list.getListeners());
			instance.start();
		}
	}
	public static ServerCommunication getInstance(){
		return instance;
	}
	private ServerCommunication(BluetoothSocket sock, Map<String,Listener> l) throws IOException{
		super();
		bt_socket = sock;
		input = bt_socket.getInputStream();
		in = new BufferedReader(new InputStreamReader(input));
		out = bt_socket.getOutputStream();
		listeners = new PartialListenerContainer();
		listeners.addListener(l);
	}
	private ServerCommunication(String addr, int port, Map<String,Listener> l){
		super();
		remoteAddress = addr;
		remotePort = port;
		listeners = new PartialListenerContainer();
		listeners.addListener(l);
	}
	private ServerCommunication(Socket sock, PartialListenerContainer list) throws IOException {
		super();
		listeners = new PartialListenerContainer();
		listeners.addListener(list.getListeners());
		wifi_socket = sock;
		input = wifi_socket.getInputStream();
		in = new BufferedReader(new InputStreamReader(input));
		out = wifi_socket.getOutputStream();
		wifi_socket.setKeepAlive(true);
		wifi_socket.setSoTimeout(TIMEOUT_READ);
	}
	private int lastWrite = 0;
	private int lastPingReceived = 0;
	public void run() {
		if(wifi_socket==null && bt_socket==null){
			try {
				wifi_socket = new Socket(remoteAddress, remotePort);
				wifi_socket.setKeepAlive(true);
				wifi_socket.setSoTimeout(TIMEOUT_READ);
				input = wifi_socket.getInputStream();
				in = new BufferedReader(new InputStreamReader(input));
				out = wifi_socket.getOutputStream();
			}
			catch (IOException e) {
				e.printStackTrace();
				disconnect(true);
			}
		}
		listeners.eseguiListener("clientConnected");
		while(in!=null){
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
					forceDisconnect();;
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

	private synchronized String read() {
		String r = null;
		try{
			if(wifi_socket==null || wifi_socket.isClosed() || in == null)
				throw new IOException("socket chiuso");
			if (input.available() > 0) {
				r = in.readLine();
				Log.d("CommunicationR", r);
			}
		}
		catch(IOException e){
			e.printStackTrace();
			forceDisconnect();
		}
		return r;
	}
	public synchronized void write(String s){
		try {
			if(wifi_socket==null || wifi_socket.isClosed() || out == null)
				throw new IOException("socket chiuso");
			out.write((s + "\n").getBytes());
			out.flush();
			lastWrite=0;
			Log.d("CommunicationW", s);
		}
		catch (IOException e) {
			e.printStackTrace();
			disconnect(true);
		}
	}
	public void forceDisconnect(){
		disconnect(false);
	}
	public void disconnect(boolean eseguiListener) {
		if(eseguiListener){
			if(wifi_socket!=null && !wifi_socket.isClosed())
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
		
		if (in != null){
			try {
				in.close();
				in = null;
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
		if (out != null){
			try {
				out.close();
				out = null;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (wifi_socket != null){
			try {
				wifi_socket.close();
				wifi_socket = null;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(bt_socket != null){
			try {
				bt_socket.close();
				bt_socket = null;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		instance = null;
	}
	public PartialListenerContainer getListeners(){
		return listeners;
	}
	public void addListeners(Map<String,Listener> l){
		listeners.addListener(l);
	}
	public boolean isServer() {
		return isServer;
	}
	public void setServer(boolean isServer) {
		this.isServer = isServer;
	}
}
