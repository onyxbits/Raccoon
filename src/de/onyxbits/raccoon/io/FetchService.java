package de.onyxbits.raccoon.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.akdeniz.googleplaycrawler.GooglePlayAPI;

import de.onyxbits.raccoon.App;

/**
 * Download an APK.
 * 
 * @author patrick
 * 
 */
public class FetchService implements Runnable {

	private int versionCode;
	private String appId;
	private Archive archive;
	private GooglePlayAPI service;
	private int offerType;
	private FetchListener callback;

	/**
	 * Create a new downloader
	 * 
	 * @param archive
	 *          archive to download to
	 * @param appId
	 *          app packagename
	 * @param versionCode
	 *          app version code
	 * @param offerType
	 *          app offertype
	 * @param callback
	 *          optional callback to notify about progress (may be null).
	 */
	public FetchService(Archive archive, String appId, int versionCode, int offerType,
			FetchListener callback) {
		this.appId = appId;
		this.versionCode = versionCode;
		this.archive = archive;
		this.offerType = offerType;
		this.callback = callback;
	}

	public void run() {
		try {
			service = App.createConnection(archive);
			File target = archive.fileUnder(appId, versionCode);
			InputStream in = service.download(appId, versionCode, offerType);

			target.getParentFile().mkdirs();
			FileOutputStream out = new FileOutputStream(target);
			byte[] buffer = new byte[1024 * 16];
			int length;
			long received = 0;
			callback.onChunk(this, 0);
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
				received += length;
				if (callback != null) {
					if (callback.onChunk(this, received)) {
						out.close();
						in.close();
						target.delete();
						return;
					}
				}
			}
			out.close();
			in.close();
			archive.getDownloadLogger().addEntry(target);
			if (callback != null) {
				callback.onComplete(this);
			}
		}
		catch (Exception e) {
			if (callback != null) {
				callback.onFailure(this, e);
			}
		}
	}
}
