package com.floreysoft.jmte.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Mini parser for mini DSL stuff. Use it when you do not want
 * <ul>
 * <li>a full parser, as it would be overkill
 * <li>to code by hand it in an ad-hoc way, as you have done this wrong too many
 * times (just think about escaping and quoting)
 * <li>built-in regexp , as it is too slow and you actually do not quite up to
 * it (true for me)
 * </ul>
 * 
 * <p>
 * This is a fast, simple parser that takes input and a single, global hierarchy
 * of operators and spits out "AST" implemented as nested Lists.
 * </p>
 * 
 * <pre>
 * String input = "string(maxLength=10, trim, uppercase)";
 * String[] operators = { "()", ",", "=" }; // in order of precedence, can be pair of start/end or single char separator
 * List<Object> parse = new NestedParser().parse(input, operators);
 * System.out.println(parse);
 * 
 * Output =>
 * 
 * [string, [[maxLength, 10], trim, uppercase]]
 * 
 * TODO: Possible extensions could be
 * <ul>
 * <li>a hierarchy of operators (not a list, but a tree) to parser even more complicated inputs
 * <li>Panic mode calling a user specified callback for further processing when either less or more segments as expected are found in a split
 * </ul>
 * 
 */
public final class NestedParser {
	
	private final static MiniParser MINI_PARSER = MiniParser.rawOutputInstance();
	private final static MiniParser INNER_MINI_PARSER = MiniParser.trimmedInstance();

	public static String access(final List<? extends Object> ast,
			final int index) {
		return access(ast, index, null);
	}

	public static String access(final List<? extends Object> ast,
			final int index, final String defaultValue) {
		if (ast == null) {
			return defaultValue;
		}

		if (index < ast.size()) {
			Object object = ast.get(index);
			if (object instanceof String) {
				return (String) object;
			} else {
				throw new IllegalArgumentException(
						"You can only access scalar strings");
			}
		}
		return defaultValue;
	}

	// TODO create a version that allows for multi character operators
	public List<Object> parse(final String input, final List<String> operators) {
		final List<Object> result = new ArrayList<Object>();
		if (operators.size() != 0) {
			final boolean innerLoop = operators.size() == 1;
			final MiniParser currentParser = innerLoop ? INNER_MINI_PARSER
					: MINI_PARSER;
			final String operator = operators.get(0);
			final List<String> segments;
			if (operator.length() == 1) {
				segments = currentParser.split(input, operator.charAt(0));
			} else if (operator.length() == 2) {

				List<String> allSegments = currentParser.scan(input, String
						.valueOf(operator.charAt(0)), String.valueOf(operator
						.charAt(1)), true);
				if (allSegments.size() > 0) {
					// the first part is not processed any further
					result.add(allSegments.get(0));
				}
				if (allSegments.size() > 1) {
					segments = allSegments.subList(1, allSegments.size());
				} else {
					segments = Collections.emptyList();
				}

			} else {
				throw new IllegalArgumentException(
						"Operators must either be start/end pairs or single characters");

			}
			if (innerLoop) {
				result.addAll(segments);
			} else {
				for (String segment : segments) {
					List<Object> parse = parse(segment, operators.subList(1,
							operators.size()));
					if (parse.size() == 1 && parse.get(0) instanceof String) {
						result.add(parse.get(0));
					} else {
						result.add(parse);
					}
				}
			}
		}
		return result;
	}

	public List<Object> parse(final String input, final String[] operators) {
		return parse(input, Arrays.asList(operators));
	}

}
