package de.onyxbits.raccoon.io;

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
	public boolean onChunk(Object src, long numBytes);

	/**
	 * Called when a download finished successfully
	 * 
	 * @param src
	 *          who is calling
	 */
	public void onComplete(Object src);

	/**
	 * Called when a download fails for some reason.
	 * 
	 * @param src
	 *          who is calling
	 * @param e
	 *          the exception that was thrown.
	 */
	public void onFailure(Object src, Exception e);
}
