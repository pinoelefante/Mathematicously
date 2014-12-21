package it.pinoelefante.mathematicously.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartialListenerContainer {
	private Map<String, Listener> listeners;
	
	public PartialListenerContainer(){
		listeners = new HashMap<String, Listener>();
	}
	public Listener addListener(String cmd, Listener list){
		return listeners.put(cmd, list);
	}
	public void addListeners(List<String> cmd, List<Listener> ls){
		if(cmd.size()!=ls.size())
			return;
		for(int i=0;i<cmd.size();i++){
			listeners.put(cmd.get(i), ls.get(i));
		}
	}
	public void removeListeners(String... l){
		if(l!=null){
			for(int i=0;i<l.length;i++){
				listeners.remove(l[i]);
			}
		}
	}
	public Listener removeListener(String cmd){
		return listeners.remove(cmd);
	}
	public void eseguiListener(String cmd, String... params){
		Listener l = listeners.get(cmd);
		if(l!=null)
			l.execute(params);
	}
	public Listener getListener(String cmd){
		return listeners.get(cmd);
	}
	public Map<String,Listener> getListeners(){
		return listeners;
	}
	public void addListener(Map<String, Listener> m){
		listeners.putAll(m);
	}
	public void clear(){
		listeners.clear();
	}
}
