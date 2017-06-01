package sub.fwb;

public class Timer {

	private long start;
	private long stop;

	public void setStart(long millis) {
		start = millis;
	}

	public void setStop(long millis) {
		stop = millis;
	}

	public String getDurationMessage() {
		long duration = stop - start;
		long minutes = duration / 1000 / 60;
		long seconds = (duration - minutes * 60 * 1000) / 1000;
		String potentialZero = seconds < 10 ? "0" : "";

		return "Took " + minutes + ":" + potentialZero + seconds + " minutes (" + duration + " milliseconds)";
	}
}
