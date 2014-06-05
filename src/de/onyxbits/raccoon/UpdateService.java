package de.onyxbits.raccoon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Vector;

import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsEntry;
import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;

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
			createService();
			System.err.println("# Downloading packagelist");
			response = service.bulkDetails(archive.list());
		}
		catch (Exception e) {
			System.err.println("# Error: " + e.getMessage());
			return;
		}

		Vector<File> received = new Vector<File>();
		for (BulkDetailsEntry bulkDetailsEntry : response.getEntryList()) {
			try {
				File res = download(bulkDetailsEntry.getDoc());
				if (res != null) {
					received.add(res);
					archive.getDownloadLogger().addEntry(res);
				}
			}
			catch (Exception e) {
				System.err.println("# Error: " + e.getMessage());
			}
		}
		if (received.size() == 0) {
			System.err.println("# Nothing to update");
		}
		else {
			System.err.println("# Successfully downloaded: ");
		}
		for (File f : received) {
			System.err.println(f.getAbsolutePath());
		}
	}

	/**
	 * Download an app
	 * 
	 * @param doc
	 *          app description
	 * @return the target file or null if the target already existed.
	 * @throws Exception
	 *           if something went wrong.
	 */
	private File download(DocV2 doc) throws Exception {
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
			}
			catch (Exception e) {
				target.delete();
				throw e;
			}
			return target;
		}
		return null;
	}

	/**
	 * Hook up with GPlay
	 * 
	 * @throws Exception
	 *           if something goes wrong.
	 */
	private void createService() throws Exception {
		String pwd = archive.getPassword();
		String uid = archive.getUserId();
		String aid = archive.getAndroidId();
		service = new GooglePlayAPI(uid, pwd, aid);

		if (archive.getProxyClient() != null) {
			service.setClient(archive.getProxyClient());
		}
		service.setToken(archive.getAuthToken());
		if (service.getToken() == null) {
			service.login();
			archive.setAuthToken(service.getToken());
		}
	}
}
