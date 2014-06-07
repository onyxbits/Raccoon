package de.onyxbits.raccoon.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsEntry;
import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;

import de.onyxbits.raccoon.App;

/**
 * Scans the apk storage, downloads the latest APKs
 * 
 * @author patrick
 * 
 */
public class UpdateService implements Runnable {

	private Archive archive;
	private GooglePlayAPI service;

	public UpdateService(Archive archive) {
		this.archive = archive;
	}

	public void run() {
		BulkDetailsResponse response = null;
		try {
			System.err.println("# Login...");
			service=App.createConnection(archive);
			System.err.println("# Downloading packagelist");
			response = service.bulkDetails(archive.list());
		}
		catch (Exception e) {
			System.err.println("# Error: " + e.getMessage());
			return;
		}

		for (BulkDetailsEntry bulkDetailsEntry : response.getEntryList()) {
			try {
				download(bulkDetailsEntry.getDoc());
			}
			catch (Exception e) {
				System.err.println("# Error: " + e.getMessage());
			}
		}
	}

	/**
	 * Download an app
	 * 
	 * @param doc
	 *          app description
	 * @throws Exception
	 *           if something went wrong.
	 */
	private void download(DocV2 doc) throws Exception {
		String pn = doc.getBackendDocid();
		if (pn == null || pn.length() == 0) {
			throw new IllegalAccessException("Package not available");
		}
		int vc = doc.getDetails().getAppDetails().getVersionCode();
		File target = archive.fileUnder(pn, vc);
		if (!target.exists()) {
			System.err.println("# Downloading: " + target.getAbsolutePath());
			try {
				int ot = doc.getOffer(0).getOfferType();
				InputStream in = service.download(pn, vc, ot);

				target.getParentFile().mkdirs();
				FileOutputStream out = new FileOutputStream(target);
				byte[] buffer = new byte[1024 * 16];
				int length;
				while ((length = in.read(buffer)) > 0) {
					out.write(buffer, 0, length);
				}
				out.close();
				in.close();
				archive.getDownloadLogger().addEntry(target);
			}
			catch (Exception e) {
				target.delete();
				throw e;
			}
		}
	}
}
