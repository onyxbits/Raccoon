package com.floreysoft.jmte.token;

public abstract class AbstractToken implements Token {

	protected String text;
	protected int line;
	protected int column;
	protected String sourceName;
	private int tokenIndex;

	public AbstractToken() {
	}

	public AbstractToken(AbstractToken token) {
		this.text = token.text;
		this.line = token.line;
		this.column = token.column;
		this.sourceName = token.sourceName;
		this.setTokenIndex(token.getTokenIndex());
	}

	public AbstractToken(char[] buffer, int start, int end, int tokenIndex) {
		this(null, buffer, start, end, tokenIndex);
	}

	public AbstractToken(String sourceName, char[] buffer, int start, int end,
			int tokenIndex) {
		this.setSourceName(sourceName);
		setText(buffer, start, end);
		setLine(buffer, start, end);
		setColumn(buffer, start, end);
		this.setTokenIndex(tokenIndex);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public void setText(char[] buffer, int start, int end) {
		setText(new String(buffer, start, end - start));
	}

	public void setLine(char[] buffer, int start, int end) {
		line = 1;
		for (int i = 0; i < start; i++) {
			if (buffer[i] == '\n') {
				line++;
			}
		}
	}

	public void setColumn(char[] buffer, int start, int end) {
		column = 0;
		if (buffer.length != 0) {
			for (int i = start; i >= 0; i--) {
				if (buffer[i] == '\n') {
					break;
				} else {
					column++;
				}
			}
		}
	}

	@Override
	public String toString() {
		return getText();
	}

	@Override
	public String emit() {
		return getText();
	}
	
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	@Override
	public String getSourceName() {
		return sourceName;
	}
	
	@Override
	public int getTokenIndex() {
		return tokenIndex;
	}

	public void setTokenIndex(int tokenIndex) {
		this.tokenIndex = tokenIndex;
	}
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}