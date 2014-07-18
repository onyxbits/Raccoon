package com.floreysoft.jmte.message;

import java.util.Map;

import com.floreysoft.jmte.ErrorHandler;
import com.floreysoft.jmte.token.Token;

public class ProductionErrorHandler extends AbstractErrorHandler implements
		ErrorHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(String messageKey, Token token,
			Map<String, Object> parameters) throws ParseException {
		Message message = new ResourceBundleMessage(messageKey).withModel(
				parameters).onToken(token);
		logger.severe(message.format(locale));
	}
}
