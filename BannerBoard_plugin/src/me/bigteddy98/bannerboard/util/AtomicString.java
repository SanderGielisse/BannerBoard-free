package me.bigteddy98.bannerboard.util;

public class AtomicString {

	private String get;

	public AtomicString() {
		this(null);
	}

	public AtomicString(String s) {
		this.get = s;
	}

	public String get() {
		return get;
	}

	public void set(String get) {
		this.get = get;
	}
}
