package me.bigteddy98.bannerboard.draw;

import org.bukkit.Bukkit;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageUtil {

	public static Map<String, BufferedImage> loadCache(File imageMap) {
		Map<String, BufferedImage> tmp = new HashMap<>();

		Bukkit.getLogger().info("Loading images...");

		loadFolder(tmp, "", imageMap);
		
		Bukkit.getLogger().info("Successfully loaded " + tmp.size() + " image(s).");

		return tmp;
	}

	private static void loadFolder(Map<String, BufferedImage> tmp, String prefix, File imageMap) {
		// Filter for image files ending with .png
		File[] files = imageMap.listFiles((dir, name) -> name.endsWith(".png"));
		if (files == null) {
			Bukkit.getLogger().info("Skipping " + imageMap.getName() + ". No files found...");
			return;
		}
		
		for (File file : files) {

			if (file.isDirectory()) {
				loadFolder(tmp, prefix + file.getName() + "/", file);
				continue;
			}

			String name = file.getName();
			try {
				tmp.put(prefix + name, ImageIO.read(file));
				Bukkit.getLogger().info("Successfully loaded image " + prefix + name + ".");
			} catch (IOException e) {
				Bukkit.getLogger().warning("Could not load image " + file.getName() + ". " + e.getMessage());
			}
		}
	}
}
