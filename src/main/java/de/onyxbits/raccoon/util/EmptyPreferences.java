package de.onyxbits.raccoon.util;

import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * Empty preferences that do not provide or store any value
 */
public class EmptyPreferences extends AbstractPreferences {
	protected EmptyPreferences() { super(null, ""); }
	@Override protected AbstractPreferences childSpi(String name) { return new EmptyPreferences(); }
	@Override protected String[] childrenNamesSpi() throws BackingStoreException { return new String[0]; }
	@Override protected void flushSpi() throws BackingStoreException {}
	@Override protected String getSpi(String key) { return null; }
	@Override protected String[] keysSpi() throws BackingStoreException { return new String[0]; }
	@Override protected void putSpi(String key, String value) {}
	@Override protected void removeNodeSpi() throws BackingStoreException {}
	@Override protected void removeSpi(String key) {}
	@Override protected void syncSpi() throws BackingStoreException {}
}
