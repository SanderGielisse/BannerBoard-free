package me.bigteddy98.bannerboard;

import me.bigteddy98.bannerboard.api.BannerBoardRenderer;
import me.bigteddy98.bannerboard.draw.BannerCanvas;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorManager {

	// this thread will be the only thread transforming images to a byte array
	private final ExecutorService rendererExecutor = Executors.newSingleThreadExecutor();

	// these threads will handle any preparation needed, such as a skin which has to be retrieved from a URL
	public final ExecutorService preparationExecutor = Executors.newFixedThreadPool(2); // just to be sure we can instantly process

	public void render(final int slide, final Player pl, final Main plugin, final BannerBoard board, final RenderCallback rb) {
		final String name = pl.getName();
		// for loop every slide
		this.submit(this.preparationExecutor, () -> {
			final Map<Integer, Object> preps = new HashMap<>();
			// do preparation async
			for (BannerBoardRenderer<?> s : board.getReadOnlyRenderers(slide)) {
				Object prep = null;
				try {
					prep = s.asyncRenderPrepare(pl);
				} catch (Throwable e) {
					Bukkit.getLogger().warning("Failed to do preperations for user " + name + ". " + e.getClass().getSimpleName() + " " + cap(e.getMessage()) + ".");
				}
				preps.put(s.getId(), prep);
			}
			// switch to the renderer executor
			ExecutorManager.this.submit(rendererExecutor, () -> {
				BufferedImage image = board.getImage(pl, preps, slide);
				if (image.getWidth() != board.getPixelWidth() || image.getHeight() != board.getPixelHeight()) {
					throw new RuntimeException("BufferedImage does not have correct size: should be " + board.getPixelWidth() + "x" + board.getPixelHeight() + " but is " + image.getWidth() + "x" + image.getHeight() + " for rotation" + board.getRotation());
				}
				final byte[][] data = new byte[board.getWidth() * board.getHeight()][];
				int i = 0;
				for (int x = 0; x < board.getWidth(); x++) {
					for (int y = 0; y < board.getHeight(); y++) {
						int startX = x * 128;
						int startY = y * 128;
						BufferedImage sub = image.getSubimage(startX, startY, 128, 128);
						// render it
						BannerCanvas canvas = new BannerCanvas();
						canvas.drawImage(0, 0, sub);
						data[i++] = canvas.getBuffer();
					}
				}
				// send back to bukkit thread
				Bukkit.getScheduler().runTask(plugin, () -> rb.finished(data));
			});
		});
	}

	private String cap(String message) {
		if (message.length() > 100)
			message = message.substring(0, 100);
		return message;
	}

	public void submit(ExecutorService service, final Runnable runnable) {
		service.submit(() -> {
			try {
				runnable.run();
			} catch (Throwable e) {
				e.printStackTrace(); 
			}
		});
	}

	public void shutdown() {
		this.rendererExecutor.shutdown();
		this.preparationExecutor.shutdown();
	}
}
