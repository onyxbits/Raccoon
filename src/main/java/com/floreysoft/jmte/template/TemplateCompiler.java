package com.floreysoft.jmte.template;

import com.floreysoft.jmte.Engine;

/**
 * Compiles template source to an executable template.
 * 
 */
public interface TemplateCompiler {
	public Template compile(String template, String sourceName, Engine engine);
}
