package com.akdeniz.googleplaycrawler;

import java.io.InputStream;


/**
 * Just a container for a a return type.
 * 
 * @author patrick
 * 
 */
public class CryptoBlob {

	public InputStream in;
	public byte[] hmac;

	public CryptoBlob(InputStream in, byte[] hmac) {
		this.in = in;
		this.hmac = hmac;
	}
}
