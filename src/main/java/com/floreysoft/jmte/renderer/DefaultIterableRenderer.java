package com.floreysoft.jmte.renderer;

import java.util.Iterator;
import java.util.Locale;

import com.floreysoft.jmte.Renderer;
import com.floreysoft.jmte.TemplateContext;


@SuppressWarnings("unchecked")
public class DefaultIterableRenderer implements Renderer<Iterable> {

	@Override
	public String render(Iterable iterable, Locale locale) {
		final String renderedResult;

		final Iterator<?> iterator = iterable.iterator();
		renderedResult = iterator.hasNext() ? iterator.next().toString() : "";
		return renderedResult;

	}

}
