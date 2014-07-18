package com.floreysoft.jmte.message;

import java.util.Locale;
import java.util.logging.Logger;

import com.floreysoft.jmte.ErrorHandler;
import com.floreysoft.jmte.token.Token;

public abstract class AbstractErrorHandler implements ErrorHandler {
	protected final Logger logger = Logger
			.getLogger(getClass().getName());

	protected Locale locale = new Locale("en");

	@Override
	public void error(String messageKey, Token token) throws ParseException {
		error(messageKey, token, null);
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}
}
