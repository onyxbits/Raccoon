package com.floreysoft.jmte;

import com.floreysoft.jmte.token.Token;

/**
 * Callback for execution steps of the engine. Reports in interpreted mode only.
 * 
 */
public interface ProcessListener {
	public static enum Action {
		/**
		 * Expression being executed.
		 */
		EVAL,
		/**
		 * Expression being skipped.
		 */
		SKIP,
		/**
		 * End of control structure.
		 */
		END
	}

	/**
	 * Reports a step executed by the engine
	 * 
	 * @param context
	 *            current context during template evaluation
	 * @param token
	 *            the token that is handled
	 * @param action
	 *            the action that is executed on the action
	 */
	void log(TemplateContext context, Token token, Action action);
}
