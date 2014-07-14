package com.akdeniz.googleplaycrawler;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.akdeniz.googleplaycrawler.GooglePlay.AndroidAppDeliveryData;
import com.akdeniz.googleplaycrawler.GooglePlay.HttpCookie;
import com.akdeniz.googleplaycrawler.misc.Base64;

public class DownloadData {

	private AndroidAppDeliveryData appDeliveryData;
	private String downloadUrl;
	private HttpCookie downloadAuthCookie;
	private GooglePlayAPI api;

	public DownloadData(GooglePlayAPI api, AndroidAppDeliveryData appDeliveryData) {
		this.appDeliveryData = appDeliveryData;
		this.api = api;
		this.downloadUrl = appDeliveryData.getDownloadUrl();
		this.downloadAuthCookie = appDeliveryData.getDownloadAuthCookie(0);
	}

	/**
	 * Access the APK file
	 * 
	 * @return an inputstream from which the app can be read (already processed
	 *         through crypto).
	 * @throws NoSuchPaddingException
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 */
	public InputStream openApp() throws IOException, NoSuchAlgorithmException,
			NoSuchProviderException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException {
		InputStream ret = api.executeDownload(downloadUrl, downloadAuthCookie.getName() + "="
				+ downloadAuthCookie.getValue());
		if (appDeliveryData.hasEncryptionParams()) {
			int version = ret.read();
			if (version != 0) {
				throw new IOException("Unknown crypto container!");
			}
			ret.skip(4); // Meta data
			byte[] iv = new byte[16];
			ret.read(iv);
			byte[] encoded = appDeliveryData.getEncryptionParams().getEncryptionKey().getBytes("UTF-8");
			byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
			SecretKeySpec key = new SecretKeySpec(decoded, "AES");
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
			return new CipherInputStream(ret, cipher);
		}
		else {
			return ret;
		}
	}

	/**
	 * Access the first expansion
	 * 
	 * @return a stream or null if there is no expansion.
	 */
	public InputStream openMainExpansion() throws IOException {
		if (appDeliveryData.getAdditionalFileCount() < 1) {
			return null;
		}
		String url = appDeliveryData.getAdditionalFile(0).getDownloadUrl();
		return api.executeDownload(url,
				downloadAuthCookie.getName() + "=" + downloadAuthCookie.getValue());
	}

	/**
	 * Access the second expansion
	 * 
	 * @return a stream or null if there is no expansion.
	 */
	public InputStream openPatchExpansion() throws IOException {
		if (appDeliveryData.getAdditionalFileCount() < 2) {
			return null;
		}
		String url = appDeliveryData.getAdditionalFile(1).getDownloadUrl();
		return api.executeDownload(url,
				downloadAuthCookie.getName() + "=" + downloadAuthCookie.getValue());
	}

	public int getPatchFileVersion() {
		if (appDeliveryData.getAdditionalFileCount()>1) {
			return appDeliveryData.getAdditionalFile(1).getVersionCode();
		}
		return -1;
	}

	public int getMainFileVersion() {
		if (appDeliveryData.getAdditionalFileCount()>0) {
			return appDeliveryData.getAdditionalFile(0).getVersionCode();
		}
		return -1;
	}
}
