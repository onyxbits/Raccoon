package com.floreysoft.jmte.encoder;

import com.floreysoft.jmte.Engine;

/**
 * An encoder from one string to another. Called on all rendered objects coming from
 * the model. Template remains unaffected. Useful e.g. for escaping special characters in output formats like XML or JSON.
 * 
 * @see XMLEncoder
 * @see Engine#setEncoder()
 */
public interface Encoder {
	/**
	 * Encodes a string into another one.
	 * 
	 * @param the
	 *            string to encode
	 * @return the encoded string
	 */
	public String encode(String string);
}
