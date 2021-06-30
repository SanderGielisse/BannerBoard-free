package me.bigteddy98.bannerboard.util.colors;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class ColorManager {

	private final byte[] CACHE = new byte[256 * 256 * 256];

	public ColorManager(String useFile) {
		System.out.println("[INFO] [BannerBoard] Loading color cache table " + useFile + "...");
		byte[] decompressed;
		try (DataInputStream in = new DataInputStream(new BufferedInputStream(ColorManager.class.getResourceAsStream(useFile)))) {
			final int size = in.readInt();
			final byte[] compressed = new byte[size];
			in.read(compressed);

			decompressed = Compressor.decompress(compressed);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

		try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(decompressed))) {
			for (int i = 0; i < CACHE.length; i++) {
				CACHE[i] = in.readByte();
			}
			if (in.available() != 0)
				throw new RuntimeException("Bytes left while loading cache table file");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte findNearestColor(int rgb) {
		return CACHE[hashKey(rgb)];
	}

	private int hashKey(int rgb) {
		/*
		 * for (int red = 0; red < 256; red++) {
				for (int green = 0; green < 256; green++) {
					for (int blue = 0; blue < 256; blue++) {
		 */

		int red = (rgb >> 16) & 0x0ff;
		int green = (rgb >> 8) & 0x0ff;
		int blue = (rgb) & 0x0ff;
		return blue + //
				green * 256 + //
				red * (256 * 256);
	}
}
