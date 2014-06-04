package de.onyxbits.raccoon;

/**
 * A utility class for safely opening urls in the webbrowser.
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
}
