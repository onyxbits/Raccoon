package com.floreysoft.jmte.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for embedded mini languages.
 * 
 * <p>
 * <ul>
 * <li>Solves Demarcation: Where does an embedded language begin and where does
 * it end
 * <ul>
 * <li>Escaping
 * <li>Quotation
 * <li>Graceful reaction to and recovery from invalid input
 * </ul>
 * </li>
 * <li>Lays ground for common patterns of mini langauge processing
 * <ul>
 * <li>all kinds of nested brackets
 * <li>segmentation of data
 * <li>not loosing context
 * <li>context sensitive parsing aka lexer modes/states
 * </ul>
 * </li>
 * </ul>
 * </p>
 * 
 * Not thread safe.
 * 
 * @author olli
 * 
 */
public final class MiniParser {

	public final static char DEFAULT_ESCAPE_CHAR = '\\';
	public final static char DEFAULT_QUOTE_CHAR = '"';

	public static MiniParser defaultInstance() {
		return new MiniParser(DEFAULT_ESCAPE_CHAR, DEFAULT_QUOTE_CHAR, false,
				false, false);
	}

	public static MiniParser trimmedInstance() {
		return new MiniParser(DEFAULT_ESCAPE_CHAR, DEFAULT_QUOTE_CHAR, false,
				true, false);
	}

	public static MiniParser ignoreCaseInstance() {
		return new MiniParser(DEFAULT_ESCAPE_CHAR, DEFAULT_QUOTE_CHAR, true,
				false, false);
	}

	public static MiniParser fullRawInstance() {
		return new MiniParser((char) -1, (char) -1, false, false, true);
	}

	public static MiniParser rawOutputInstance() {
		return new MiniParser(DEFAULT_ESCAPE_CHAR, DEFAULT_QUOTE_CHAR, false,
				false, true);
	}

	private final char escapeChar;
	private final char quoteChar;
	private final boolean ignoreCase;
	private final boolean trim;
	private final boolean rawOutput;

	private transient boolean escaped = false;
	private transient boolean quoted = false;

	public MiniParser(final char escapeChar, final char quoteChar,
			final boolean ignoreCase, final boolean trim,
			final boolean rawOutput) {
		this.escapeChar = escapeChar;
		this.quoteChar = quoteChar;
		this.ignoreCase = ignoreCase;
		this.trim = trim;
		this.rawOutput = rawOutput;
	}

	public String replace(final String input, final String oldString,
			final String newString) {
		try {
			if (oldString == null || oldString.equals("")) {
				return input;
			}
			StringBuilder buffer = new StringBuilder();
			for (int index = 0; index < input.length(); index++) {
				if (input.regionMatches(ignoreCase, index, oldString, 0,
						oldString.length())) {
					buffer.append(newString);
					index += oldString.length() - 1;
				} else {
					char c = input.charAt(index);
					append(buffer, c);
				}
			}

			return buffer.toString();
		} finally {
			escaped = false;
			quoted = false;
		}
	}

	public List<String> split(final String input, final char separator) {
		return split(input, separator, Integer.MAX_VALUE);
	}

	public List<String> split(final String input, final char separator,
			final int maxSegments) {
		return splitInternal(input, false, separator, null, maxSegments);
	}

	public List<String> split(final String input, final String separatorSet) {
		return split(input, separatorSet, Integer.MAX_VALUE);
	}

	public List<String> split(final String input, final String separatorSet,
			final int maxSegments) {
		return splitInternal(input, false, (char) -1, separatorSet, maxSegments);
	}

	public List<String> splitOnWhitespace(final String input,
			final int maxSegments) {
		return splitInternal(input, true, (char) -1, null, maxSegments);
	}

	public List<String> splitOnWhitespace(final String input) {
		return splitOnWhitespace(input, Integer.MAX_VALUE);
	}

	// Common implementation for single char separator and string set separator.
	// Has the benefit of shared code and caliper mini benchmarks showed no
	// measurable performance penalty for additional check which separator to
	// use
	private List<String> splitInternal(final String input,
			final boolean splitOnWhitespace, final char separator,
			final String separatorSet, final int maxSegments) {
		if (input == null) {
			return null;
		}
		try {
			final List<String> segments = new ArrayList<String>();
			StringBuilder buffer = new StringBuilder();

			for (int index = 0; index < input.length(); index++) {
				final char c = input.charAt(index);
				boolean separatedByWhitespace = false;
				if (splitOnWhitespace) {
					for (; index < input.length()
							&& Character.isWhitespace(input.charAt(index)); index++) {
						separatedByWhitespace = true;
					}
					if (separatedByWhitespace) {
						index--;
					}
				}

				final boolean separates = separatedByWhitespace
						|| (separatorSet != null ? separatorSet.indexOf(c) != -1
								: c == separator);
				// in case we are not already in the last segment and there is
				// an
				// unsecaped, unquoted separator, this segment is now done
				if (segments.size() != maxSegments - 1 && separates
						&& !isEscaped()) {
					finish(segments, buffer);
					buffer = new StringBuilder();
				} else {
					append(buffer, c);
				}
			}
			if (!splitOnWhitespace || buffer.length() != 0) {
				finish(segments, buffer);
			}
			return segments;
		} finally {
			escaped = false;
			quoted = false;
		}
	}

	private void finish(final List<String> segments, StringBuilder buffer) {
		String string = buffer.toString();
		segments.add(trim ? string.trim() : string);
	}

	public int lastIndexOf(final String input, final String substring) {
		return indexOfInternal(input, substring, true);
	}

	public int indexOf(final String input, final String substring) {
		return indexOfInternal(input, substring, false);
	}

	private int indexOfInternal(final String input, final String substring,
			boolean last) {
		int resultIndex = -1;
		for (int index = 0; index < input.length(); index++) {
			if (input.regionMatches(ignoreCase, index, substring, 0, substring
					.length())
					&& !isEscaped()) {
				resultIndex = index;
				if (!last) {
					break;
				}
			}
		}
		return resultIndex;

	}

	public List<String> scan(final String input, final String splitStart,
			final String splitEnd) {
		return scan(input, splitStart, splitEnd, false);
	}

	public List<String> greedyScan(final String input, final String splitStart,
			final String splitEnd) {
		return scan(input, splitStart, splitEnd, true);
	}

	public List<String> scan(final String input, final String splitStart,
			final String splitEnd, boolean greedy) {
		if (input == null) {
			return null;
		}
		try {
			final List<String> segments = new ArrayList<String>();
			StringBuilder buffer = new StringBuilder();
			boolean started = false;
			int lastIndexOfEnd = greedy ? lastIndexOfEnd = lastIndexOf(input,
					splitEnd) : -1;
			char c;
			int index = 0;
			while (index < input.length()) {
				c = input.charAt(index);
				final boolean greedyCond = !started || !greedy
						|| index == lastIndexOfEnd;
				final String separator = started ? splitEnd : splitStart;
				if (input.regionMatches(ignoreCase, index, separator, 0,
						separator.length())
						&& !isEscaped() && greedyCond) {
					finish(segments, buffer);
					buffer = new StringBuilder();
					started = !started;
					index += separator.length();
				} else {
					append(buffer, c);
					index++;
				}
			}
			// add trailing element to result
			if (buffer.length() != 0) {
				finish(segments, buffer);
			}
			return segments;
		} finally {
			escaped = false;
			quoted = false;
		}
	}

	public String unescape(final String input) {
		final StringBuilder unescaped = new StringBuilder();
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			append(unescaped, c);
		}
		return unescaped.toString();
	}

	// the heart of it all
	private void append(StringBuilder buffer, char c) {

		// version manually simplified
		// final boolean shouldAppend = rawOutput || escaped
		// || (c != quoteChar && c != escapeChar);
		// final boolean newEscaped = c == escapeChar && !escaped;
		// final boolean newQuoted = (c == quoteChar && !escaped) ? !quoted
		// : quoted;

		// side-effect free version directly extracted from if

		// final boolean shouldAppend = (c == escapeChar && (escaped ||
		// rawOutput))
		// || (c == quoteChar && (escaped || rawOutput))
		// || !(c == quoteChar || c == escapeChar);
		// final boolean newEscaped = c == escapeChar ? !escaped
		// : (c == quoteChar ? false : false);
		// final boolean newQuoted = c == escapeChar ? quoted
		// : (c == quoteChar ? (!escaped ? !quoted : quoted) : quoted);

		// if (shouldAppend) {
		// buffer.append(c);
		// }
		//
		// escaped = newEscaped;
		// quoted = newQuoted;

		// original version
		// XXX needed to revert to this original version as micro benchmark
		// tests
		// showed a slow down of more than 100%
		if (c == escapeChar) {
			if (escaped || rawOutput) {
				buffer.append(c);
			}
			escaped = !escaped;
		} else if (c == quoteChar) {
			if (escaped) {
				buffer.append(c);
				escaped = false;
			} else {
				quoted = !quoted;
				if (rawOutput) {
					buffer.append(c);
				}
			}
		} else {
			buffer.append(c);
			escaped = false;
		}
	}

	private boolean isEscaped() {
		return escaped || quoted;
	}
}
