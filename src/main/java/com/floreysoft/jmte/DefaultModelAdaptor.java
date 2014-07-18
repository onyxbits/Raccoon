package com.floreysoft.jmte;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import com.floreysoft.jmte.token.Token;

/**
 * Default implementation of the model adapter.
 * <p>
 * Does the object traversal using the "." operator. Resolved value will be
 * checked if it is either a {@link Processor} or a {@link Callable} in which
 * case the final resolved value is computed by calling those executable
 * objects.
 * </p>
 * 
 * <p>
 * Inherit from this adapter if you want a slight change of this behavior and
 * set your new adaptor on the engine
 * {@link Engine#setModelAdaptor(ModelAdaptor)} .
 * </p>
 */
public class DefaultModelAdaptor implements ModelAdaptor {

	protected Map<Class<?>, Map<String, Member>> cache = new HashMap<Class<?>, Map<String, Member>>();

	@Override
	@SuppressWarnings("rawtypes")
	public Object getValue(TemplateContext context, Token token,
			List<String> segments, String expression) {
		Object value = traverse(segments, context.model, context.errorHandler, token);
		// if value implements both, we use the more specialized implementation
		if (value instanceof Processor) {
			value = ((Processor) value).eval(context);
		} else if (value instanceof Callable) {
			try {
				value = ((Callable) value).call();
			} catch (Exception e) {
			}
		}
		return value;
	}

	protected Object traverse(List<String> segments, Map<String, Object> model,
			ErrorHandler errorHandler, Token token) {
		if (segments.size() == 0) {
			return null;
		}
		String objectName = segments.get(0);
		Object value = model.get(objectName);

		value = traverse(value, segments, 1, errorHandler, token);
		return value;
	}

	protected Object traverse(Object o, List<String> attributeNames, int index,
			ErrorHandler errorHandler, Token token) {
		Object result;
		if (index >= attributeNames.size()) {
			result = o;
		} else {
			if (o == null) {
				return null;
			}
			String attributeName = attributeNames.get(index);
			Object nextStep = nextStep(o, attributeName, errorHandler, token);
			result = traverse(nextStep, attributeNames, index + 1,
					errorHandler, token);
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	protected Object nextStep(Object o, String attributeName,
			ErrorHandler errorHandler, Token token) {
		Object result;
		if (o instanceof String) {
			errorHandler.error("no-call-on-string", token, new ModelBuilder(
					"receiver", o.toString()).build());
			return o;
		} else if (o instanceof Map) {
			Map map = (Map) o;
			result = map.get(attributeName);
		} else {
			try {
				result = getPropertyValue(o, attributeName);
			} catch (Exception e) {
				errorHandler.error("property-access-error", token,
						new ModelBuilder("property", attributeName, "object",
								o, "exception", e).build());
				result = "";
			}
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	protected Object getPropertyValue(Object o, String propertyName) {
		try {
			// XXX this is so strange, can not call invoke on key and value for
			// Map.Entry, so we have to get this done like this:
			if (o instanceof Map.Entry) {
				final Map.Entry entry = (Entry) o;
				if (propertyName.equals("key")) {
					final Object result = entry.getKey();
					return result;
				} else if (propertyName.equals("value")) {
					final Object result = entry.getValue();
					return result;
				}

			}
			boolean valueSet = false;
			Object value = null;
			Member member = null;
			final Class<?> clazz = o.getClass();
			Map<String, Member> members = cache.get(clazz);
			if (members == null) {
				members = new HashMap<String, Member>();
				cache.put(clazz, members);
			} else {
				member = members.get(propertyName);
				if (member != null) {
					if (member.getClass() == Method.class)
						return ((Method) member).invoke(o);
					if (member.getClass() == Field.class)
						return ((Field) member).get(o);
				}
			}

			final String suffix = Character.toUpperCase(propertyName.charAt(0))
					+ propertyName.substring(1);
			final Method[] declaredMethods = clazz.getMethods();
			for (Method method : declaredMethods) {
				if (Modifier.isPublic(method.getModifiers())
						&& (method.getName().equals("get" + suffix) || method
								.getName().equals("is" + suffix))) {
					value = method.invoke(o, (Object[]) null);
					valueSet = true;
					member = method;
					break;

				}
			}
			if (!valueSet) {
				final Field field = clazz.getField(propertyName);
				if (Modifier.isPublic(field.getModifiers())) {
					value = field.get(o);
					member = field;
					valueSet = true;
				}
			}
			if (valueSet) {
				members.put(propertyName, member);
			}
			return value;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
