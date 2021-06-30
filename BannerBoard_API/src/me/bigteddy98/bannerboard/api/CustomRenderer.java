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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.bukkit.plugin.Plugin;

public class CustomRenderer {

	private final Plugin plugin;
	private final boolean doLoadSkinCache;
	private final Class<? extends BannerBoardRenderer> customRenderer;

	public CustomRenderer(Plugin plugin, boolean doLoadSkinCache, Class<? extends BannerBoardRenderer> customRenderer) {
		this.plugin = plugin;
		this.doLoadSkinCache = doLoadSkinCache;
		this.customRenderer = customRenderer;
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public boolean isDoLoadSkinCache() {
		return doLoadSkinCache;
	}

	public Class<? extends BannerBoardRenderer> getCustomRenderer() {
		return customRenderer;
	}

	public BannerBoardRenderer create(List<Setting> parameters, int allowedWidth, int allowedHeight) throws IncorrectBannerBoardConstructorException, DisableBannerBoardException {
		try {
			return this.customRenderer.getConstructor(List.class, Integer.TYPE, Integer.TYPE).newInstance(parameters, allowedWidth, allowedHeight);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IncorrectBannerBoardConstructorException(e);
		}
	}
}
