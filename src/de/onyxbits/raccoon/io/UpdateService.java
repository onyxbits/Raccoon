package de.onyxbits.raccoon.io;

import java.io.File;

import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsEntry;
import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;

import de.onyxbits.raccoon.App;

/**
 * Scans the apk storage, downloads the latest APKs. Packages are downloaded
 * single file:
 * <ul>
 * <li>If we wanted to download in parallel, we'd have to measure the speed of
 * the uplink and calculate how many threads we could run at once without
 * congesting the line. The speedup is not even remotely worth the trouble.
 * <li>The callbacks would get tremendously more complex. Again, it's not worth
 * the trouble.
 * <li>Google may not be fond of massively parallel downloads, so let's not do
 * anything that might trigger something.
 * </ul>
 * 
 * @author patrick
 * 
 */
public class UpdateService implements Runnable {

	private Archive archive;
	private GooglePlayAPI service;
	private UpdateListener callback;

	/**
	 * Construct a new service
	 * 
	 * @param archive
	 *          the archive to update.
	 * @param callback
	 *          the listener to report back to. This may be null. Note:
	 *          UpdateService internally uses FetchService for downloading and
	 *          will pass the listener on. Listeners should be prepared to get
	 *          called from a FetchService as well.
	 */
	public UpdateService(Archive archive, UpdateListener callback) {
		this.archive = archive;
		this.callback = callback;
	}

	/**
	 * Start updating.
	 */
	public void run() {
		BulkDetailsResponse response = null;
		try {
			service = App.createConnection(archive);
			response = service.bulkDetails(archive.list());
		}
		catch (Exception e) {
			if (callback != null) {
				callback.onFailure(this, e);
			}
			return;
		}

		for (BulkDetailsEntry bulkDetailsEntry : response.getEntryList()) {
			DocV2 doc = bulkDetailsEntry.getDoc();
			String pn = doc.getBackendDocid();
			int vc = -1;
			int ot = -1;
			try {
				vc = doc.getDetails().getAppDetails().getVersionCode();
				ot = doc.getOffer(0).getOfferType();
			}
			catch (Exception e) {
				// Somethign in the apk storage did not resolve. This could be an app
				// that was puleld from Google Play or a directory s/he created. Design
				// decission: ignore silently. In the first case the user doesn't want
				// to bother in the second, s/hedoes not need to.
				continue;
			}
			File target = archive.fileUnder(pn, vc);
			if (!target.exists()) {
				if (callback != null) {
					switch (callback.onBeginDownload(this, doc)) {
						case UpdateListener.SKIP: {
							continue;
						}
						case UpdateListener.ABORT: {
							return;
						}
					}
				}
				FetchService fs = new FetchService(archive, pn, vc, ot, callback);
				fs.run();
			}
		}
	}
}
