/* 
 * BannerBoard
 * Copyright (C) 2016 Sander Gielisse
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.bigteddy98.bannerboard;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import me.bigteddy98.bannerboard.api.BannerBoardAPI;
import me.bigteddy98.bannerboard.api.BannerBoardRenderer;
import me.bigteddy98.bannerboard.api.CustomRenderer;
import me.bigteddy98.bannerboard.api.PlaceHolder;
import me.bigteddy98.bannerboard.api.SkinType;
import me.bigteddy98.bannerboard.util.DrawUtil;

public class InternalBannerBoardAPI implements BannerBoardAPI {

	@Override
	public void registerPlaceHolder(String name, PlaceHolder placeHolder) {
		check();
		nameCheck(name);
		Main.getInstance().placeHolderManager.registerPlaceHolder(name, placeHolder);
	}

	@Override
	public Map<String, PlaceHolder> getRegisteredPlaceHolders() {
		return Main.getInstance().placeHolderManager.getReadOnlyCopy();
	}

	@Override
	public void registerCustomRenderer(String name, CustomRenderer customRenderer) {
		check();
		nameCheck(name);
		Main.getInstance().rendererManager.registerRenderer(name, customRenderer);
	}

	@Override
	public void registerCustomRenderer(String name, Plugin plugin, boolean doLoadSkinCache, Class<? extends BannerBoardRenderer<?>> customRenderer) {
		this.registerCustomRenderer(name, new CustomRenderer(plugin, doLoadSkinCache, customRenderer));
	}

	@Override
	public Map<String, CustomRenderer> getRegisteredRenderers() {
		return Main.getInstance().rendererManager.getReadOnlyCopy();
	}

	@Override
	public BufferedImage getCachedSkin(String name, SkinType type) {
		return null;
	}

	@Override
	public boolean hasCachedSkin(String name, SkinType type) {
		return false;
	}

	@Override
	public BufferedImage getLoadedImage(String fileName) {
		if (!Main.getInstance().cachedImages.containsKey(fileName)) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[WARNING] [BannerBoard] Could not find file /plugins/BannerBoard/images/" + fileName + ".");
			return null;
		}
		return Main.getInstance().cachedImages.get(fileName);
	}

	@Override
	public boolean hasLoadedImage(String fileName) {
		return Main.getInstance().cachedImages.containsKey(fileName);
	}

	@Override
	public BufferedImage drawFancyText(int width, int height, String text, Font font, Color textColor, Color strokeColor, int strokeThickness, Integer xOffset, Integer yOffset) {
		return DrawUtil.drawFancyText(width, height, text, font, textColor, strokeColor, strokeThickness, xOffset, yOffset);
	}

	// some static methods
	private static void check() {
		if (!Bukkit.isPrimaryThread()) {
			throw new UnsupportedOperationException("The BannerBoard API can only be accessed from the primary Bukkit thread");
		}
	}

	private static Pattern notAllowed = Pattern.compile("[^a-z0-9_ ]", Pattern.CASE_INSENSITIVE);

	private static void nameCheck(String s) {
		if (notAllowed.matcher(s).find()) {
			throw new IllegalArgumentException("BannerBoard names may not contain any special characters");
		}
	}

	@Override
	public String applyPlaceholders(Player p, String text) {
		return Main.getInstance().applyPlaceholders(text, p);
	}

	@Override
	public int getFrameIndex(ItemFrame frame) {
		check();
		for (BannerBoard board : Main.getInstance().memoryManager.getLoadedBannerBoards()) {
			List<ItemFrame> frames = board.buildItemFrameList();
			for (int i = 0; i < frames.size(); i++) {
				if (frames.get(i).getUniqueId().equals(frame.getUniqueId())) {
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	public int getBannerBoardId(ItemFrame frame) {
		for (BannerBoard board : Main.getInstance().memoryManager.getLoadedBannerBoards()) {
			for (ItemFrame f : board.buildItemFrameList()) {
				if (f.getUniqueId().equals(frame.getUniqueId())) {
					return board.getId();
				}
			}
		}
		return -1;
	}

	@Override
	public BufferedImage fetchImage(String url) {
		try {
			return Main.getInstance().fetchImage(url);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
