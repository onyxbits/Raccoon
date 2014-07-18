package com.floreysoft.jmte.encoder;

/**
 * Encodes all XML special characters as described
 * <a href="http://stackoverflow.com/questions/1091945/where-can-i-get-a-list-of-the-xml-document-escape-characters">
 * http://stackoverflow.com/questions/1091945/where-can-i-get-a-list-of-the-xml-document-escape-characters</a>
 * 
 * @see http://stackoverflow.com/questions/1091945/where-can-i-get-a-list-of-the-xml-document-escape-characters
 *
 */
public class XMLEncoder implements Encoder {

	@Override
	public String encode(String string) {
		StringBuilder sb = new StringBuilder((int) (string.length() * 1.2));
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			switch (c) {
			case '&':
				sb.append("&amp;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			case '\'':
				sb.append("&apos;");
				break;
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
