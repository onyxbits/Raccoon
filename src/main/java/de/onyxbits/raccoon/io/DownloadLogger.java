package de.onyxbits.raccoon.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.io.IOUtils;

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
	public static final String LOGCOMPLETEOLD = "downloads-complete.old";
	private File completeLog;
	private File completeLogOld;

	/**
	 * Create a new logger
	 * 
	 * @param logfile
	 *          the file to log to.
	 */
	public DownloadLogger(Archive archive) {
		completeLog = new File(new File(archive.getRoot(), Archive.LOGDIR), LOGCOMPLETE);
		completeLogOld = new File(new File(archive.getRoot(), Archive.LOGDIR), LOGCOMPLETEOLD);
	}

	/**
	 * List what was downloaded in the last session
	 * 
	 * @return new files in the storage.
	 */
	public List<FileNode> getLastSessionDownloads() {
		Vector<FileNode> ret = new Vector<FileNode>();
		try {
			String tmp = IOUtils.toString(new FileInputStream(completeLogOld));
			StringTokenizer tk = new StringTokenizer(tmp, "\n");
			while (tk.hasMoreTokens()) {
				ret.add(new FileNode(new File(tk.nextToken())));
			}
		}
		catch (Exception e) {
		}
		return ret;
	}

	/**
	 * Clears the download log. This should be called when starting the session.
	 */
	public synchronized void clear() {
		if (completeLogOld.exists()) {
			completeLogOld.delete();
		}

		if (completeLog.exists()) {
			completeLog.renameTo(completeLogOld);
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
		fw.write(file.getAbsolutePath() + "\n");
		fw.close();
	}
}
