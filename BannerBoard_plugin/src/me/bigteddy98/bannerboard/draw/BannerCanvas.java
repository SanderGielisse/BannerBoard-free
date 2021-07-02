package me.bigteddy98.bannerboard.draw;

import me.bigteddy98.bannerboard.Main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class BannerCanvas {

	private final byte[] buffer = new byte[128 * 128];

	{
		// fill it
		Arrays.fill(buffer, (byte) 0);
	}

	public void setRawPixel(int x, int y, byte color) {
		if (x < 0 || y < 0 || x > 127 || y > 127) {
			throw new IllegalArgumentException("Can not set coordinates bigger than 127 or smaller than 0 [X=" + x + " Y=" + y + "]");
		}

		if (buffer[y * 128 + x] != color) {
			buffer[y * 128 + x] = color;
		}
	}

	public void drawImage(int x, int y, Image image) {
		byte[] decodedImage = this.imageToBytes(image);
		for (int rawX = 0; rawX < image.getWidth(null); rawX++) {
			for (int rawY = 0; rawY < image.getHeight(null); rawY++) {
				byte color = decodedImage[rawY * image.getWidth(null) + rawX];
				this.setRawPixel(x + rawX, y + rawY, color);
			}
		}
	}

	private byte[] imageToBytes(Image image) {
		BufferedImage tmp = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = tmp.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();

		int[] pixels = new int[tmp.getWidth() * tmp.getHeight()];
		tmp.getRGB(0, 0, tmp.getWidth(), tmp.getHeight(), pixels, 0, tmp.getWidth());

		byte[] result = new byte[tmp.getWidth() * tmp.getHeight()];

		for (int i = 0; i < pixels.length; i++) {
			result[i] = Main.getInstance().colorManager.findNearestColor(pixels[i]);
		}
		return result;
	}

	public byte[] getBuffer() {
		return this.buffer;
	}
}
