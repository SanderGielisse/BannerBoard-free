package me.bigteddy98.bannerboard.config;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import me.bigteddy98.bannerboard.BannerBoard;
import me.bigteddy98.bannerboard.Main;
import me.bigteddy98.bannerboard.api.BannerBoardRenderer;
import me.bigteddy98.bannerboard.api.CustomRenderer;
import me.bigteddy98.bannerboard.api.IncorrectBannerBoardConstructorException;
import me.bigteddy98.bannerboard.api.Setting;
import me.bigteddy98.bannerboard.util.SizeUtil;

public class ConfigurationManager {

	private Main plugin;
	private final List<String> defaultRenderers = new ArrayList<>();

	{
		this.defaultRenderers.add("COLOR -color 0,0,255");
		this.defaultRenderers.add("TEXT -text TEST %name% TEST -size 12 -font " + GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()[0] + " -color 255,255,255 -strokeColor 0,0,0 -strokeThickness 0 -xOffset 10 -yOffset 70");
	}

	public void init(Main plugin) {
		this.plugin = plugin;

		plugin.getConfig().options().copyDefaults(true);
		plugin.saveConfig();
	}

	public void saveBannerBoard(BannerBoard board) {
		plugin.getConfig().set("bannerboards." + board.getId() + ".internal.frames", LocationSerializer.serializeList(board.getLocationList()));
		plugin.getConfig().set("bannerboards." + board.getId() + ".internal.facing", board.getFace().name());
		plugin.getConfig().set("bannerboards." + board.getId() + ".internal.width", board.getWidth());
		plugin.getConfig().set("bannerboards." + board.getId() + ".internal.height", board.getHeight());
		plugin.getConfig().set("bannerboards." + board.getId() + ".internal.rotation", board.getRotation());
		plugin.getConfig().set("bannerboards." + board.getId() + ".configurable.renderers", new ArrayList<>(this.defaultRenderers));
		plugin.saveConfig();
	}

	public void deleteBannerBoard(BannerBoard board) {
		plugin.getConfig().set("bannerboards." + board.getId(), null);
		plugin.saveConfig();
	}

	public int newId() {
		int oldId = this.plugin.getConfig().getInt("lastUsedId");
		int newId = oldId + 1;

		this.plugin.getConfig().set("lastUsedId", newId);
		this.plugin.saveConfig();

		return newId;
	}

	public void loadAll() {
		if (!this.plugin.getConfig().contains("bannerboards")) {
			return;
		}

		for (String id : this.plugin.getConfig().getConfigurationSection("bannerboards").getKeys(false)) {
			List<String> frames = plugin.getConfig().getStringList("bannerboards." + id + ".internal.frames");
			BlockFace face = BlockFace.valueOf(plugin.getConfig().getString("bannerboards." + id + ".internal.facing"));

			final List<Location> locFrames = LocationSerializer.deserializeList(frames);
			if (!plugin.getConfig().contains("bannerboards." + id + ".internal.width")) {
				// calculate and set

				plugin.getConfig().set("bannerboards." + id + ".internal.width", SizeUtil.getWidth(locFrames));
				plugin.getConfig().set("bannerboards." + id + ".internal.height", SizeUtil.getHeight(locFrames));
				plugin.getConfig().set("bannerboards." + id + ".internal.rotation", 0);
				plugin.saveConfig();
			}

			int width = plugin.getConfig().getInt("bannerboards." + id + ".internal.width");
			int height = plugin.getConfig().getInt("bannerboards." + id + ".internal.height");
			int rotation = plugin.getConfig().getInt("bannerboards." + id + ".internal.rotation");

			BannerBoard board = new BannerBoard(Integer.parseInt(id), locFrames, face, width, height, rotation);

			// add all renderers
			List<String> renderers = plugin.getConfig().getStringList("bannerboards." + id + ".configurable.renderers");

			int totalIndex = -1;
			int slide = 0;
			for (String renderer : renderers) {
				if (renderer == null || renderer.equals("")) {
					continue;
				}
				totalIndex++;
				// at least we have a string
				String type = renderer.split(" ")[0].toUpperCase();

				Map<String, CustomRenderer> registeredRenderers = Main.getInstance().rendererManager.getReadOnlyCopy();

				if (type.equalsIgnoreCase("SLIDEDELAY")) {
					if (totalIndex != 0) {
						plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[WARNING] [BannerBoard] SLIDESETTINGS must been the first renderer defined for bannerboard with ID " + id + ".");
						continue;
					}
					String delay = renderer.split(" ")[1];

					try {
						int secs = Integer.parseInt(delay);
						if (secs < 1) {
							throw new NumberFormatException();
						}
						board.startRunnable(secs);
					} catch (NumberFormatException e) {
						plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[WARNING] [BannerBoard] SLIDESETTINGS must be directly followed by a number (" + delay + " is not a number), error for bannerboard with ID " + id + ".");
					}
					continue;
				} else if (type.equalsIgnoreCase("NEXTSLIDE")) {
					slide++;
					continue;
				} else if (!registeredRenderers.containsKey(type)) {
					plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[WARNING] [BannerBoard] Renderer with type " + type + " was not found for bannerboard with ID " + id + ".");
					continue;
				}

				final List<Setting> settings = new ArrayList<>();

				String[] data = renderer.split(" -");
				for (int i = 1; i < data.length; i++) {
					String setting = data[i];
					// for example gives us font Adobe Caslon Pro

					String keyName = setting.split(" ")[0];
					String value = setting.replace(keyName + " ", "");

					settings.add(new Setting(keyName, value.replace("\\", "")));
				}

				// constructor has arguments List<Setting> parameters, int
				// allowedWidth, int allowedHeight
				try {
					settings.add(new Setting("bannerId", board.getId() + ""));
					settings.add(new Setting("slide", (slide + 1) + ""));
					BannerBoardRenderer<?> tmp = registeredRenderers.get(type).create(settings, board.getPixelWidthWithRotation(), board.getPixelHeightWithRotation());
					board.addTopRenderer(slide, tmp);
				} catch (IncorrectBannerBoardConstructorException e) {
					if (e.getCause() instanceof InvocationTargetException) {
						plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[WARNING] [BannerBoard] BannerBoard with ID " + id + " was disabled, because it encountered an exception. " + e.getCause().getCause().getMessage());
						e.getCause().printStackTrace();
						e.getCause().getCause().printStackTrace();
					} else {
						e.printStackTrace();
					}
				}
			}
			board.setSlides(slide + 1);
			plugin.memoryManager.load(board);
		}
	}
}
