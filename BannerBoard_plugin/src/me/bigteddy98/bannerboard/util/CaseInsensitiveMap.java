package me.bigteddy98.bannerboard.util;

import java.util.TreeMap;

public class CaseInsensitiveMap<V> extends TreeMap<String, V> {

	private static final long serialVersionUID = 1L;

	public CaseInsensitiveMap(CaseInsensitiveMap<V> old) {
		super(String.CASE_INSENSITIVE_ORDER);
		this.putAll(old);
	}

	public CaseInsensitiveMap() {
		super(String.CASE_INSENSITIVE_ORDER);
	}
}