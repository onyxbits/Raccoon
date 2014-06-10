package de.onyxbits.raccoon.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A simple session log for recording the files that were downloaded. The log is
 * meant to be used as an input for shellscripts, so the logformat is dead
 * simple: every line contains the filename of one successfully downloded file.
 * 
 * @author patrick
 * 
 */
public class DownloadLogger {

	public static final String LOGCOMPLETE = "downloads-complete.txt";
	private File completeLog;

	/**
	 * Create a new logger
	 * 
	 * @param logfile
	 *          the file to log to.
	 */
	public DownloadLogger(Archive archive) {
		completeLog = new File(new File(archive.getRoot(), Archive.LOGDIR), LOGCOMPLETE);
	}

	/**
	 * Clears the download log. This should be called when starting the session.
	 */
	public synchronized void clear() {
		if (completeLog.exists()) {
			completeLog.delete();
		}
	}

	/**
	 * Log a completed download to the logfile
	 * 
	 * @param file
	 *          the file that was downloaded
	 * @throws IOException
	 *           if writing fails.
	 */
	public synchronized void addEntry(File file) throws IOException {
		completeLog.getParentFile().mkdirs();
		FileWriter fw = new FileWriter(completeLog, true);
		fw.write(completeLog.getAbsolutePath() + "\n");
		fw.close();
	}
}
