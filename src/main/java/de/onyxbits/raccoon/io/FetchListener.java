package de.onyxbits.raccoon.io;

import java.io.File;

public interface FetchListener {

	/**
	 * Called periodically to signal progress.
	 * 
	 * @param src
	 *          who is calling
	 * @param numBytes
	 *          number of bytes received so far.
	 * @return true to abort and delete file. False to continue downloading.
	 */
	public boolean onChunk(FetchService src, long numBytes);

	/**
	 * Called when a download finished successfully
	 * 
	 * @param src
	 *          who is calling
	 */
	public void onComplete(FetchService src);

	/**
	 * Called when a download fails for some reason.
	 * 
	 * @param src
	 *          who is calling
	 * @param e
	 *          the exception that was thrown.
	 */
	public void onFailure(FetchService src, Exception e);

	/**
	 * Called to signal that the download was aborted by the user.
	 * 
	 * @param src
	 *          who is calling
	 */
	public void onAborted(FetchService src);

	/**
	 * Called when a file is about to be downloaded.
	 * 
	 * @param src
	 *          calling object
	 * @param file
	 *          the file that will be written to.
	 */
	public void onBeginFile(FetchService src, File file);

	/**
	 * Called when a file is finished. Do not rely on the file actually being in
	 * the archive after this. A download may contain extension files and the user
	 * may cancel the download of an extension in which case all downloaded files
	 * will be deleted. Only after onComplete() is called, you can assume the file
	 * to be actually stored.
	 * 
	 * @param src
	 *          calling object
	 * @param file
	 *          the file that was downloaded.
	 */
	public void onFinishFile(FetchService src, File file);
}
