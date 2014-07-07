package de.onyxbits.raccoon;

import java.io.File;

/**
 * 
 * Wrapper for the desktop class.
 * 
 * @author patrick
 * 
 */
public class BrowseUtil {

	/**
	 * Open a url
	 * 
	 * @param url
	 *          url to open
	 * @return any exception that might be thrown
	 */
	public static Exception openUrl(String url) {
		try {
			if (java.awt.Desktop.isDesktopSupported()) {
				java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

				if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
					java.net.URI uri = new java.net.URI(url);
					desktop.browse(uri);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return e;
		}
		return null;
	}

	/**
	 * Open a file in the filemanager
	 * 
	 * @param file
	 *          the destination file
	 * @return exception if thrown or null.
	 */
	public static Exception openFile(File file) {
		try {
			if (java.awt.Desktop.isDesktopSupported()) {
				java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

				if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
					desktop.open(file);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return e;
		}
		return null;
	}
}
