package com.floreysoft.jmte.util;

/**
 * Pairs of begin/end.
 * 
 */
public final class StartEndPair {
	public final int start;
	public final int end;

	public StartEndPair(final int start, final int end) {
		this.start = start;
		this.end = end;
	}

	@Override
	public String toString() {
		return "" + start + "-" + end;
	}
}