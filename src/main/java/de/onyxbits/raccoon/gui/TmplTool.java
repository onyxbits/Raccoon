package de.onyxbits.raccoon.gui;

import java.io.InputStream;
import java.util.HashMap;

import com.floreysoft.jmte.Engine;

/**
 * Tool for loading a template from the resources and expanding the variables in
 * it.
 * 
 * @author patrick
 * 
 */
class TmplTool {

	public static final String TMPLDIR = "/rsrc/templates/";
	
	private static Engine engine = new Engine();

	/**
	 * Load a template, expand the variables in it.
	 * 
	 * @param tmplFile
	 *          file, relative to TMPLDIR.
	 * @param model
	 *          A string to object mapping for expanding variables.
	 * @return template with expanded variables.
	 */
	public static String transform(String tmplFile, HashMap<String, Object> model) {
		String tmpl = getTemplate(TMPLDIR + tmplFile);
		return engine.transform(tmpl, model);
	}

	/**
	 * Read a resource file and return it as a sring
	 * 
	 * @param path
	 *          resource path
	 * @return content.
	 */
	private static String getTemplate(String path) {
		String tmpl = ""; //$NON-NLS-1$
		try {
			InputStream ins = new Object().getClass().getResourceAsStream(path);
			byte[] b = new byte[ins.available()];
			ins.read(b);
			tmpl = new String(b);
			ins.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return tmpl;
	}
}
