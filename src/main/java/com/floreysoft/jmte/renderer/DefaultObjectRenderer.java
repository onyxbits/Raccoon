package com.floreysoft.jmte.renderer;

import java.util.List;
import java.util.Locale;

import com.floreysoft.jmte.Renderer;
import com.floreysoft.jmte.TemplateContext;
import com.floreysoft.jmte.util.Util;

public class DefaultObjectRenderer implements Renderer<Object> {

	@Override
	public String render(Object value, Locale locale) {
		final String renderedResult;

		if (value instanceof String) {
			renderedResult = (String) value;
		} else {
			final List<Object> arrayAsList = Util.arrayAsList(value);
			if (arrayAsList != null) {
				renderedResult = arrayAsList.size() > 0 ? arrayAsList.get(0)
						.toString() : "";
			} else {
				renderedResult = value.toString();
			}
		}
		return renderedResult;
	}
}
