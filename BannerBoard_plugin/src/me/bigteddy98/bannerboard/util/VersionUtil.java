package me.bigteddy98.bannerboard.util;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class VersionUtil {

	public static final List<String> SUPPORTED_VERSIONS = new ArrayList<String>() {

		private static final long serialVersionUID = -3393009554143811830L;

		{
			this.add("v1_8_R1");
			this.add("v1_8_R2");
			this.add("v1_8_R3");
			this.add("v1_9_R1");
			this.add("v1_9_R2");
			this.add("v1_10_R1");
			this.add("v1_11_R1");
			this.add("v1_12_R1");
			this.add("v1_13_R1");
			this.add("v1_13_R2");
			this.add("v1_14_R1");
			this.add("v1_15_R1");
			this.add("v1_16_R1");
			this.add("v1_16_R2");
			this.add("v1_16_R3");
			
			this.add("v1_17_R1");
		}
	};

	public static String getSpigotVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
	}

	public static boolean isHigherThan(String string) {
		if (!SUPPORTED_VERSIONS.contains(string))
			throw new RuntimeException("Unknown version " + string);

		final int currentIndex = SUPPORTED_VERSIONS.indexOf(getSpigotVersion());
		final int theIndex = SUPPORTED_VERSIONS.indexOf(string);
		return currentIndex >= theIndex;
	}
}
