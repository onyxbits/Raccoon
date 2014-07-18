package com.floreysoft.jmte.token;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.floreysoft.jmte.TemplateContext;
import com.floreysoft.jmte.util.Util;

public abstract class ExpressionToken extends AbstractToken {

	public final static String segmentsToString(List<String> segments,
			int start, int end) {
		if (start >= segments.size() || end > segments.size()) {
			throw new IllegalArgumentException("Range is not inside segments");
		}
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < end; i++) {
			String segment = segments.get(i);
			builder.append(segment);
			if (i < end - 1) {
				builder.append(".");
			}
		}
		return builder.toString();
	}

	private List<String> segments;
	private String expression;

	public ExpressionToken(String expression) {
		if (expression == null) {
			throw new IllegalArgumentException(
					"Parameter expression must not be null");
		}
		this.setExpression(expression);
	}

	protected ExpressionToken(List<String> segments, String expression) {
		this.segments = segments;
		this.expression = expression;
	}

	public boolean isComposed() {
		return getSegments().size() > 1;
	}

	public boolean isEmpty() {
		return getSegments().size() == 0;
	}

	public List<String> getSegments() {
		return segments;
	}

	public String getFirstSegment() {
		if (isEmpty()) {
			throw new IllegalStateException("There is no first segment");
		}

		return getSegments().get(0);
	}

	public String getLastSegment() {
		if (isEmpty()) {
			throw new IllegalStateException("There is no last segment");
		}

		return getSegments().get(getSegments().size() - 1);
	}

	public String getAllButLastSegment() {
		if (isEmpty()) {
			throw new IllegalStateException("There are no segments");
		}

		return segmentsToString(segments, 0, getSegments().size() - 1);
	}

	public String getAllButFirstSegment() {
		if (isEmpty()) {
			throw new IllegalStateException("There are no segments");
		}

		return segmentsToString(segments, 1, getSegments().size());
	}

	public void setExpression(String expression) {
		this.text = null;
		this.segments = Util.MINI_PARSER.split(expression, '.');
		this.expression = Util.MINI_PARSER.unescape(expression);
	}

	public String getExpression() {
		return expression;
	}

	public void setSegments(List<String> segments) {
		this.segments = segments;
		this.expression = segmentsToString(segments, 0, segments.size());
		this.text = null;
	}

	@Override
	public abstract Object evaluate(TemplateContext context);

	protected Object evaluatePlain(TemplateContext context) {
		final Object value = context.modelAdaptor.getValue(context, this, getSegments(), getExpression());
		return value;
	}

}
