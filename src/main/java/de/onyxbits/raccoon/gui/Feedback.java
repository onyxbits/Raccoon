package de.onyxbits.raccoon.gui;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import de.onyxbits.raccoon.BrowseUtil;
import de.onyxbits.raccoon.Messages;

/**
 * Ask the user for feedback after a reasonable amount of uses.
 * 
 * @author patrick
 * 
 */
class Feedback {

	public static final String URL = "http://www.onyxbits.de/raccoon/feedback";
	public static final int DAYS = 7;
	public static final int USES = 5;

	public static final String KEY_COUNT = "downloadcount";
	public static final String KEY_FIRST = "firstdownload";
	public static final String KEY_DONE = "dontshow";

	/**
	 * Call this when the user performs a "use" action.
	 * 
	 * @param center
	 *          a component to center the feedback dialog upon (or null).
	 */
	public static void used(JFrame center) {
		Preferences prefs = Preferences.userNodeForPackage(Feedback.class);
		if (prefs.getBoolean(KEY_DONE, false)) {
			return;
		}
		long count = prefs.getLong(KEY_COUNT, 0) + 1;
		prefs.putLong(KEY_COUNT, count);

		long first = prefs.getLong(KEY_FIRST, 0);
		if (first == 0) {
			first = System.currentTimeMillis();
			prefs.putLong(KEY_FIRST, first);
		}
		try {
			prefs.flush();
		}
		catch (BackingStoreException e) {
			e.printStackTrace();
		}
		if (count >= USES) {
			if (System.currentTimeMillis() >= first + (DAYS * 24 * 60 * 60 * 1000)) {
				showDialog(center);
			}
		}
	}

	private static void showDialog(JFrame center) {
		Preferences prefs = Preferences.userNodeForPackage(Feedback.class);
		Object[] options = {
				Messages.getString("Feedback.yes"),
				Messages.getString("Feedback.later"),
				Messages.getString("Feedback.no") };
		String title = Messages.getString("Feedback.title");
		String message = Messages.getString("Feedback.message");

		int n = JOptionPane.showOptionDialog(center, message, title, JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		switch (n) {
			case 0: {
				prefs.putBoolean(KEY_DONE, true);
				BrowseUtil.openUrl(URL);
				break;
			}
			case 1: {
				prefs.putLong(KEY_COUNT, 0);
				break;
			}
			case 2: {
				prefs.putBoolean(KEY_DONE, true);
				break;
			}
		}
		try {
			prefs.flush();
		}
		catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

}
