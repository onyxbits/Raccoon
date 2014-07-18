package com.floreysoft.jmte.token;

import com.floreysoft.jmte.TemplateContext;

public class InvalidToken extends AbstractToken {
	public Object evaluate(TemplateContext context) {
		context.engine.getErrorHandler().error("invalid-expression", this);
		return "";
	}
}
