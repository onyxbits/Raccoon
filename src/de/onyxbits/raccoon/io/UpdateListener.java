package de.onyxbits.raccoon.io;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;

public interface UpdateListener extends FetchListener {

	/**
	 * Called to signal that a download is about to be started.
	 * 
	 * @param src
	 *          calling object.
	 * @param doc
	 *          app description.
	 */
	public void onBeginDownload(Object src, DocV2 doc);

}
