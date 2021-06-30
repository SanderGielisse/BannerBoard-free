package me.bigteddy98.bannerboard;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.bigteddy98.bannerboard.api.BannerBoardRenderer;
import me.bigteddy98.bannerboard.util.VersionUtil;

public class BannerBoard {

	private final int id;
	private final List<Location> locationList;
	private final BlockFace face;
	private final List<List<Short>> frameIds = new ArrayList<>();
	private final int width;
	private final int height;
	private final int rotation;

	private int currentSlide = 0;

	private final List<List<BannerBoardRenderer<?>>> internalRenderers = new ArrayList<>();

	private boolean equals(Location l1, Location l2) {
		return l1.getBlockX() == l2.getBlockX() && l1.getBlockY() == l2.getBlockY() && l1.getBlockZ() == l2.getBlockZ();
	}

	public BannerBoard(int id, List<Location> locationList, BlockFace face, int width, int height, int rotation) {
		this.id = id;
		this.locationList = locationList;
		this.face = face;
		this.width = width;
		this.height = height;
		this.rotation = rotation;
	}

	public void setCurrentSlide(int currentSlide) {
		this.currentSlide = currentSlide;
	}

	public int getCurrentSlide() {
		return currentSlide;
	}

	public List<Location> getLocationList() {
		return locationList;
	}

	public List<ItemFrame> buildItemFrameList() {
		List<ItemFrame> frames = new ArrayList<>();
		locationLoop: for (Location loc : this.locationList) {
			for (Entity e : loc.getChunk().getEntities()) {
				if (e instanceof ItemFrame && equals(e.getLocation(), loc)) {
					frames.add((ItemFrame) e);
					continue locationLoop;
				}
			}
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[WARNING] [BannerBoard] Itemframe missing at " + loc.toString() + ". Please restore the missing itemframe and restart your server to prevent errors.");
		}
		return frames;
	}

	public List<Short> getMapIds(int slide) {
		if (this.frameIds.isEmpty()) {
			throw new RuntimeException("getMapIds() called before setSlides(), not allowed.");
		}
		return this.frameIds.get(slide);
	}

	public int getId() {
		return id;
	}

	public BlockFace getFace() {
		return face;
	}

	public Collection<? extends BannerBoardRenderer<?>> getReadOnlyRenderers(int slide) {
		synchronized (this.internalRenderers) {
			return new ArrayList<>(this.internalRenderers.get(slide));
		}
	}

	public Collection<? extends BannerBoardRenderer<?>> getReadOnlyAllRenderers() {
		List<BannerBoardRenderer<?>> tmp = new ArrayList<>();
		synchronized (this.internalRenderers) {
			for (List<BannerBoardRenderer<?>> list : this.internalRenderers) {
				for (BannerBoardRenderer<?> s : list) {
					tmp.add(s);
				}
			}
		}
		return tmp;
	}

	public void addTopRenderer(int slide, BannerBoardRenderer<?> renderer) {
		synchronized (this.internalRenderers) {
			if (this.internalRenderers.size() <= slide) {
				this.internalRenderers.add(new ArrayList<BannerBoardRenderer<?>>());
			}
			this.internalRenderers.get(slide).add(renderer);
		}
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public int getPixelWidth() {
		return this.getWidth() * 128;
	}

	public int getPixelHeight() {
		return this.getHeight() * 128;
	}

	// called from async thread

	@SuppressWarnings("unchecked") // we are sure the object is correct
	public BufferedImage getImage(Player p, Map<Integer, Object> prep, int slide) {

		final int pixelWidth = this.getPixelWidthWithRotation();
		final int pixelHeight = this.getPixelHeightWithRotation();

		BufferedImage tmp = new BufferedImage(pixelWidth, pixelHeight, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = tmp.createGraphics();

		try {
			// let every renderer do it's work
			for (BannerBoardRenderer<?> renderer : this.getReadOnlyRenderers(slide)) {
				if (renderer.hasSetting("permission")) {
					String permission = renderer.getSetting("permission").getValue();
					if (!p.hasPermission(permission)) {
						continue;
					}
				}

				// apply extra rotation
				// angle 180,200,10

				long start = System.currentTimeMillis();
				renderer.render(p, tmp, g);

				Object renderPrep = null;
				if (prep.containsKey(renderer.getId())) {
					renderPrep = prep.get(renderer.getId());
				}
				((BannerBoardRenderer<Object>) renderer).render(p, tmp, g, renderPrep);

				long took = System.currentTimeMillis() - start;

				if (took >= 3000) {
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[WARNING] [BannerBoard] Took " + (took / 1000D) + " second(s) to render " + renderer.getClass().getSimpleName() + ", the server might be under heavy load, or did the system time change?");
				}
			}

			int rotateTimes = 0;
			if (this.rotation == -90)
				rotateTimes = 3;
			if (this.rotation == 90)
				rotateTimes = 1;
			if (this.rotation == 180)
				rotateTimes = 2;

			for (int i = 0; i < rotateTimes; i++)
				tmp = rotate(tmp);

			return tmp;
		} finally {
			g.dispose();
		}
	}

	private static BufferedImage rotate(BufferedImage in) {

		// same dims but swapped
		BufferedImage res = new BufferedImage(in.getHeight(), in.getWidth(), BufferedImage.TYPE_4BYTE_ABGR);
		final byte[] inPix = ((DataBufferByte) in.getRaster().getDataBuffer()).getData();
		final byte[] resPix = ((DataBufferByte) res.getRaster().getDataBuffer()).getData();

		int pointer = 0;
		for (int y = 0; y < in.getHeight(); y++) {
			for (int x = 0; x < in.getWidth(); x++) {
				int destPos = (((x * res.getWidth()) + ((in.getHeight() - 1) - y)) * 4);
				resPix[destPos++] = inPix[pointer++];
				resPix[destPos++] = inPix[pointer++];
				resPix[destPos++] = inPix[pointer++];
				resPix[destPos++] = inPix[pointer++];
			}
		}

		return res;
	}

	public void startRunnable(int delay) {
		// find one of our free ids
		long delayTicks = delay * 20L;
		new BukkitRunnable() {

			@Override
			public void run() {
				if (!locationList.get(0).getChunk().isLoaded()) {
					return;
				}
				int nextSlide = getCurrentSlide() + 1;
				if (nextSlide >= getSlides()) {
					nextSlide = 0;
				}
				setSlide(nextSlide);
			}
		}.runTaskTimer(Main.getInstance(), delayTicks, delayTicks);
	}

	private int slides;

	public void setSlides(int slides) {
		this.slides = slides;
		// make sure we have enough IDs
		for (int slide = 0; slide < slides; slide++) {
			if (slide >= this.frameIds.size()) {
				this.frameIds.add(new ArrayList<Short>());
			}
			List<ItemFrame> frameList = this.buildItemFrameList();
			for (int frame = 0; frame < frameList.size(); frame++) {
				this.frameIds.get(slide).add(Main.getInstance().idManager.getId());
			}
		}
		this.setSlide(0);
	}

	public int getSlides() {
		if (this.slides == 0) {
			throw new RuntimeException("getSlides() called before setSlides(), not allowed.");
		}
		return this.slides;
	}

	public void setSlide(final int slide) {
		this.currentSlide = slide;
		final List<ItemFrame> frameList = this.buildItemFrameList();
		for (int i = 0; i < frameList.size(); i++) {
			final short id = this.getMapIds(slide).get(i);
			ItemStack map = new ItemStack(Material.MAP, 1, id);

			// need to call NMS itemstack.getOrCreateTag().setInt("map", id);
			if (VersionUtil.isHigherThan("v1_13_R1"))
				try {
					map = Tag_1_13_R1.buildMap(id);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException | ClassNotFoundException | NoSuchFieldException | SecurityException e) {
					throw new RuntimeException(e);
				}

			frameList.get(i).setItem(map);
		}
	}

	public int getRotation() {
		return this.rotation;
	}

	public int getPixelWidthWithRotation() {
		if (this.rotation == -90 || this.rotation == 90)
			return this.getPixelHeight();
		return this.getPixelWidth();
	}

	public int getPixelHeightWithRotation() {
		if (this.rotation == -90 || this.rotation == 90)
			return this.getPixelWidth();
		return this.getPixelHeight();
	}
}
