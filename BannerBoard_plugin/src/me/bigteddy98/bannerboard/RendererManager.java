package me.bigteddy98.bannerboard;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import me.bigteddy98.bannerboard.api.CustomRenderer;
import me.bigteddy98.bannerboard.draw.renderer.ClickableRenderer;
import me.bigteddy98.bannerboard.draw.renderer.ColorRenderer;
import me.bigteddy98.bannerboard.draw.renderer.ImageRenderer;
import me.bigteddy98.bannerboard.draw.renderer.LiveImageRenderer;
import me.bigteddy98.bannerboard.draw.renderer.SkinRenderer;
import me.bigteddy98.bannerboard.draw.renderer.TextRenderer;
import me.bigteddy98.bannerboard.draw.renderer.URLImageRenderer;
import me.bigteddy98.bannerboard.util.CaseInsensitiveMap;

public class RendererManager {

	// little thread safety fix here
	private final CaseInsensitiveMap<CustomRenderer> registeredRenderers = new CaseInsensitiveMap<CustomRenderer>();

	public RendererManager() {
		this.registerRenderer("color", new CustomRenderer(Main.getInstance(), false, ColorRenderer.class));
		this.registerRenderer("image", new CustomRenderer(Main.getInstance(), false, ImageRenderer.class));
		this.registerRenderer("text", new CustomRenderer(Main.getInstance(), false, TextRenderer.class));
		this.registerRenderer("skin", new CustomRenderer(Main.getInstance(), true, SkinRenderer.class));
		this.registerRenderer("liveimg", new CustomRenderer(Main.getInstance(), false, LiveImageRenderer.class));
		this.registerRenderer("urlimg", new CustomRenderer(Main.getInstance(), false, URLImageRenderer.class));
		this.registerRenderer("interact", new CustomRenderer(Main.getInstance(), false, ClickableRenderer.class));
	}

	public void registerRenderer(String name, CustomRenderer customRenderer) {
		name = name.toUpperCase();

		synchronized (this.registeredRenderers) {
			if (this.registeredRenderers.containsKey(name)) {
				String owner = this.registeredRenderers.get(name).getPlugin().getName();
				String doubler = customRenderer.getPlugin().getName();
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[BannerBoard] [WARN] Custom renderer " + name + " is already reserved for plugin " + owner + ", so it failed to register for plugin " + doubler + ". You can still use the " + name + " custom renderer from " + doubler + " by using " + doubler + ":" + name + " instead.");
			} else {
				Bukkit.getConsoleSender().sendMessage("[BannerBoard] [INFO] Successfully registered BannerBoard custom renderer " + name + " for plugin " + customRenderer.getPlugin().getName() + "...");
				this.registeredRenderers.put(name, customRenderer);
			}
			this.registeredRenderers.put(customRenderer.getPlugin().getName() + ":" + name, customRenderer);
			Bukkit.getConsoleSender().sendMessage("[BannerBoard] [INFO] Successfully registered BannerBoard custom renderer " + (customRenderer.getPlugin().getName() + ":" + name) + " for plugin " + customRenderer.getPlugin().getName() + "...");
		}
	}

	public Map<String, CustomRenderer> getReadOnlyCopy() {
		synchronized (this.registeredRenderers) {
			return new CaseInsensitiveMap<CustomRenderer>(this.registeredRenderers);
		}
	}
}
