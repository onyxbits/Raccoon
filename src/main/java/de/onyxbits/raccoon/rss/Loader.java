package de.onyxbits.raccoon.rss;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import de.onyxbits.raccoon.App;

/**
 * Load and cache the newsfeed.
 * 
 * @author patrick
 * 
 */
public class Loader {
	
	/**
	 * Feed URL
	 */
	public static final String FEED = "http://www.onyxbits.de/raccoon/newsfeed";
	
	/**
	 * Time To live (how long the cached version is valid): 1 hour.
	 */
	public static final long TTL = 60*60*1000;

	/**
	 * Call this on program start to load and cache the feed.
	 * 
	 * @throws IOException
	 *           if downloading the xml file fails
	 */
	public static void update() throws IOException {
		long now = System.currentTimeMillis();
		File file = getFeedCache();
		if (!file.exists() || file.lastModified() < now - TTL) {
			FileUtils.copyURLToFile(new URL(FEED), file, 5000, 5000);
		}
	}

	/**
	 * Where to cache the newsfeed.
	 * 
	 * @return a file.
	 */
	public static File getFeedCache() {
		return new File(App.getDir(App.CACHEDIR), "newsfeed.xml");
	}
}
