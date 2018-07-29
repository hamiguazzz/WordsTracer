package hamiguazzz.utils;

public final class StopWatch {

	private String name;
	private long timeStart;
	private StopWatchState state;
	private long lastPause;
	private long timeEnd;
	private long sleepInterval;

	public StopWatch() {
		this("");
	}

	public StopWatch(String name) {
		this.name = name;
		this.state = StopWatchState.NOT_START;
	}

	public void start() {
		if (state != StopWatchState.NOT_START) throw new IllegalStateException("can't start");
		timeStart = now();
		state = StopWatchState.RUNNING;
	}

	public void stop() {
		if (state != StopWatchState.RUNNING)
			throw new IllegalStateException(name + " not running");
		timeEnd = now();
		state = StopWatchState.END;
	}

	public void reStart() {
		state = StopWatchState.RUNNING;
		timeEnd = 0;
		sleepInterval = 0;
		lastPause = 0;
		timeStart = now();
	}

	public void pause() {
		if (state != StopWatchState.RUNNING) throw new IllegalStateException(name + " must stop first");
		lastPause = now();
		state = StopWatchState.PAUSED;
	}

	public void resume() {
		if (state != StopWatchState.PAUSED)
			throw new IllegalStateException(name + " was not paused");
		sleepInterval += now() - lastPause;
		state = StopWatchState.RUNNING;
	}

	public static final long CONVERT_TO_MS = 1000000;
	private static final long EXTRA_COST = 0;

	private long now() {
		return System.nanoTime();
	}

	public long getInterval() {
		switch (state) {
			case RUNNING:
			case PAUSED:
				return (now() - timeStart - getSleepInterval() - EXTRA_COST) / CONVERT_TO_MS;
			case END:
				return (timeEnd - timeStart - getSleepInterval() - EXTRA_COST) / CONVERT_TO_MS;
			case NOT_START:
				return 0;
			default:
				throw new IllegalStateException(state.name());
		}
	}

	public long getSleepInterval() {
		switch (state) {
			case NOT_START:
			case RUNNING:
			case END:
				return sleepInterval / CONVERT_TO_MS;
			case PAUSED:
				return (sleepInterval + now() - lastPause) / CONVERT_TO_MS;
			default:
				throw new IllegalStateException(state.name());
		}
	}

	@Override
	public String toString() {
		return (name.equals("") ? "" : name + "|") + getInterval() + "ms";
	}

	enum StopWatchState {
		NOT_START, PAUSED, RUNNING, END
	}
}
