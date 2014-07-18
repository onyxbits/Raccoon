package com.floreysoft.jmte.message;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.token.Token;

public class ResourceBundleMessage implements Message {

	protected static String getTemplate(ResourceBundle bundle, String key,
			String fallback) {
		return key != null && bundle.containsKey(key) ? bundle.getString(key)
				: fallback;
	}
	
	private final String messageCode;

	public ResourceBundleMessage(String messageCode) {
		this.messageCode = messageCode;
	}

	private String locationCode = "location";
	private String prefixCode = "prefix";
	private String frameCode = "frame";
	private Map<String, Object> argumentModel = new HashMap<String, Object>();
	private String baseName = "com.floreysoft.jmte.message.messages";

	public ResourceBundleMessage useLocationCode(String locationCode) {
		this.locationCode = locationCode;
		return this;
	}

	public ResourceBundleMessage onToken(Token token) {
		this.argumentModel.put("token", token);
		return this;
	}

	public ResourceBundleMessage usePrefixCode(String prefixCode) {
		this.prefixCode = prefixCode;
		return this;
	}

	public ResourceBundleMessage useFrameCode(String frameCode) {
		this.frameCode = frameCode;
		return this;
	}

	public ResourceBundleMessage withModel(Map<String, Object> model) {
		if (model != null) {
			this.argumentModel.putAll(model);
		}
		return this;
	}

	public ResourceBundleMessage withBaseName(String baseName) {
		this.baseName = baseName;
		return this;
	}

	public String getMessageCode() {
		return messageCode;
	}

	public String getLocationCode() {
		return locationCode;
	}

	public String getPrefixCode() {
		return prefixCode;
	}

	public String getFrameCode() {
		return frameCode;
	}

	public Map<String, Object> getArgumentModel() {
		return argumentModel;
	}

	public String getBaseName() {
		return baseName;
	}

	@Override
	public String format() {
		return format(new Locale("en"));
	}

	@Override
	public String format(Locale locale) {
		final ResourceBundle messages = ResourceBundle.getBundle(baseName, locale);
		final String frameTemplate = getTemplate(messages, frameCode, "${prefix} ${location}: ${message}");
		final String prefixTemplate = getTemplate(messages, prefixCode, "");
		final String locationTemplate = getTemplate(messages, locationCode, "");
		final String messageTemplate = getTemplate(messages, messageCode, "");
		
		Engine engine = new Engine();
		engine.setErrorHandler(new InternalErrorHandler());
		argumentModel.put("prefix", engine.transform(prefixTemplate, argumentModel));
		argumentModel.put("location", engine.transform(locationTemplate, argumentModel));
		argumentModel.put("message", engine.transform(messageTemplate, argumentModel));
		
		String transformed = engine.transform(frameTemplate, argumentModel);
		return transformed;

	}

	@Override
	public String formatPlain() {
		return formatPlain(new Locale("en"));
	}

	@Override
	public String formatPlain(Locale locale) {
		final ResourceBundle messages = ResourceBundle.getBundle(baseName, locale);
		final String messageTemplate = getTemplate(messages, messageCode, "");
		
		Engine engine = new Engine();
		engine.setErrorHandler(new InternalErrorHandler());
		String transformed = engine.transform(messageTemplate, argumentModel);
		return transformed;
	}
}
