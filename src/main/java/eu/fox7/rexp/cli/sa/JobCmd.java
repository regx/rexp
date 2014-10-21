package eu.fox7.rexp.cli.sa;

import com.beust.jcommander.Parameter;
import eu.fox7.rexp.cli.Cmd;
import eu.fox7.rexp.isc.analysis.basejobs.BatchJob;
import eu.fox7.rexp.isc.analysis.basejobs.BatchJob.Item;
import eu.fox7.rexp.util.Log;

public abstract class JobCmd<T extends BatchJob<? extends Item<?>>> extends Cmd {
	@Parameter(
		names = {"-b", "--begin"},
		description = "Begin asynchronous run",
		required = false
	)
	protected boolean begin;

	@Parameter(
		names = {"-e", "--end"},
		description = "End asynchronous run",
		required = false
	)
	protected boolean end;

	private final T job;

	public JobCmd(T job) {
		this.job = job;
	}

	public T getJob() {
		return job;
	}

	@Override
	public void init() {
		begin = false;
		end = false;
	}

	protected void preExecute() {
	}

	@Override
	public void execute() {
		preExecute();

		if (end) {
			Log.i("Stopping %s", getClass().getSimpleName());
			job.stop();
		} else if (begin) {
			Log.i("Starting %s", getClass().getSimpleName());
			job.start();
		} else {
			job.execute();
		}
	}
}
