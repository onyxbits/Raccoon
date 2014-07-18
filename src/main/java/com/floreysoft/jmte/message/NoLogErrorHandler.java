package com.floreysoft.jmte.message;

import java.util.Map;

import com.floreysoft.jmte.ErrorHandler;
import com.floreysoft.jmte.token.Token;

public class NoLogErrorHandler extends AbstractErrorHandler implements ErrorHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(String messageKey, Token token, Map<String, Object> parameters) throws ParseException {
		// Silent by design
		Message message = new ResourceBundleMessage(messageKey).withModel(parameters).onToken(token);
		throw new ParseException(message);
	}
}
