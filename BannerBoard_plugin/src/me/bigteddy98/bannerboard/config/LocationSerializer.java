package me.bigteddy98.bannerboard.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class LocationSerializer {

	public static Object serializeList(List<Location> locs) {
		List<String> tmp = new ArrayList<>();
		for (Location loc : locs) {
			tmp.add(serialize(loc));
		}
		return tmp;
	}

	public static String serialize(Location location) {
		return location.getWorld().getName() + "_" + location.getX() + "_" + location.getY() + "_" + location.getZ() + "_" + location.getYaw() + "_" + location.getPitch();
	}

	public static List<Location> deserializeList(List<String> strings) {
		List<Location> tmp = new ArrayList<>();
		for (String s : strings) {
			tmp.add(deserialize(s));
		}
		return tmp;
	}

	public static Location deserialize(String s) {
		String[] split = s.split("_");

		// my_world_name_X_Y_Z_Y_P

		// in total 6 sections
		int worldSize = split.length - 6;

		StringBuilder worldName = new StringBuilder();
		for (int i = 0; i <= worldSize; i++) {
			worldName.append(split[i]);
			if (i != worldSize) { // do not end with a _
				worldName.append('_');
			}
		}

		return new Location(Bukkit.getWorld(worldName.toString()), Double.parseDouble(split[1 + worldSize]), Double.parseDouble(split[2 + worldSize]), Double.parseDouble(split[3 + worldSize]), Float.parseFloat(split[4 + worldSize]), Float.parseFloat(split[5 + worldSize]));
	}
}
