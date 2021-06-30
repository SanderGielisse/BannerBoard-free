package me.bigteddy98.bannerboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * @author Sander
 * All this work is a workaround for MC bug https://bugs.mojang.com/browse/MC-46345 which also applies to BungeeCord server switching
 */
public class IdManager {

	private final List<Short> ids = new ArrayList<>();

	public void load(int idCountStart, int amount) throws ConfigException, IOException {
		for (int i = 0; i < amount; i++) {
			short id = (short) (idCountStart + i);
			Mapping.claimId(id);
			this.ids.add(id);
		}
	}

	public short getId() {
		if (this.ids.isEmpty()) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[WARNING] [BannerBoard] No ID's free, remove banners or make your ID range bigger.");
			throw new RuntimeException("No ID's free, remove banners or make your ID range bigger");
		}
		return this.ids.remove(0);
	}

	public void clear() {
		this.ids.clear();
	}
}
