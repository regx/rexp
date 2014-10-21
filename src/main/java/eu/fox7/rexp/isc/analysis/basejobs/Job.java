package eu.fox7.rexp.isc.analysis.basejobs;

import eu.fox7.rexp.util.Log;

public abstract class Job implements Runnable {
	private volatile boolean runnable;
	private volatile boolean running;

	private Thread thread;

	public Job() {
		this.runnable = true;
		this.running = false;
	}

	protected boolean isRunnable() {
		return runnable;
	}

	protected void setRunnable(boolean runnable) {
		this.runnable = runnable;
	}

	public boolean isRunning() {
		return running;
	}

	public void start() {
		setRunnable(true);
		if (!isRunning()) {
			thread = new Thread(this);
			thread.start();
		}
	}

	public void stop() {
		setRunnable(false);
	}

	@Override
	public void run() {
		running = true;
		onStart();
		work();
		onStop();
		running = false;
	}

	public void join() {
		try {
			if (thread != null) {
				thread.join();
			}
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			Log.f("%s", ex);
		}
	}

	public void execute() {
		start();
		join();
	}

	protected void onStart() {
	}

	protected void onStop() {
	}

	protected abstract void work();

	protected boolean isInterrupted() {
		return (thread != null) ? thread.isInterrupted() : true;
	}

	public void interrupt() {
		if (thread != null) {
			thread.interrupt();
		}
	}
}
