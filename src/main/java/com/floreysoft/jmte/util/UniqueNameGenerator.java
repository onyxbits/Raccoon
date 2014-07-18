package com.floreysoft.jmte.util;

import java.util.HashMap;
import java.util.Map;

public class UniqueNameGenerator<F, T> {

	private final Map<F, T> translations = new HashMap<F, T>();

	private final String prefix;
	private long cnt;

	public UniqueNameGenerator(String prefix, long startCnt) {
		this.prefix = prefix;
		this.cnt = startCnt;
	}

	public UniqueNameGenerator(String prefix) {
		this(prefix, 0);
	}

	public UniqueNameGenerator() {
		this("N", 0);
	}

	public String nextUniqueName() {
		return prefix + ++cnt;
	}

	public String currentUniqueName() {
		return prefix + cnt;
	}
	
	public void registerTranslation(F from, T to) {
		translations.put(from, to);
	}

	public T lookupTranslation(F from) {
		return translations.get(from);
	}

	public boolean hasTranslation(F from) {
		return translations.containsKey(from);
	}

}
