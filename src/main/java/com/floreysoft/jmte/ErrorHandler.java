package com.floreysoft.jmte;

import java.util.Map;

import com.floreysoft.jmte.message.ParseException;
import com.floreysoft.jmte.token.Lexer;
import com.floreysoft.jmte.token.Token;

/**
 * Interface used to handle errors while expanding a template.
 */
public interface ErrorHandler {

	/**
	 * Handles an error while interpreting a template in an appropriate way.
	 * This might contain logging the error or even throwing an exception.
	 * 
	 * @param messageKey
	 *            key for the error message
	 * @param token
	 *            the token this error occurred on
	 * @param parameters
	 *            additional parameters to be filled into message or
	 *            <code>null</<code> if you do not have additional parameters
	 */
	public void error(String messageKey, Token token,
			Map<String, Object> parameters) throws ParseException;

	/**
	 * Handles an error while interpreting a template in an appropriate way.
	 * This might contain logging the error or even throwing an exception.
	 * 
	 * @param messageKey
	 *            key for the error message
	 * @param token
	 *            the token this error occurred on
	 */
	public void error(String messageKey, Token token) throws ParseException;

}
