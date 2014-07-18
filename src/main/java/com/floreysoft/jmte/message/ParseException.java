package com.floreysoft.jmte.message;


@SuppressWarnings("serial")
public class ParseException extends MessageException {
	public ParseException(Message message) {
		super(message);
	}
}
