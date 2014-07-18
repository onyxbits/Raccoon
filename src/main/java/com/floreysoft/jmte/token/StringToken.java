package com.floreysoft.jmte.token;

import java.util.List;

import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.Renderer;
import com.floreysoft.jmte.TemplateContext;
import com.floreysoft.jmte.encoder.Encoder;
import com.floreysoft.jmte.renderer.RawRenderer;

public class StringToken extends ExpressionToken {
	// ${<h1>,address(NIX),</h1>;long(full)}
	private final String defaultValue; // NIX
	private final String prefix; // <h1>
	private final String suffix; // </h1>
	private final String rendererName; // long
	private final String parameters; // full

	public StringToken() {
		this("", "", null, null, null, null, null);
	}

	public StringToken(String text, String variableName, String defaultValue,
			String prefix, String suffix, String rendererName, String parameters) {
		super(variableName);
		this.defaultValue = defaultValue;
		this.prefix = prefix;
		this.suffix = suffix;
		this.rendererName = rendererName;
		this.parameters = parameters;
		setText(text);
	}

	public StringToken(String variableName) {
		this(variableName, variableName, null, null, null, null, null);
	}

	public StringToken(String text, List<String> segments, String variableName,
			String defaultValue, String prefix, String suffix,
			String rendererName, String parameters) {
		super(segments, variableName);
		this.defaultValue = defaultValue;
		this.prefix = prefix;
		this.suffix = suffix;
		this.rendererName = rendererName;
		this.parameters = parameters;
		setText(text);
	}

	public StringToken(List<String> segments, String variableName) {
		super(segments, variableName);
		this.defaultValue = null;
		this.prefix = null;
		this.suffix = null;
		this.rendererName = null;
		this.parameters = null;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object evaluate(TemplateContext context) {
		boolean rawRendering = false;
		final Object value = evaluatePlain(context);

		final String renderedResult;
		if (value == null || value.equals("")) {
			renderedResult = defaultValue != null ? defaultValue : "";
		} else {
			String namedRendererResult = null;
			if (rendererName != null && !rendererName.equals("")) {
				final NamedRenderer rendererForName = context
						.resolveNamedRenderer(rendererName);
				if (rendererForName != null) {
					if (rendererForName instanceof RawRenderer) {
						rawRendering = true;
					}
					namedRendererResult = rendererForName.render(value, parameters, context.locale);
				}
			}
			if (namedRendererResult != null) {
				renderedResult = namedRendererResult;
			} else {
				final Renderer<Object> rendererForClass = (Renderer<Object>) context
						.resolveRendererForClass(value.getClass());
				if (rendererForClass != null) {
					if (rendererForClass instanceof RawRenderer) {
						rawRendering = true;
					}
					renderedResult = rendererForClass.render(value, context.locale);
				} else {
					renderedResult = value.toString();
				}
			}
		}

		if (renderedResult == null || renderedResult.equals("")) {
			return renderedResult;
		} else {
			final String prefixedRenderedResult = (prefix != null ? prefix : "") + renderedResult + (suffix != null ? suffix : "");
			Encoder encoder = context.getEncoder();
			if (!rawRendering && encoder != null) {
				final String encodedPrefixedRenderedResult = encoder.encode(prefixedRenderedResult);
				return encodedPrefixedRenderedResult;
			} else {
				return prefixedRenderedResult;
			}
		}
	}

	public String getRendererName() {
		return rendererName;
	}

	public String getParameters() {
		return parameters;
	}

  @Override
  public String emit() {
    StringBuilder sb = new StringBuilder();
    if ( prefix != null ) {
      sb.append(prefix).append(',');
    }
    sb.append(getExpression());
    if ( defaultValue != null ) {
      sb.append('(').append(defaultValue).append(')');
    }
    if ( suffix != null ) {
      sb.append(',').append(suffix);
    }
    if ( rendererName != null ) {
      sb.append(';').append(rendererName);
    }
    if ( parameters != null ) {
      sb.append('(').append(parameters).append(')');
    }
    return sb.toString();
  }
}
