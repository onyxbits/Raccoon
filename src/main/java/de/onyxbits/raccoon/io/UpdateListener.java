package de.onyxbits.raccoon.io;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;

public interface UpdateListener extends FetchListener {

	public static int PROCEED = 0;
	public static int SKIP = 1;
	public static int ABORT = 2;

	/**
	 * Called to signal that a download is about to be started.
	 * 
	 * @param src
	 *          calling object.
	 * @param doc
	 *          app description.
	 * @return PROCEED, SKIP or ABORT
	 */
	public int onBeginDownload(Object src, DocV2 doc);

}
