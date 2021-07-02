package me.bigteddy98.bannerboard.util;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.bigteddy98.bannerboard.api.SkinType;

// TODO: Is this actually used?
public class SkinData {

	private final UUID uuid;
	private final String playerName;
	private final Map<SkinType, BufferedImage> cachedImages = Collections.synchronizedMap(new HashMap<>());

	public SkinData(UUID uuid, String playerName) {
		this.uuid = uuid;
		this.playerName = playerName;
	}

	public UUID getUUID() {
		return uuid;
	}

	public String getPlayerName() {
		return playerName;
	}

	public Map<SkinType, BufferedImage> getCachedImages() {
		return cachedImages;
	}
}
