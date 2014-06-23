package com.akdeniz.googleplaycrawler;

import java.io.InputStream;

import com.akdeniz.googleplaycrawler.misc.Base64;


/**
 * Just a container for a a return type.
 * 
 * @author patrick
 * 
 */
public class CryptoBlob {

	public InputStream in;
	public byte[] key;

	/**
	 * New containter
	 * 
	 * @param in
	 *          the inputstream to download from
	 * @param encKey
	 *          base64 encoded encryption key.
	 */
	public CryptoBlob(InputStream in, byte[] encKey) {
		this.in = in;
		this.key = Base64.decode(encKey,Base64.DEFAULT);
	}
}
