package eu.fox7.rexp.isc.analysis2.gen;

import eu.fox7.rexp.regexp.core.Counter;

import java.util.Random;

public class Rand {

	private static final int NOT_SET = -2;
	private int depthLow = NOT_SET;
	private int depthHigh = NOT_SET;
	private int widthLow = NOT_SET;
	private int widthHigh = NOT_SET;
	private int minLow = NOT_SET;
	private int minHigh = NOT_SET;
	private int maxLow = NOT_SET;
	private int maxHigh = NOT_SET;
	private Random random;

	public Rand() {
		random = new Random();
	}

	public Rand(long seed) {
		random = new Random(seed);
	}

	public void setDefaults(int val) {
		setMin(0);
		setMax(val);
		setWidth(val);
		setDepth(val);
	}

	public void setConterLow(int low) {
		setMinLow(low);
		setMaxLow(low);
	}

	public void setConterHigh(int high) {
		setMinHigh(high);
		setMaxHigh(high);
	}
	

	public Rand setDepthLow(int depthLow) {
		this.depthLow = depthLow;
		return this;
	}

	public Rand setDepthHigh(int depthHigh) {
		this.depthHigh = depthHigh;
		return this;
	}

	public Rand setDepth(int depth) {
		this.depthLow = depth;
		this.depthHigh = depth;
		return this;
	}

	public Rand setDepth(int low, int high) {
		this.depthLow = low;
		this.depthHigh = high;
		return this;
	}

	public int getDepth() {
		return select(depthLow, depthHigh);
	}

	public int getDepth(int max) {
		return select(Math.min(depthLow, max), Math.min(depthHigh, max));
	}
	

	public Rand setWidthLow(int widthLow) {
		this.widthLow = widthLow;
		return this;
	}

	public Rand setWidthHigh(int widthHigh) {
		this.widthHigh = widthHigh;
		return this;
	}

	public Rand setWidth(int width) {
		this.widthLow = width;
		this.widthHigh = width;
		return this;
	}

	public Rand setWidth(int low, int high) {
		this.widthLow = low;
		this.widthHigh = high;
		return this;
	}

	public int getWidth() {
		return select(widthLow, widthHigh);
	}
	

	public Rand setMinLow(int minLow) {
		this.minLow = minLow;
		return this;
	}

	public Rand setMinHigh(int minHigh) {
		this.minHigh = minHigh;
		return this;
	}

	public Rand setMin(int min) {
		this.minLow = min;
		this.minHigh = min;
		return this;
	}

	public Rand setMin(int low, int high) {
		this.minLow = low;
		this.minHigh = high;
		return this;
	}

	public int getMin() {
		return select(minLow, minHigh);
	}
	

	public Rand setMaxLow(int maxLow) {
		this.maxLow = maxLow;
		return this;
	}

	public Rand setMaxHigh(int maxHigh) {
		this.maxHigh = maxHigh;
		return this;
	}

	public Rand setMax(int max) {
		this.maxLow = max;
		this.maxHigh = max;
		return this;
	}

	public Rand setMax(int low, int high) {
		this.maxLow = low;
		this.maxHigh = high;
		return this;
	}

	public int getMax() {
		return select(maxLow, maxHigh);
	}
	

	public int select(int high) {
		return random.nextInt(high + 1);
	}

	public int select(int low, int high) {
		int localLow = (low != NOT_SET) ? low : 0;
		int localHigh = (high != NOT_SET) ? high : 0;
		localHigh = localHigh == Counter.INFINITY ? Integer.MAX_VALUE : localHigh;
		if (localLow > localHigh) {
			localHigh = localLow;
		}
		return localLow + random.nextInt(localHigh - localLow + 1);
	}
}
