package me.bigteddy98.bannerboard;

public class ConfigException extends Exception {

	private static final long serialVersionUID = -2994578589100338882L;

	public ConfigException() {
		super();
	}

	public ConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigException(String message) {
		super(message);
	}

	public ConfigException(Throwable cause) {
		super(cause);
	}
}
