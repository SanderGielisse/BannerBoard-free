package me.bigteddy98.bannerboard.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.bigteddy98.bannerboard.Main;
import me.bigteddy98.bannerboard.SkinRequest;
import me.bigteddy98.bannerboard.api.SkinType;

public class SkinCache {

	// HEAD_3D("3DHEAD"), HEAD_ONLY("HEAD"), ENTIRE_SKIN("SKIN");

	private final Map<SkinType, SkinRequest> typeLinks;

	public SkinCache(String server) {
		Map<SkinType, SkinRequest> links = new HashMap<>();
		links.put(SkinType.fromName("HEAD"), new SkinRequest(server + "fullskin-%NAME%-640-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-1") {

			@Override
			public BufferedImage pull(BufferedImage image) {
				// cut it%%
				image = image.getSubimage(250, 312, 139, 139); // asp ratio 1:1
				return resize(image);
			}
		});

		links.put(SkinType.fromName("3DHEAD"), new SkinRequest(server + "fullskin-%NAME%-640-344-39-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-1") {

			@Override
			public BufferedImage pull(BufferedImage image) {
				// cut it
				image = image.getSubimage(222, 282, 202, 202); // asp ratio 1:1
				// resize to 128x128
				return resize(image);
			}
		});

		// "+server+"fullskin-sander2798-640-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0
		links.put(SkinType.fromName("SKIN"), new SkinRequest(server + "fullskin-%NAME%-640-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0") {

			@Override
			public BufferedImage pull(BufferedImage image) {
				// cut it
				return image.getSubimage(150, 95, 305, 490);
			}
		});
		typeLinks = Collections.unmodifiableMap(links);
	}

	private static BufferedImage resize(BufferedImage img) {
		BufferedImage d = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = d.createGraphics();
		g.drawImage(img, 0, 0, 128, 128, null);
		g.dispose();
		return d;
	}

	public BufferedImage getSkin(String name, SkinType type) throws IOException {
		SkinRequest r = this.typeLinks.get(type);
		return Main.getInstance().fetchImage(r.getLink().replace("%NAME%", name));
	}
}
