package me.bigteddy98.bannerboard;

import java.awt.image.BufferedImage;
import java.io.IOException;

public abstract class SkinRequest {

	private final String link;

	public SkinRequest(String link) {
		this.link = link;
	}

	public String getLink() {
		return link;
	}

	public abstract BufferedImage pull(BufferedImage image) throws IOException;
}
