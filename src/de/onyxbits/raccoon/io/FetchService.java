package de.onyxbits.raccoon.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.akdeniz.googleplaycrawler.GooglePlayAPI;

import de.onyxbits.raccoon.App;

/**
 * For directly downloading an APK file without going through search (the user
 * must discover packagename, versioncode and offertype by other means).
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

	public FetchService(Archive archive, String appId, int versionCode, int offerType) {
		this.appId = appId;
		this.versionCode = versionCode;
		this.archive = archive;
		this.offerType = offerType;
	}

	public void run() {
		try {
			System.err.println("# Login...");
			service = App.createConnection(archive);
			File target = archive.fileUnder(appId, versionCode);
			InputStream in = service.download(appId, versionCode, offerType);
			System.err.println("# Downloading: "+target.getAbsolutePath());

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
			System.err.println("# Success");
		}
		catch (Exception e) {
			System.err.println("# Error: "+e.getMessage());
		}
	}
}
