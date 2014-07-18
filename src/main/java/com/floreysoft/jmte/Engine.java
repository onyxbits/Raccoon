package com.floreysoft.jmte;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.http.MethodNotSupportedException;

import com.floreysoft.jmte.encoder.Encoder;
import com.floreysoft.jmte.message.DefaultErrorHandler;
import com.floreysoft.jmte.message.SilentErrorHandler;
import com.floreysoft.jmte.renderer.DefaultCollectionRenderer;
import com.floreysoft.jmte.renderer.DefaultIterableRenderer;
import com.floreysoft.jmte.renderer.DefaultMapRenderer;
import com.floreysoft.jmte.renderer.DefaultObjectRenderer;
import com.floreysoft.jmte.template.InterpretedTemplate;
import com.floreysoft.jmte.template.Template;
import com.floreysoft.jmte.template.TemplateCompiler;
import com.floreysoft.jmte.token.IfToken;
import com.floreysoft.jmte.token.Token;
import com.floreysoft.jmte.util.Tool;
import com.floreysoft.jmte.util.Util;

/**
 * <p>
 * The template engine - <b>THIS IS WHERE YOU START LOOKING</b>.
 * </p>
 * 
 * <p>
 * Usually this is the only class you need calling
 * {@link #transform(String, Map)}. Like this
 * </p>
 * 
 * <pre>
 * Engine engine = new Engine();
 * String transformed = engine.transform(input, model);
 * </pre>
 * 
 * <p>
 * You have to provide the template input written in the template language and a
 * model from String to Object. Maybe like this
 * </p>
 * 
 * <pre>
 * String input = &quot;${name}&quot;;
 * Map&lt;String, Object&gt; model = new HashMap&lt;String, Object&gt;();
 * model.put(&quot;name&quot;, &quot;Minimal Template Engine&quot;);
 * Engine engine = new Engine();
 * String transformed = engine.transform(input, model);
 * assert (transformed.equals(&quot;Minimal Template Engine&quot;));
 * </pre>
 * 
 * where <code>input</code> contains the template and <code>model</code> the
 * model. <br>
 * 
 * <p>
 * Use {@link #setUseCompilation(boolean)} to switch on compilation mode. This
 * will compile the template into Java byte code before execution. Especially
 * when the template is used often this will speed up the execution by a factor
 * between 2 and 10. However, each compiled template results in a new class
 * definition and a new globally cached singleton instance of it.
 * </p>
 * 
 * <p>
 * This class is thread safe.
 * </p>
 * 
 * @see ErrorHandler
 * @see Tool
 * @see Renderer
 * @see NamedRenderer
 * @see ModelAdaptor
 * @see ProcessListener
 */
public final class Engine implements RendererRegistry {

	public final static String VERSION = "@version@";

	public static Engine createCachingEngine() {
		Engine engine = new Engine();
		engine.setEnabledInterpretedTemplateCache(true);
		return engine;
	}

	public static Engine createNonCachingEngine() {
		Engine engine = new Engine();
		engine.setEnabledInterpretedTemplateCache(false);
		return engine;
	}

	public static Engine createCompilingEngine() {
		Engine engine = new Engine();
		engine.setUseCompilation(true);
		return engine;
	}

	public static Engine createDefaultEngine() {
		Engine engine = new Engine();
		return engine;
	}

	private String exprStartToken = "${";
	private String exprEndToken = "}";
	private double expansionSizeFactor = 2;
	private ErrorHandler errorHandler = new DefaultErrorHandler();
	private boolean useCompilation = false;
	private boolean enabledInterpretedTemplateCache = true;
	private ModelAdaptor modelAdaptor = new DefaultModelAdaptor();
	private Encoder encoder = null;

	// compiler plus all compiled classes live as long as this engine
	private TemplateCompiler compiler;

	// compiled templates cache lives as long as this engine
	private final Map<String, Template> compiledTemplates = new HashMap<String, Template>();

	// interpreted templates cache lives as long as this engine
	private final Map<String, Template> interpretedTemplates = new HashMap<String, Template>();

	private final Map<Class<?>, Renderer<?>> renderers = new HashMap<Class<?>, Renderer<?>>();
	private final Map<Class<?>, Renderer<?>> resolvedRendererCache = new HashMap<Class<?>, Renderer<?>>();

	private final Map<String, AnnotationProcessor<?>> annotationProcessors = new HashMap<String, AnnotationProcessor<?>>();

	private final Map<String, NamedRenderer> namedRenderers = new HashMap<String, NamedRenderer>();
	private final Map<Class<?>, Set<NamedRenderer>> namedRenderersForClass = new HashMap<Class<?>, Set<NamedRenderer>>();

	/**
	 * Creates a new engine having <code>${</code> and <code>}</code> as start
	 * and end strings for expressions.
	 */
	public Engine() {
		init();
	}

	private void init() {
		registerRenderer(Object.class, new DefaultObjectRenderer());
		registerRenderer(Map.class, new DefaultMapRenderer());
		registerRenderer(Collection.class, new DefaultCollectionRenderer());
		registerRenderer(Iterable.class, new DefaultIterableRenderer());
	}

	/**
	 * Checks if all given variables are there and if so, that they evaluate to true inside an if.
	 */
	public boolean variablesAvailable(Map<String, Object> model, String... vars) {
		final TemplateContext context = new TemplateContext(null, null, null, new ScopedMap(model), modelAdaptor, this,
				new SilentErrorHandler(), null);
		for (String var : vars) {
			final IfToken token = new IfToken(var, false);
			if (!(Boolean) token.evaluate(context)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Transforms a template into an expanded output using the given model.
	 * 
	 * @param template
	 *            the template to expand
	 * @param locale
	 *            the locale being passed into renderers in
	 *            {@link TemplateContext}
	 * @param sourceName
	 *            the name of the current template (if there is anything like
	 *            that)
	 * @param model
	 *            the model used to evaluate expressions inside the template
	 * @return the expanded output
	 */
	public synchronized String transform(String template, Locale locale, String sourceName, Map<String, Object> model,
			ProcessListener processListener) {
		return transformInternal(template, locale, sourceName, model, getModelAdaptor(), processListener);
	}

	public synchronized String transform(String template, String sourceName, Map<String, Object> model,
			ProcessListener processListener) {
		return transformInternal(template, sourceName, model, getModelAdaptor(), processListener);
	}

	public synchronized String transform(String template, Locale locale, String sourceName, Map<String, Object> model) {
		return transformInternal(template, locale, sourceName, model, getModelAdaptor(), null);
	}

	public synchronized String transform(String template, String sourceName, Map<String, Object> model) {
		return transformInternal(template, sourceName, model, getModelAdaptor(), null);
	}

	public synchronized String transform(String template, Locale locale, Map<String, Object> model) {
		return transformInternal(template, locale, null, model, getModelAdaptor(), null);
	}

	public synchronized String transform(String template, Map<String, Object> model) {
		return transformInternal(template, null, model, getModelAdaptor(), null);
	}

	public synchronized String transform(String template, Map<String, Object> model, ProcessListener processListener) {
		return transformInternal(template, null, model, getModelAdaptor(), processListener);
	}

	public synchronized String transform(String template, Locale locale, Map<String, Object> model,
			ProcessListener processListener) {
		return transformInternal(template, locale, null, model, getModelAdaptor(), processListener);
	}

	String transformInternal(String template, String sourceName, Map<String, Object> model, ModelAdaptor modelAdaptor,
			ProcessListener processListener) {
		Locale locale = Locale.getDefault();
		return transformInternal(template, locale, sourceName, model, modelAdaptor, processListener);
	}

	String transformInternal(String template, Locale locale, String sourceName, Map<String, Object> model,
			ModelAdaptor modelAdaptor, ProcessListener processListener) {
		Template templateImpl = getTemplate(template, sourceName);
		String output = templateImpl.transform(model, locale, modelAdaptor, processListener);
		return output;
	}

	/**
	 * Replacement for {@link java.lang.String.format}. All arguments will be
	 * put into the model having their index starting from 1 as their name.
	 * 
	 * @param pattern
	 *            the template
	 * @param args
	 *            any number of arguments
	 * @return the expanded template
	 */
	public synchronized String format(final String pattern, final Object... args) {
		Map<String, Object> model = Collections.emptyMap();
		ModelAdaptor modelAdaptor = new ModelAdaptor() {

			@Override
			public Object getValue(TemplateContext context, Token token, List<String> segments, String expression) {
				int index = Integer.parseInt(expression) - 1;
				return args[index];
			}

		};

		String output = transformInternal(pattern, null, model, modelAdaptor, null);
		return output;
	}

	/**
	 * Gets all variables used in the given template.
	 */
	public synchronized Set<String> getUsedVariables(String template) {
		Template templateImpl = getTemplate(template, null);
		return templateImpl.getUsedVariables();
	}

	@Override
	public synchronized Engine registerNamedRenderer(NamedRenderer renderer) {
		namedRenderers.put(renderer.getName(), renderer);
		Set<Class<?>> supportedClasses = Util.asSet(renderer.getSupportedClasses());
		for (Class<?> clazz : supportedClasses) {
			Class<?> classInHierarchy = clazz;
			while (classInHierarchy != null) {
				addSupportedRenderer(classInHierarchy, renderer);
				classInHierarchy = classInHierarchy.getSuperclass();
			}
		}
		return this;
	}

	@Override
	public synchronized Engine deregisterNamedRenderer(NamedRenderer renderer) {
		namedRenderers.remove(renderer.getName());
		Set<Class<?>> supportedClasses = Util.asSet(renderer.getSupportedClasses());
		for (Class<?> clazz : supportedClasses) {
			Class<?> classInHierarchy = clazz;
			while (classInHierarchy != null) {
				Set<NamedRenderer> renderers = namedRenderersForClass.get(classInHierarchy);
				renderers.remove(renderer);
				classInHierarchy = classInHierarchy.getSuperclass();
			}
		}
		return this;
	}

	private void addSupportedRenderer(Class<?> clazz, NamedRenderer renderer) {
		Collection<NamedRenderer> compatibleRenderers = getCompatibleRenderers(clazz);
		compatibleRenderers.add(renderer);
	}

	@Override
	public synchronized Collection<NamedRenderer> getCompatibleRenderers(Class<?> inputType) {
		Set<NamedRenderer> renderers = namedRenderersForClass.get(inputType);
		if (renderers == null) {
			renderers = new HashSet<NamedRenderer>();
			namedRenderersForClass.put(inputType, renderers);
		}
		return renderers;
	}

	@Override
	public synchronized Collection<NamedRenderer> getAllNamedRenderers() {
		Collection<NamedRenderer> values = namedRenderers.values();
		return values;
	}

	@Override
	public NamedRenderer resolveNamedRenderer(String rendererName) {
		return namedRenderers.get(rendererName);
	}

	public synchronized Engine registerAnnotationProcessor(AnnotationProcessor<?> annotationProcessor) {
		annotationProcessors.put(annotationProcessor.getType(), annotationProcessor);
		return this;
	}

	public synchronized Engine deregisterAnnotationProcessor(AnnotationProcessor<?> annotationProcessor) {
		annotationProcessors.remove(annotationProcessor.getType());
		return this;
	}

	AnnotationProcessor<?> resolveAnnotationProcessor(String type) {
		return annotationProcessors.get(type);
	}

	@Override
	public synchronized <C> Engine registerRenderer(Class<C> clazz, Renderer<C> renderer) {
		renderers.put(clazz, renderer);
		resolvedRendererCache.clear();
		return this;
	}

	@Override
	public synchronized Engine deregisterRenderer(Class<?> clazz) {
		renderers.remove(clazz);
		resolvedRendererCache.clear();
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <C> Renderer<C> resolveRendererForClass(Class<C> clazz) {
		Renderer resolvedRenderer = resolvedRendererCache.get(clazz);
		if (resolvedRenderer != null) {
			return resolvedRenderer;
		}

		resolvedRenderer = renderers.get(clazz);
		if (resolvedRenderer == null) {
			Class<?>[] interfaces = clazz.getInterfaces();
			for (Class<?> interfaze : interfaces) {
				resolvedRenderer = resolveRendererForClass(interfaze);
				if (resolvedRenderer != null) {
					break;
				}
			}
		}
		if (resolvedRenderer == null) {
			Class<?> superclass = clazz.getSuperclass();
			if (superclass != null) {
				resolvedRenderer = resolveRendererForClass(superclass);
			}
		}
		if (resolvedRenderer != null) {
			resolvedRendererCache.put(clazz, resolvedRenderer);
		}
		return resolvedRenderer;
	}

	public synchronized void setEncoder(Encoder encoder) {
		this.encoder = encoder;
	}
	
	public synchronized Encoder getEncoder() {
		return encoder;
	}
	
	public synchronized void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	public synchronized ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	public synchronized String getExprStartToken() {
		return exprStartToken;
	}

	public synchronized String getExprEndToken() {
		return exprEndToken;
	}

	public synchronized void setExprStartToken(String exprStartToken) {
		this.exprStartToken = exprStartToken;
	}

	public synchronized void setExprEndToken(String exprEndToken) {
		this.exprEndToken = exprEndToken;
	}

	public synchronized void setExpansionSizeFactor(double expansionSizeFactor) {
		this.expansionSizeFactor = expansionSizeFactor;
	}

	public synchronized double getExpansionSizeFactor() {
		return expansionSizeFactor;
	}

	public synchronized boolean isUseCompilation() {
		return useCompilation;
	}

	public synchronized void setUseCompilation(boolean useCompilation) {
		throw new RuntimeException("This part has been removed to cut down dependencies!");
		//this.useCompilation = useCompilation;
	}

	public synchronized void setModelAdaptor(ModelAdaptor modelAdaptor) {
		this.modelAdaptor = modelAdaptor;
	}

	public synchronized ModelAdaptor getModelAdaptor() {
		return modelAdaptor;
	}

	public synchronized boolean isEnabledInterpretedTemplateCache() {
		return enabledInterpretedTemplateCache;
	}

	public synchronized void setEnabledInterpretedTemplateCache(boolean enabledInterpretedTemplateCache) {
		this.enabledInterpretedTemplateCache = enabledInterpretedTemplateCache;
	}

	/**
	 * Gets a template for a certain source.
	 * 
	 * @param template
	 *            the template source
	 * @return the prepared template
	 */
	public Template getTemplate(String template) {
		return getTemplate(template, null);
	}

	/**
	 * Gets a template for a certain source.
	 * 
	 * @param template
	 *            the template source
	 * @param sourceName
	 *            the template name
	 * @return the prepared template
	 */
	public Template getTemplate(String template, String sourceName) {
		Template templateImpl;
		if (useCompilation) {
			templateImpl = compiledTemplates.get(template);
			if (templateImpl == null) {
				templateImpl = compiler.compile(template, sourceName, this);
				compiledTemplates.put(template, templateImpl);
			}
			return templateImpl;
		} else {
			if (enabledInterpretedTemplateCache) {
				templateImpl = interpretedTemplates.get(template);
				if (templateImpl == null) {
					templateImpl = new InterpretedTemplate(template, sourceName, this);
					interpretedTemplates.put(template, templateImpl);
				}
			} else {
				templateImpl = new InterpretedTemplate(template, sourceName, this);
			}
		}
		return templateImpl;
	}

	public void setCompiler(TemplateCompiler compiler) {
		this.compiler = compiler;
	}

	public TemplateCompiler getCompiler() {
		return compiler;
	}
}
