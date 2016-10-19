package de.onyxbits.raccoon.util;

import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

/**
 * Factory for EmptyPreferences
 * @see EmptyPreferences
 */
public class EmptyPreferencesFactory implements PreferencesFactory {
	@Override public Preferences systemRoot() { return new EmptyPreferences(); }
	@Override public Preferences userRoot() { return new EmptyPreferences(); }
}
