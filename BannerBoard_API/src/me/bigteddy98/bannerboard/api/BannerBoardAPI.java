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

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.Map;

import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface BannerBoardAPI {

	/**
	 * Registers a new placeholder for your plugin. Do not use any special characters in the name. The % character should not be included for the name.
	 * @param name the name of the placeholder, without the % character. This should not contain any special characters.
	 * @param placeHolder an instance of a placeholder interface.
	 */
	public void registerPlaceHolder(String name, PlaceHolder placeHolder);

	/**
	 * 
	 * @return a map containing all registered placeholders, including the inbuilt BannerBoard placeholder %name%. The key element is the name without the % character.
	 */
	public Map<String, PlaceHolder> getRegisteredPlaceHolders();

	/**
	 * Registers a new renderer for your plugin. Do not use any uppercase characters for the name.
	 * @param name the name of your renderer, do not use any uppercases here.
	 * @param customRenderer an instance of the CustomRenderer class.
	 */
	@Deprecated
	public void registerCustomRenderer(String name, CustomRenderer customRenderer);

	/**
	 * Registers a new renderer for your plugin. Do not use any uppercase characters for the name.
	 * @param name the name of your renderer, do not use any uppercases here.
	 * @param plugin
	 * @param doLoadSkinCache set to true when using the players skin, set to false otherwise
	 * @param customRenderer you renderer class which should extend BannerBoardRenderer
	 */
	public void registerCustomRenderer(String name, Plugin plugin, boolean doLoadSkinCache, Class<? extends BannerBoardRenderer<?>> customRenderer);

	/**
	 * Do not modify the returned HashMap, it's read-only, so modifying it will have no effect.
	 * @return a case insensitive map containing all registered renderers, including the inbuilt BannerBoard renderers image, skin, text and color.
	 */
	public Map<String, CustomRenderer> getRegisteredRenderers();

	/**
	 * Use this to receive a player's skin.
	 * @param name the name of the player, this is case sensitive.
	 * @param type the type of skin you like to request from the cache.
	 * @return a BufferedImage of a players skin. This might return null if the player cache was not found or the cache failed to read the data.
	 */
	public BufferedImage getCachedSkin(String name, SkinType type);

	/**
	 * Make sure to always do a null check when using getCachedSkin(), this method does not guarantee the file can actually be read.
	 * If the skin is not cached, something went wrong while the player logged in, there is nothing you can do about that at this point.
	 * @param name the name of the player, this is case sensitive.
	 * @param type the type of skin you like to request from the cache.
	 * @return true if the skin was cached. Or false if it wasn't.
	 */
	public boolean hasCachedSkin(String name, SkinType type);

	/**
	 * Use this to get a loaded image. Use hasLoadedImage() first, to make sure the image is loaded.
	 * @param fileName the name is the filename, including the extension, for example example_image.png.
	 * @return a BufferedImage which was loaded by BannerBoard (so it was located in the /plugins/BannerBoard/images/ folder on startup). Returns null if the image was not found or if the file was corrupt.
	 */
	public BufferedImage getLoadedImage(String fileName);

	/**
	 * @param fileName the name is the filename, including the extension, for example example_image.png.
	 * @return true if the image was loaded. You dont have to do a null check when using getLoadedImage(), this method does guarantee the file can actually be read.
	 */
	public boolean hasLoadedImage(String fileName);

	/**
	 * Draws a nice looking text to a BufferedImage. Draw the returned BufferedImage on your own BufferedImage at (0,0).
	 * @param width the width of the entire bannerboard
	 * @param height the height of the entire bannerboard
	 * @param text the text that will be drawn
	 * @param font the font that will be used for drawing, When creating the font, keep in mind that all fonts located in the /plugins/BannerBoard/fonts/ folder are already loaded.
	 * @param textColor the color in which the text will be drawn.
	 * @param strokeColor the color of the stroke around the text, set strokeThickness to 0 to disable.
	 * @param strokeThickness the thickness of the stroke around the text, set to 0 to disable.
	 * @param xOffset the x-offset at which the text will be drawn, set to null to center automatically.
	 * @param yOffset the y-offset at which the text will be drawn, set to null to center automatically.
	 * @return draw the returned BufferedImage on your own BufferedImage at (0,0).
	 */
	public BufferedImage drawFancyText(int width, int height, String text, Font font, Color textColor, Color strokeColor, int strokeThickness, Integer xOffset, Integer yOffset);

	/**
	 * 
	 * @param p the player for which the placeholders should be applied
	 * @param text the input text
	 * @return the output text with all placeholders applied
	 */
	public String applyPlaceholders(Player p, String text);

	/**
	 * 
	 * @param frame the itemframe
	 * @return the index of the frame, -1 if not found
	 */
	public int getFrameIndex(ItemFrame frame);

	/**
	 * 
	 * @param frame the itemframe
	 * @return the id of the bannerboard, -1 if not found
	 */
	public int getBannerBoardId(ItemFrame frame);

	public BufferedImage fetchImage(String url);
}
