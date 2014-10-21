package eu.fox7.rexp.util;

import eu.fox7.rexp.isc.analysis.util.PropertiesManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;

public class Log {
	private static final String PROP_USE_LOG_COLOR = "env.log.color";

	public static void main(String[] args) {
		configureRootLogger(Level.ALL);
		System.out.println("\u001B[0;36mTest colors\u001B[0m");
		Log.e("a");
		Log.w("a");
		Log.i("a");
		Log.d("a");
		Log.v("a");
		System.out.println("Normal sysout");
	}

	private static final Logger logger = Logger.getLogger(Log.class.toString());

	public static void i(String s, Object... args) {
		s = args.length > 0 ? String.format(s, args) : s;
		logger.log(Level.INFO, s);
	}

	public static void w(String s, Object... args) {
		s = args.length > 0 ? String.format(s, args) : s;
		logger.log(Level.WARNING, s);
	}

	public static void e(String s, Object... args) {
		s = args.length > 0 ? String.format(s, args) : s;
		logger.log(Level.SEVERE, s);
	}

	public static void d(String s, Object... args) {
		s = args.length > 0 ? String.format(s, args) : s;
		logger.log(Level.FINE, s);
	}

	public static void v(String s, Object... args) {
		s = args.length > 0 ? String.format(s, args) : s;
		logger.log(Level.FINER, s);
	}

	public static void f(String s, Object... args) {
		s = args.length > 0 ? String.format(s, args) : s;
		logger.log(Level.FINEST, s);
	}

	public static void configureRootLogger(Level level) {
		boolean useColor = PropertiesManager.getBoolean(PROP_USE_LOG_COLOR);
		configureRootLogger(level, useColor);
	}

	public static void configureRootLogger(Level level, boolean useColor) {
		LogManager lm = LogManager.getLogManager();
		Logger rootLogger = lm.getLogger("");
		rootLogger.setLevel(level);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(level);
		if (useColor) {
			handler.setFormatter(new ColoredLogFormatter());
		} else {
			handler.setFormatter(new CustomLogFormatter());
		}
		for (Handler h : rootLogger.getHandlers()) {
			rootLogger.removeHandler(h);
		}
		rootLogger.addHandler(handler);
	}

	public static Level getLoggerLevel() {
		LogManager lm = LogManager.getLogManager();
		Logger rootLogger = lm.getLogger("");
		return rootLogger.getLevel();
	}
}

class CustomLogFormatter extends Formatter {
	private static final int SIZE = 1000;
	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

	@Override
	public String format(LogRecord record) {
		StringBuilder builder = new StringBuilder(SIZE);
		buildString(builder, record);
		builder.append("\n");
		return builder.toString();
	}

	protected void buildString(StringBuilder builder, LogRecord record) {
		builder.append("[").append(record.getLevel()).append("] ");
		builder.append(formatMessage(record));
	}

	protected void buildStringComplex(StringBuilder builder, LogRecord record) {
		builder.append(df.format(new Date(record.getMillis()))).append(" - ");
		builder.append("[").append(record.getSourceClassName()).append(".");
		builder.append(record.getSourceMethodName()).append("] - ");
		builder.append("[").append(record.getLevel()).append("] - ");
		builder.append(formatMessage(record));
	}
}

class ColoredLogFormatter extends CustomLogFormatter {
	public static final String SANE = "\u001B[0m";
	public static final String RED = "\u001B[0;31m";
	public static final String GREEN = "\u001B[0;32m";
	public static final String YELLOW = "\u001B[0;33m";
	public static final String BLUE = "\u001B[0;34m";
	public static final String MAGENTA = "\u001B[0;35m";
	public static final String CYAN = "\u001B[0;36m";
	public static final String WHITE = "\u001B[0;37m";

	public static final String DARK_BLACK = "\u001B[1;30m";
	public static final String DARK_RED = "\u001B[1;31m";
	public static final String DARK_GREEN = "\u001B[1;32m";
	public static final String DARK_YELLOW = "\u001B[1;33m";
	public static final String DARK_BLUE = "\u001B[1;34m";
	public static final String DARK_MAGENTA = "\u001B[1;35m";
	public static final String DARK_CYAN = "\u001B[1;36m";
	public static final String DARK_WHITE = "\u001B[1;37m";

	public static final String BACKGROUND_BLACK = "\u001B[40m";
	public static final String BACKGROUND_RED = "\u001B[41m";
	public static final String BACKGROUND_GREEN = "\u001B[42m";
	public static final String BACKGROUND_YELLOW = "\u001B[43m";
	public static final String BACKGROUND_BLUE = "\u001B[44m";
	public static final String BACKGROUND_MAGENTA = "\u001B[45m";
	public static final String BACKGROUND_CYAN = "\u001B[46m";
	public static final String BACKGROUND_WHITE = "\u001B[47m";

	private static final Map<Level, String> LOG_LEVEL_COLORS;

	static {
		LOG_LEVEL_COLORS = new HashMap<Level, String>();
		LOG_LEVEL_COLORS.put(Level.FINEST, CYAN);
		LOG_LEVEL_COLORS.put(Level.FINER, CYAN);
		LOG_LEVEL_COLORS.put(Level.FINE, BLUE);
		LOG_LEVEL_COLORS.put(Level.INFO, BACKGROUND_BLACK);
		LOG_LEVEL_COLORS.put(Level.WARNING, MAGENTA);
		LOG_LEVEL_COLORS.put(Level.SEVERE, RED);
	}

	@Override
	protected void buildString(StringBuilder builder, LogRecord record) {
		String colorStr = LOG_LEVEL_COLORS.get(record.getLevel());
		builder.append(colorStr != null ? colorStr : DARK_BLACK);
		super.buildString(builder, record);
		builder.append(SANE);
	}
}
