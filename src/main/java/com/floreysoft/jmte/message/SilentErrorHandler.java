package com.floreysoft.jmte.message;

import java.util.Map;

import com.floreysoft.jmte.ErrorHandler;
import com.floreysoft.jmte.token.Token;

public class SilentErrorHandler extends AbstractErrorHandler implements ErrorHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(String messageKey, Token token, Map<String, Object> parameters) throws ParseException {
		// Silent by design
	}
}
