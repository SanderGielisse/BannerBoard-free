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
package me.bigteddy98.bannerboard.api;

import org.bukkit.entity.Player;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public abstract class BannerBoardRenderer<T> {

	private static int counter = 0;

	private final int id = counter++;
	private final List<Setting> settings;
	private final int allowedWidth;
	private final int allowedHeight;

	public BannerBoardRenderer(List<Setting> parameters, int allowedWidth, int allowedHeight) {
		this.settings = parameters;
		this.allowedWidth = allowedWidth;
		this.allowedHeight = allowedHeight;
	}

	public void render(Player p, BufferedImage image, Graphics2D g, T preparation) {

	}

	public void render(Player p, BufferedImage image, Graphics2D g) {

	}

	public T asyncRenderPrepare(Player p) throws Exception {
		// doesn't do anything by default, can be used to be overwritten
		return null;
	}

	public int getAllowedWidth() {
		return allowedWidth;
	}

	public int getAllowedHeight() {
		return allowedHeight;
	}

	public List<Setting> getSettings() {
		return settings;
	}

	public Setting getSetting(String name) {
		for (Setting s : this.settings) {
			if (s.getName().equalsIgnoreCase(name)) {
				return s;
			}
		}
		return null;
	}

	public boolean hasSetting(String name) {
		for (Setting s : this.getSettings()) {
			if (s.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	public Color decodeColor(String color) {
		String[] split = color.replace(" ", "").split(",");
		int red = Integer.parseInt(split[0]);
		int green = Integer.parseInt(split[1]);
		int blue = Integer.parseInt(split[2]);
		int alpha = 255;
		if (split.length > 3) {
			alpha = Integer.parseInt(split[3]);
		}
		return new Color(red, green, blue, alpha);
	}

	public int getId() {
		return id;
	}
}
