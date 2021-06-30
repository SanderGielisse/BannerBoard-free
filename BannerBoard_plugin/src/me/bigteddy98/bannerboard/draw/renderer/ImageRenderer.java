package me.bigteddy98.bannerboard.draw.renderer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import org.bukkit.entity.Player;

import me.bigteddy98.bannerboard.api.BannerBoardManager;
import me.bigteddy98.bannerboard.api.BannerBoardRenderer;
import me.bigteddy98.bannerboard.api.DisableBannerBoardException;
import me.bigteddy98.bannerboard.api.Setting;

public class ImageRenderer extends BannerBoardRenderer<Void> {

	public ImageRenderer(List<Setting> parameters, int allowedWidth, int allowedHeight) {
		super(parameters, allowedWidth, allowedHeight);

		if (!this.hasSetting("src")) {
			throw new DisableBannerBoardException("Renderer IMAGE did not have a valid src parameter, renderer disabled...");
		}

		// try loading the image
		if (this.hasSetting("src")) {
			String name = this.getSetting("src").getValue();
			if (!BannerBoardManager.getAPI().hasLoadedImage(name)) {
				throw new DisableBannerBoardException("Renderer IMAGE could not find image " + name + ", renderer disabled...");
			}
		}
	}

	@Override
	public void render(Player p, BufferedImage image, Graphics2D g) {
		Setting setting = this.getSetting("src");

		Integer xOffset = null;
		Integer yOffset = null;

		if (this.hasSetting("xOffset")) {
			xOffset = Integer.parseInt(this.getSetting("xOffset").getValue());
		}
		if (this.hasSetting("yOffset")) {
			yOffset = Integer.parseInt(this.getSetting("yOffset").getValue());
		}

		BufferedImage tbd = BannerBoardManager.getAPI().getLoadedImage(setting.getValue());
		int width = tbd.getWidth();
		int height = tbd.getHeight();

		if (this.hasSetting("width")) {
			width = Integer.parseInt(this.getSetting("width").getValue());
		}
		if (this.hasSetting("height")) {
			height = Integer.parseInt(this.getSetting("height").getValue());
		}

		// fix the possible yOffset and xOffset null
		if (xOffset == null) {
			xOffset = (image.getWidth() / 2) - (width / 2);
		}
		if (yOffset == null) {
			yOffset = (image.getHeight() / 2) - (height / 2);
		}

		g.drawImage(tbd, xOffset, yOffset, width, height, null);
	}
}
