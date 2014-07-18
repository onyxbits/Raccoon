package com.floreysoft.jmte.template;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.floreysoft.jmte.ModelAdaptor;
import com.floreysoft.jmte.ProcessListener;

public interface Template {
	/**
	 * Transforms a template into an expanded output using the given model.
	 * 
	 * @param model
	 *            the model used to evaluate expressions inside the template
	 * @param locale
	 *            the locale used to render this template
	 * @param modelAdaptor
	 *            adaptor used for this transformation to look up values from
	 *            model
	 * @return the expanded output
	 */
	public String transform(Map<String, Object> model, Locale locale,
			ModelAdaptor modelAdaptor, ProcessListener processListener);

	/**
	 * Transforms a template into an expanded output using the given model.
	 * 
	 * @param model
	 *            the model used to evaluate expressions inside the template
	 * @param locale
	 *            the locale used to render this template
	 * @return the expanded output
	 */
	public String transform(Map<String, Object> model, Locale locale,
			ProcessListener processListener);

	/**
	 * Transforms a template into an expanded output using the given model.
	 * 
	 * @param model
	 *            the model used to evaluate expressions inside the template
	 * @param locale
	 *            the locale used to render this template
	 * @return the expanded output
	 */
	public String transform(Map<String, Object> model, Locale locale);

	public Set<String> getUsedVariables();

}
