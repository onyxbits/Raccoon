package com.floreysoft.jmte.template;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.ModelAdaptor;
import com.floreysoft.jmte.ProcessListener;
import com.floreysoft.jmte.ScopedMap;
import com.floreysoft.jmte.TemplateContext;
import com.floreysoft.jmte.util.Util;

public abstract class AbstractCompiledTemplate extends AbstractTemplate {

	public AbstractCompiledTemplate() {
	}

	public AbstractCompiledTemplate(Engine engine) {
		this.engine = engine;
	}

	@Override
	public Set<String> getUsedVariables() {
		return usedVariables;
	}

	@Override
	public synchronized String transform(Map<String, Object> model, Locale locale, ModelAdaptor modelAdaptor, ProcessListener processListener) {
		TemplateContext context = new TemplateContext(template, locale, sourceName,
				new ScopedMap(model), modelAdaptor, engine, engine.getErrorHandler(), processListener);

		String transformed = transformCompiled(context);
		return transformed;

	}

	protected abstract String transformCompiled(TemplateContext context);

	public void setEngine(Engine engine) {
		this.engine = engine;
	}

	public Engine getEngine() {
		return engine;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getTemplate() {
		return template;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getSourceName() {
		return sourceName;
	}

	@Override
	public String toString() {
		return template;
	}

}
