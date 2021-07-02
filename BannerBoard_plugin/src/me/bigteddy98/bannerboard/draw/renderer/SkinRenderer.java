package me.bigteddy98.bannerboard.draw.renderer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.bigteddy98.bannerboard.Main;
import me.bigteddy98.bannerboard.api.BannerBoardRenderer;
import me.bigteddy98.bannerboard.api.Setting;
import me.bigteddy98.bannerboard.api.SkinType;

public class SkinRenderer extends BannerBoardRenderer<BufferedImage> {

	public SkinRenderer(List<Setting> parameters, int allowedWidth, int allowedHeight) {
		super(parameters, allowedWidth, allowedHeight);

		// type - 3DHEAD HEAD FACE
		// - xOffset
		// - yOffset
		// - width
		// - height

		if (!this.hasSetting("type")) {
			parameters.add(new Setting("type", "HEAD"));
		}

		String type = this.getSetting("type").getValue();
		if (!type.equals("3DHEAD") && !type.equals("HEAD") && !type.equals("SKIN")) {
			Bukkit.getLogger().warning("Renderer SKIN did not have a value type parameter " + type + ", using default HEAD now...");
			this.getSetting("type").setValue("HEAD");
		}

		if (!this.hasSetting("xOffset")) {
			parameters.add(new Setting("xOffset", "CENTERED"));
		}
		if (!this.hasSetting("yOffset")) {
			parameters.add(new Setting("yOffset", "CENTERED"));
		}
	}

	@Override
	public BufferedImage asyncRenderPrepare(Player p) throws Exception {
		SkinType type = SkinType.fromName(this.getSetting("type").getValue());
		try {
			return Main.getInstance().skinCache.getSkin(p.getName(), type);
		} catch (RuntimeException e) {
			Bukkit.getLogger().warning("Skin could not be downloaded for player " + p.getName() + ". " + e.getMessage());
			return null;
		}
	}

	@Override
	public void render(Player p, BufferedImage image, Graphics2D g, BufferedImage skin) {
		if (skin == null) {
			return;
		}

		Integer xOffset = null;
		if (!this.getSetting("xOffset").getValue().equalsIgnoreCase("CENTERED")) {
			xOffset = Integer.parseInt(this.getSetting("xOffset").getValue());
		}

		Integer yOffset = null;
		if (!this.getSetting("yOffset").getValue().equalsIgnoreCase("CENTERED")) {
			yOffset = Integer.parseInt(this.getSetting("yOffset").getValue());
		}

		int width = skin.getWidth();
		if (this.hasSetting("width")) {
			width = Integer.parseInt(this.getSetting("width").getValue());
		}

		int height = skin.getHeight();
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

		g.drawImage(skin, xOffset, yOffset, width, height, null);
	}
}
