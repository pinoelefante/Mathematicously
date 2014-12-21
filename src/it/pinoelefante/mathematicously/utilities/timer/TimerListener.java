package it.pinoelefante.mathematicously.utilities.timer;

public interface TimerListener {
	public void onScheduledPauseEnd();

	public void onTimerEnd();
	
	public void onTimerSleepEnd();
	
	public void onTimerInterrupt();
}
