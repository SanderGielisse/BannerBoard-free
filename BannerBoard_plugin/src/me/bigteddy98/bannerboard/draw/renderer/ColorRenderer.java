package me.bigteddy98.bannerboard.draw.renderer;

import me.bigteddy98.bannerboard.api.BannerBoardRenderer;
import me.bigteddy98.bannerboard.api.Setting;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class ColorRenderer extends BannerBoardRenderer<Void> {

	public ColorRenderer(List<Setting> parameters, int allowedWidth, int allowedHeight) {
		super(parameters, allowedWidth, allowedHeight);

		if (!this.hasSetting("color")) {
			Bukkit.getLogger().warning("Renderer COLOR did not have a valid color parameter, using the default -color 0,0,0,255 now...");
			parameters.add(new Setting("color", "0,0,0,255"));
		}
	}

	@Override
	public void render(Player p, BufferedImage image, Graphics2D g) {
		// decode the color
		String color = this.getSetting("color").getValue();

		g.setColor(this.decodeColor(color));
		g.fillRect(0, 0, this.getAllowedWidth(), this.getAllowedHeight());
	}
}
