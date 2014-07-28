package de.onyxbits.raccoon;

import java.io.File;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * 
 * Wrapper for the desktop class.
 * 
 * @author patrick
 * 
 */
public class BrowseUtil implements HyperlinkListener{

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
	
	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			if ("file".equals(e.getURL().getProtocol())) {
				try {
					BrowseUtil.openFile(new File(e.getURL().toURI()));
				}
				catch (Exception exp) {
					exp.printStackTrace();
				}
			}
			if ("http".equals(e.getURL().getProtocol())) {
				BrowseUtil.openUrl(e.getURL().toString());
			}
			if ("https".equals(e.getURL().getProtocol())) {
				BrowseUtil.openUrl(e.getURL().toString());
			}
		}
	}
	
}
