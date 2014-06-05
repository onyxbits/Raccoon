package de.onyxbits.raccoon;

import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;

/**
 * Just the application launcher.
 * 
 * @author patrick
 * 
 */
public class App implements Runnable {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (System.getProperty("update") != null) {
			Archive a = new Archive(new File(System.getProperty("update")));
			a.getDownloadLogger().clear();
			new UpdateService(a).run();
		}
		else {
			SwingUtilities.invokeLater(new App());
		}
	}

	public void run() {
		Preferences prefs = Preferences.userNodeForPackage(MainActivity.class);

		Archive a = new Archive(new File(prefs.get(MainActivity.LASTARCHIVE, "Raccoon")));
		a.getDownloadLogger().clear();
		MainActivity ma = MainActivity.create();
		ma.doMount(a);
		ma.setVisible(true);
	}

}
