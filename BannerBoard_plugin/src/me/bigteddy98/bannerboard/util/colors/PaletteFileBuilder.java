package me.bigteddy98.bannerboard.util.colors;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.bigteddy98.bannerboard.util.VersionUtil;
import org.bukkit.Bukkit;

public class PaletteFileBuilder {

	private static final String VERSION = "v1_16_R1";

	private static final List<MapColor> BASE_COLORS = new ArrayList<>();
	static {
		BASE_COLORS.add(new MapColor(1, 127, 178, 56));
		BASE_COLORS.add(new MapColor(2, 247, 233, 163));
		BASE_COLORS.add(new MapColor(3, 199, 199, 199));
		BASE_COLORS.add(new MapColor(4, 255, 0, 0));
		BASE_COLORS.add(new MapColor(5, 160, 160, 255));
		BASE_COLORS.add(new MapColor(6, 167, 167, 167));
		BASE_COLORS.add(new MapColor(7, 0, 124, 0));
		BASE_COLORS.add(new MapColor(8, 255, 255, 255));
		BASE_COLORS.add(new MapColor(9, 164, 168, 184));
		BASE_COLORS.add(new MapColor(10, 151, 109, 77));
		BASE_COLORS.add(new MapColor(11, 112, 112, 112));
		BASE_COLORS.add(new MapColor(12, 64, 64, 255));
		BASE_COLORS.add(new MapColor(13, 143, 119, 72));
		BASE_COLORS.add(new MapColor(14, 255, 252, 245));
		BASE_COLORS.add(new MapColor(15, 216, 127, 51));
		BASE_COLORS.add(new MapColor(16, 178, 76, 216));
		BASE_COLORS.add(new MapColor(17, 102, 153, 216));
		BASE_COLORS.add(new MapColor(18, 229, 229, 51));
		BASE_COLORS.add(new MapColor(19, 127, 204, 25));
		BASE_COLORS.add(new MapColor(20, 242, 127, 165));
		BASE_COLORS.add(new MapColor(21, 76, 76, 76));
		BASE_COLORS.add(new MapColor(22, 153, 153, 153));
		BASE_COLORS.add(new MapColor(23, 76, 127, 153));
		BASE_COLORS.add(new MapColor(24, 127, 63, 178));
		BASE_COLORS.add(new MapColor(25, 51, 76, 178));
		BASE_COLORS.add(new MapColor(26, 102, 76, 51));
		BASE_COLORS.add(new MapColor(27, 102, 127, 51));
		BASE_COLORS.add(new MapColor(28, 153, 51, 51));
		BASE_COLORS.add(new MapColor(29, 25, 25, 25));
		BASE_COLORS.add(new MapColor(30, 250, 238, 77));
		BASE_COLORS.add(new MapColor(31, 92, 219, 213));
		BASE_COLORS.add(new MapColor(32, 74, 128, 255));
		BASE_COLORS.add(new MapColor(33, 0, 217, 58));
		BASE_COLORS.add(new MapColor(34, 129, 86, 49));
		BASE_COLORS.add(new MapColor(35, 112, 2, 0));

		final int thisVersion = VersionUtil.SUPPORTED_VERSIONS.indexOf(VERSION);
		if (thisVersion >= VersionUtil.SUPPORTED_VERSIONS.indexOf("v1_12_R1")) {
			BASE_COLORS.add(new MapColor(36, 209, 177, 161));
			BASE_COLORS.add(new MapColor(37, 159, 82, 36));
			BASE_COLORS.add(new MapColor(38, 149, 87, 108));
			BASE_COLORS.add(new MapColor(39, 112, 108, 138));
			BASE_COLORS.add(new MapColor(40, 186, 133, 36));
			BASE_COLORS.add(new MapColor(41, 103, 117, 53));
			BASE_COLORS.add(new MapColor(42, 160, 77, 78));
			BASE_COLORS.add(new MapColor(43, 57, 41, 35));
			BASE_COLORS.add(new MapColor(44, 135, 107, 98));
			BASE_COLORS.add(new MapColor(45, 87, 92, 92));
			BASE_COLORS.add(new MapColor(46, 122, 73, 88));
			BASE_COLORS.add(new MapColor(47, 76, 62, 92));
			BASE_COLORS.add(new MapColor(48, 76, 50, 35));
			BASE_COLORS.add(new MapColor(49, 76, 82, 42));
			BASE_COLORS.add(new MapColor(50, 142, 60, 46));
			BASE_COLORS.add(new MapColor(51, 37, 22, 16));
		}

		if (thisVersion >= VersionUtil.SUPPORTED_VERSIONS.indexOf("v1_16_R1")) {
			BASE_COLORS.add(new MapColor(52, 189, 48, 49));
			BASE_COLORS.add(new MapColor(53, 148, 63, 97));
			BASE_COLORS.add(new MapColor(54, 92, 25, 29));
			BASE_COLORS.add(new MapColor(55, 22, 126, 134));
			BASE_COLORS.add(new MapColor(56, 58, 142, 140));
			BASE_COLORS.add(new MapColor(57, 86, 44, 62));
			BASE_COLORS.add(new MapColor(58, 20, 180, 133));
		}
	}

	private static final List<MapColor> ALL_COLORS = new ArrayList<>();
	static {
		for (MapColor base : BASE_COLORS) {
			ALL_COLORS.add(new MapColor(base.getId() * 4, base.getRed() * 180D / 255, base.getGreen() * 180D / 255,
					base.getBlue() * 180D / 255));
			ALL_COLORS.add(new MapColor(base.getId() * 4 + 1, base.getRed() * 220D / 255, base.getGreen() * 220D / 255,
					base.getBlue() * 220D / 255));
			ALL_COLORS.add(new MapColor(base.getId() * 4 + 2, base.getRed() * 255D / 255, base.getGreen() * 255D / 255,
					base.getBlue() * 255D / 255));
			ALL_COLORS.add(new MapColor(base.getId() * 4 + 3, base.getRed() * 135D / 255, base.getGreen() * 135D / 255,
					base.getBlue() * 135D / 255));
		}
	}

	/*
	private static final String VERSION = "v1_8_R1";
	private static final List<MapColor> ALL_COLORS = Arrays.asList(new MapColor[] { c(89, 125, 39), c(109, 153, 48),
			c(127, 178, 56), c(67, 94, 29), c(174, 164, 115), c(213, 201, 140), c(247, 233, 163), c(130, 123, 86),
			c(140, 140, 140), c(171, 171, 171), c(199, 199, 199), c(105, 105, 105), c(180, 0, 0), c(220, 0, 0),
			c(255, 0, 0), c(135, 0, 0), c(112, 112, 180), c(138, 138, 220), c(160, 160, 255), c(84, 84, 135),
			c(117, 117, 117), c(144, 144, 144), c(167, 167, 167), c(88, 88, 88), c(0, 87, 0), c(0, 106, 0),
			c(0, 124, 0), c(0, 65, 0), c(180, 180, 180), c(220, 220, 220), c(255, 255, 255), c(135, 135, 135),
			c(115, 118, 129), c(141, 144, 158), c(164, 168, 184), c(86, 88, 97), c(106, 76, 54), c(130, 94, 66),
			c(151, 109, 77), c(79, 57, 40), c(79, 79, 79), c(96, 96, 96), c(112, 112, 112), c(59, 59, 59),
			c(45, 45, 180), c(55, 55, 220), c(64, 64, 255), c(33, 33, 135), c(100, 84, 50), c(123, 102, 62),
			c(143, 119, 72), c(75, 63, 38), c(180, 177, 172), c(220, 217, 211), c(255, 252, 245), c(135, 133, 129),
			c(152, 89, 36), c(186, 109, 44), c(216, 127, 51), c(114, 67, 27), c(125, 53, 152), c(153, 65, 186),
			c(178, 76, 216), c(94, 40, 114), c(72, 108, 152), c(88, 132, 186), c(102, 153, 216), c(54, 81, 114),
			c(161, 161, 36), c(197, 197, 44), c(229, 229, 51), c(121, 121, 27), c(89, 144, 17), c(109, 176, 21),
			c(127, 204, 25), c(67, 108, 13), c(170, 89, 116), c(208, 109, 142), c(242, 127, 165), c(128, 67, 87),
			c(53, 53, 53), c(65, 65, 65), c(76, 76, 76), c(40, 40, 40), c(108, 108, 108), c(132, 132, 132),
			c(153, 153, 153), c(81, 81, 81), c(53, 89, 108), c(65, 109, 132), c(76, 127, 153), c(40, 67, 81),
			c(89, 44, 125), c(109, 54, 153), c(127, 63, 178), c(67, 33, 94), c(36, 53, 125), c(44, 65, 153),
			c(51, 76, 178), c(27, 40, 94), c(72, 53, 36), c(88, 65, 44), c(102, 76, 51), c(54, 40, 27), c(72, 89, 36),
			c(88, 109, 44), c(102, 127, 51), c(54, 67, 27), c(108, 36, 36), c(132, 44, 44), c(153, 51, 51),
			c(81, 27, 27), c(17, 17, 17), c(21, 21, 21), c(25, 25, 25), c(13, 13, 13), c(176, 168, 54), c(215, 205, 66),
			c(250, 238, 77), c(132, 126, 40), c(64, 154, 150), c(79, 188, 183), c(92, 219, 213), c(48, 115, 112),
			c(52, 90, 180), c(63, 110, 220), c(74, 128, 255), c(39, 67, 135), c(0, 153, 40), c(0, 187, 50),
			c(0, 217, 58), c(0, 114, 30), c(91, 60, 34), c(111, 74, 42), c(129, 86, 49), c(68, 45, 25), c(79, 1, 0),
			c(96, 1, 0), c(112, 2, 0), c(59, 1, 0), });
	*/
	//private static final String VERSION = "v1_8_R1";
	//private static final List<MapColor> ALL_COLORS = new ArrayList<>();

	public static void main(String[] args) throws IOException {
		
		for (MapColor s : ALL_COLORS) {
			System.out.println("first id " + s.getId() + " rgb " + s.getRed() + " " + s.getGreen() + " " + s.getBlue());
		}
		
		Bukkit.getLogger().info("Building color hashing map...");
		long startTime = System.currentTimeMillis();

		int todo = 256 * 256 * 256;
		int counter = 0;

		System.out.println("nearest " + naiveNearest(0, 0, 255));

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(baos))) {
			for (int red = 0; red < 256; red++) {
				for (int green = 0; green < 256; green++) {
					for (int blue = 0; blue < 256; blue++) {
						final byte naiveNearest = naiveNearest(red, green, blue).getId();
						out.writeByte(naiveNearest);

						if (counter++ % 10000 == 0) {
							System.out.println("Done percentage " + (counter * 100D / todo));
						}
					}
				}
			}
		}

		final byte[] compressed = Compressor.compress(baos.toByteArray());
		try (DataOutputStream out = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream("color_palette_" + VERSION + ".bc")))) {
			out.writeInt(compressed.length);
			out.write(compressed);
		}

		long endTime = System.currentTimeMillis();
		System.out.println("Millisecond(s) " + (endTime - startTime));
	}

	private static MapColor c(int r, int g, int b) {
		return new MapColor(r, g, b);
	}

	private static MapColor naiveNearest(int red, int green, int blue) {
		MapColor nearest = null;
		double ndist = -1;

		for (MapColor c : ALL_COLORS) {
			final double dist = distanceSquared(red, green, blue, c.getRed(), c.getGreen(), c.getBlue());
			if (nearest == null || dist < ndist) {
				nearest = c;
				ndist = dist;
			}
		}

		return nearest;
	}

	private static double distanceSquared(int c1red, int c1green, int c1blue, int c2red, int c2green, int c2blue) {
		final double r = (double) (c1red + c2red) / 2;
		final int dred = c1red - c2red;
		final int dgreen = c1green - c2green;
		final int dblue = c1blue - c2blue;

		return //
		(((2 + (r / 256)) * dred * dred) + //
				(4 * dgreen * dgreen) + //
				((2 + ((255 - r) / 256)) * dblue * dblue));
	}
}
