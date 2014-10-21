package eu.fox7.rexp.isc.experiment4;

import java.util.concurrent.Callable;

public class AllBench {
	public static void main(String[] args) throws Exception {
		String[] defArgs = new String[]{};
		final String[] fargs = args.length == 0 ? defArgs : args;
		boolean[] execVector = {false, false, true, true, true, true, false,};

		execute(
			execVector[6],
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					ScratchEval0.main(fargs);
					return null;
				}
			}
		);
		execute(
			execVector[0],
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					ScratchEval.main(fargs);
					return null;
				}
			}
		);
		execute(
			execVector[1],
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					ScratchEval2.main(fargs);
					return null;
				}
			}
		);

		execute(
			execVector[2],
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					IncEval.main(fargs);
					return null;
				}
			}
		);
		execute(
			execVector[3],
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					IncEval2.main(fargs);
					return null;
				}
			}
		);
		execute(
			execVector[4],
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					IncEval3.main(fargs);
					return null;
				}
			}
		);
		execute(
			execVector[5],
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					IncEval5.main(fargs);
					return null;
				}
			}
		);
	}

	public static void execute(boolean fork, boolean flag, Callable<Void> c) {
		if (flag) {
			try {
				c.call();
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
			}
		}
	}

	public static void execute(boolean flag, Callable<Void> c) {
		execute(false, flag, c);
	}

	public static void execute(Callable<Void> c) {
		execute(false, c);
	}
}
