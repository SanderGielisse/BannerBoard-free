package me.bigteddy98.bannerboard.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class DrawUtil {

	public static BufferedImage drawFancyText(int width, int height, String text, Font font, Color textColor, Color strokeColor, int strokeThickness, Integer xOffset, Integer yOffset) {
		BufferedImage textLayer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D textGraphics = textLayer.createGraphics();

		try {
			textGraphics.setColor(textColor);
			textGraphics.setFont(font);

			// draw the string
			FontMetrics fm = textGraphics.getFontMetrics();
			if (xOffset == null) {
				xOffset = (width - fm.stringWidth(text)) / 2;
			}
			if (yOffset == null) {
				yOffset = (fm.getAscent() + (height - (fm.getAscent() + fm.getDescent())) / 2);
			}
			textGraphics.drawString(text, xOffset, yOffset);

			if (strokeThickness > 0) {
				TextLayout layout = new TextLayout(text, font, new FontRenderContext(null, false, false));
				AffineTransform textAt = new AffineTransform();
				textAt.translate(xOffset, yOffset);
				Shape outline = layout.getOutline(textAt);
				textGraphics.setColor(strokeColor);
				textGraphics.setStroke(new BasicStroke(strokeThickness));
				textGraphics.draw(outline);
			}

			return textLayer;
		} finally {
			textGraphics.dispose();
		}
	}
}
