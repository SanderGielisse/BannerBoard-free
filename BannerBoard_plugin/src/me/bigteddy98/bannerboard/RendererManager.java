package me.bigteddy98.bannerboard;

import me.bigteddy98.bannerboard.api.CustomRenderer;
import me.bigteddy98.bannerboard.draw.renderer.*;
import me.bigteddy98.bannerboard.util.CaseInsensitiveMap;
import org.bukkit.Bukkit;

import java.util.Map;

public class RendererManager {

	// little thread safety fix here
	private final CaseInsensitiveMap<CustomRenderer> registeredRenderers = new CaseInsensitiveMap<>();

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
				Bukkit.getLogger().warning("Failed to register custom renderer " + name + " for plugin " + doubler + " as it is already reserved for plugin " + owner + ". You can still use the renderer from " + doubler + " by using " + doubler + ":" + name + " instead.");
			} else {
				Bukkit.getLogger().info("Successfully registered custom renderer " + name + " for plugin " + customRenderer.getPlugin().getName() + "...");
				this.registeredRenderers.put(name, customRenderer);
			}
			this.registeredRenderers.put(customRenderer.getPlugin().getName() + ":" + name, customRenderer);
			Bukkit.getLogger().info("Successfully registered custom renderer" + customRenderer.getPlugin().getName() + ":" + name + " for plugin " + customRenderer.getPlugin().getName() + "...");
		}
	}

	public Map<String, CustomRenderer> getReadOnlyCopy() {
		synchronized (this.registeredRenderers) {
			return new CaseInsensitiveMap<>(this.registeredRenderers);
		}
	}
}
