package it.pinoelefante.mathematicously.utilities.timer;

public class Timer {
	private boolean pause, scaduto, pause_listener, started, interrupted;
	private int	 totale;
	private int	 time_pause;
	private static int threadN;

	public Timer(int time) {
		totale = time * 1000;
	}

	public void togglePause() {
		pause = !pause;
	}

	public boolean isPaused() {
		return pause;
	}

	public boolean isExpired() {
		return scaduto;
	}

	private Thread timer;

	public void avviaTimer() {
		class timer_start extends Thread {
			public timer_start(String n){
				super(n);
			}
			@Override
			public void interrupt() {
				super.interrupt();
				interrupted = true;
				onTimerInterrupt();
			};
			public void run() {
				pause = false;
				scaduto = false;
				time_pause = 0;
				started = true;
				while (totale > 0) {
					try {
						sleep(100);
						onSleepEnd();
					}
					catch (InterruptedException e) {
						interrupt();
						return;
					}
					if (pause) {
						if (pause_listener) {
							if (time_pause > 0)
								time_pause -= 100;
							else
								pause = false;
						}
						continue;
					}
					if (pause_listener) {
						pauseListenerEnd();
					}
					totale -= 100;
				}
				scaduto = true;
				pause = true;
				if(!interrupted)
					onTimerEnd();
			}
		}
		if (timer != null && timer.isAlive()) {
			timer.interrupt();
		}
		timer = new timer_start("timer_"+(threadN++));
		timer.start();
	}

	public void aggiungiTempo(int secondi) {
		totale += (secondi * 1000);
	}

	public void setPause(boolean p) {
		pause = p;
	}

	public int getCurrentTime() {
		return totale;
	}
	public float getCurrentTimeF(){
		return ((float)totale)/1000;
	}

	public void setPause(int time) {
		pause = true;
		pause_listener = true;
		time_pause = time;
	}

	private TimerListener list;

	private void pauseListenerEnd() {
		pause_listener = false;
		if (list != null)
			list.onScheduledPauseEnd();
	}

	private void onTimerEnd() {
		if(list!=null)
			list.onTimerEnd();
	}

	public void addPauseListener(TimerListener p) {
		list = p;
	}

	public void setTime(int secondi, boolean startIfExpired) {
		totale = secondi * 1000;
		if (isExpired() && startIfExpired) {
			avviaTimer();
		}
	}

	public boolean isStarted() {
		return started;
	}

	public void stop() {
		if (timer != null && timer.isAlive()){
			timer.interrupt();
			interrupted = true;
		}
		interrupted = true;
	}
	private void onSleepEnd(){
		if(list!=null)
			list.onTimerSleepEnd();
	}
	private void onTimerInterrupt(){
		if(list!=null)
			list.onTimerInterrupt();
	}
}
