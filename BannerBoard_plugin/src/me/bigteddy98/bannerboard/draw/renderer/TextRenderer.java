package me.bigteddy98.bannerboard.draw.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.bigteddy98.bannerboard.Main;
import me.bigteddy98.bannerboard.api.BannerBoardRenderer;
import me.bigteddy98.bannerboard.api.DisableBannerBoardException;
import me.bigteddy98.bannerboard.api.FontStyle;
import me.bigteddy98.bannerboard.api.Setting;
import me.bigteddy98.bannerboard.util.AtomicString;
import me.bigteddy98.bannerboard.util.DrawUtil;

public class TextRenderer extends BannerBoardRenderer<Void> {

	public TextRenderer(List<Setting> parameters, int allowedWidth, int allowedHeight) {
		super(parameters, allowedWidth, allowedHeight);

		// -text Welkom %name% -font Adobe Caslon Pro -size 25 -color 0,0,0,100
		// -style bold -strokeColor 100,100,100,100 -strokeThickness 3
		// -strokeSensitivity 5
		// -xOffset

		if (!this.hasSetting("text")) {
			throw new DisableBannerBoardException("Renderer TEXT did not have a valid text parameter, renderer disabled...");
		}

		String randomFont = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()[0];

		if (!this.hasSetting("font")) {
			parameters.add(new Setting("font", randomFont));
		}

		if (!Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()).contains(this.getSetting("font").getValue())) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[WARNING] [BannerBoard] Renderer TEXT has an unknown font value, " + this.getSetting("font").getValue() + ", using random font " + randomFont + "...");
			parameters.add(new Setting("font", randomFont));
		}

		if (!this.hasSetting("size")) {
			parameters.add(new Setting("size", "20"));
		}
		try {
			Integer.parseInt(this.getSetting("size").getValue());
		} catch (NumberFormatException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[WARNING] [BannerBoard] Renderer TEXT has an invalid size value, " + this.getSetting("size").getValue() + ", using default size 20...");
			this.getSetting("size").setValue("20");
		}

		if (!this.hasSetting("color")) {
			parameters.add(new Setting("color", "255,255,255"));
		}
		if (!this.hasSetting("style")) {
			parameters.add(new Setting("style", "PLAIN"));
		}
		try {
			FontStyle.valueOf(this.getSetting("style").getValue().toUpperCase());
		} catch (Exception e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[WARNING] [BannerBoard] Renderer TEXT has an invalid style value, " + this.getSetting("style").getValue() + ", using default style PLAIN...");
			this.getSetting("style").setValue("PLAIN");
		}

		if (!this.hasSetting("strokeColor")) {
			parameters.add(new Setting("strokeColor", "0,0,0"));
		}
		if (!this.hasSetting("strokeThickness")) {
			parameters.add(new Setting("strokeThickness", "0"));
		}
		try {
			Integer.parseInt(this.getSetting("strokeThickness").getValue());
		} catch (NumberFormatException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[WARNING] [BannerBoard] Renderer TEXT has an invalid strokeThickness value, " + this.getSetting("strokeThickness").getValue() + ", using default thickness 0...");
			this.getSetting("strokeThickness").setValue("0");
		}
		if (!this.hasSetting("xOffset")) {
			parameters.add(new Setting("xOffset", "CENTERED"));
		}
		if (!this.hasSetting("yOffset")) {
			parameters.add(new Setting("yOffset", "CENTERED"));
		}
	}

	@Override
	public void render(final Player p, BufferedImage image, Graphics2D g) {
		final String without = this.getSetting("text").getValue();

		final Object lock = new Object();
		final AtomicString atomicWith = new AtomicString();

		// must be done from Bukkit thread
		new BukkitRunnable() {

			@Override
			public void run() {
				synchronized (lock) {
					atomicWith.set(Main.getInstance().applyPlaceholders(without.replace("%slide%", getSetting("slide").getValue()), p));
					lock.notifyAll();
				}
			}
		}.runTask(Main.getInstance());

		synchronized (lock) {
			try {
				while (atomicWith.get() == null) {
					lock.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		int size = Integer.parseInt(this.getSetting("size").getValue());

		String fontName = this.getSetting("font").getValue();
		Font font = new Font(fontName, FontStyle.valueOf(this.getSetting("style").getValue().toUpperCase()).getId(), size);

		Color textColor = this.decodeColor(this.getSetting("color").getValue());
		Color blurColor = this.decodeColor(this.getSetting("strokeColor").getValue());
		int strokeThickness = Integer.parseInt(this.getSetting("strokeThickness").getValue());

		Integer xOffset = null;
		if (!this.getSetting("xOffset").getValue().equalsIgnoreCase("CENTERED")) {
			xOffset = Integer.parseInt(this.getSetting("xOffset").getValue());
		}

		Integer yOffset = null;
		if (!this.getSetting("yOffset").getValue().equalsIgnoreCase("CENTERED")) {
			yOffset = Integer.parseInt(this.getSetting("yOffset").getValue());
		}

		g.drawImage(DrawUtil.drawFancyText(image.getWidth(), image.getHeight(), atomicWith.get(), font, textColor, blurColor, strokeThickness, xOffset, yOffset), 0, 0, null);
	}
}
