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

import java.awt.*;

public enum FontStyle {

	PLAIN(Font.PLAIN),
	BOLD(Font.BOLD),
	ITALIC(Font.ITALIC),
	BOLDITALIC(Font.BOLD + Font.ITALIC),
	;

	private final int id;

	FontStyle(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
