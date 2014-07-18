package com.floreysoft.jmte;

import java.util.Locale;


/**
 * A renderer addressed by its name, not by its type.
 * 
 * @see Renderer
 * @see RenderFormatInfo
 */
public interface NamedRenderer {
	/**
	 * Renders an object of a type supported by the renderer.
	 * 
	 * @param o
	 *            the object to render
	 * @param format
	 *            anything that tells the renderer how to do its work
	 * @param locale
	 *            the locale used in transformation
	 * @return the rendered object
	 */
	public String render(Object o, String format, Locale locale);

	/**
	 * Gets the name of the renderer.
	 */
	public String getName();

	/**
	 * Gives information about what can be passed as a format parameter to the
	 * {@link #render(Object, String)} method.
	 */
	public RenderFormatInfo getFormatInfo();

	/**
	 * Returns which classes are support by this renderer, i.e. which ones you
	 * can pass to the {@link #render(Object, String)} method.
	 */
	public Class<?>[] getSupportedClasses();

}