package com.floreysoft.jmte.token;

import com.floreysoft.jmte.TemplateContext;

public class PlainTextToken extends AbstractToken {
	public PlainTextToken(String text) {
		setText(text);
	}

	@Override
	public Object evaluate(TemplateContext context) {
		return getText();
	}

	@Override
	public String emit() {
		return getText();
	}
}
