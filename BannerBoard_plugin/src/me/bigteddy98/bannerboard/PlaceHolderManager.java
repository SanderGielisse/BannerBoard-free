package me.bigteddy98.bannerboard;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.bigteddy98.bannerboard.api.PlaceHolder;

public class PlaceHolderManager {

	// little thread safety fix here
	private final Map<String, PlaceHolder> registeredPlaceHolders = new HashMap<>();

	public PlaceHolderManager() {
		// register inbuilt placeholders
		this.registerPlaceHolder("name", new PlaceHolder(Main.getInstance()) {

			@Override
			public String onReplace(Player viewer) {
				return viewer.getName();
			}
		});
		this.registerPlaceHolder("uuid", new PlaceHolder(Main.getInstance()) {

			@Override
			public String onReplace(Player viewer) {
				return viewer.getUniqueId().toString();
			}
		});
	}

	public void registerPlaceHolder(String name, PlaceHolder p) {
		synchronized (this.registeredPlaceHolders) {
			if (this.registeredPlaceHolders.containsKey(name)) {
				String owner = this.registeredPlaceHolders.get(name).getPlugin().getName();
				String doubler = p.getPlugin().getName();
				Bukkit.getLogger().warning("Failed to register Placeholder %" + name + "% for plugin " + doubler + " as it is already reserved for plugin " + owner + ". You can still use the placeholder from " + doubler + " by using %" + doubler + ":" + name + "% instead.");
			} else {
				Bukkit.getLogger().info("Successfully registered BannerBoard placeholder %" + name + "% for plugin " + p.getPlugin().getName() + "...");
				this.registeredPlaceHolders.put(name, p);
			}
			this.registeredPlaceHolders.put(p.getPlugin().getName() + ":" + name, p);
			Bukkit.getLogger().info("Successfully registered BannerBoard placeholder %" + p.getPlugin().getName() + ":" + name + "% for plugin " + p.getPlugin().getName() + "...");
		}
	}

	public Map<String, PlaceHolder> getReadOnlyCopy() {
		synchronized (this.registeredPlaceHolders) {
			return new HashMap<>(this.registeredPlaceHolders);
		}
	}
}
