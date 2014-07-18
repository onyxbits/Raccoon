package com.floreysoft.jmte.message;

import java.util.Map;

import com.floreysoft.jmte.ErrorHandler;
import com.floreysoft.jmte.token.Token;

public class InternalErrorHandler extends AbstractErrorHandler implements
		ErrorHandler {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(String messageKey, Token token,
			Map<String, Object> parameters) throws ParseException {
		logger.warning(String.format(
				"Internal error '%s' on '%s'(%d:%d) with parameters %s",
				messageKey, token.getText(), token.getLine(),
				token.getColumn(), parameters != null ? parameters.toString()
						: ""));
	}

}
