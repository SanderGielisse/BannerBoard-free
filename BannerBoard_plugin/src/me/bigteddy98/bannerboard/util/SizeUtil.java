package me.bigteddy98.bannerboard.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public class SizeUtil {

	public static int getWidth(List<Location> list) {
		int smallestX = Integer.MAX_VALUE;
		int biggestX = -Integer.MAX_VALUE;

		int smallestZ = Integer.MAX_VALUE;
		int biggestZ = -Integer.MAX_VALUE;

		for (Location loc : list) {
			if (loc.getBlockX() < smallestX) {
				smallestX = loc.getBlockX();
			}
			if (loc.getBlockX() > biggestX) {
				biggestX = loc.getBlockX();
			}
			if (loc.getBlockZ() < smallestZ) {
				smallestZ = loc.getBlockZ();
			}
			if (loc.getBlockZ() > biggestZ) {
				biggestZ = loc.getBlockZ();
			}
		}

		int width = -1;

		if (smallestZ == biggestZ) {
			width = Math.abs(smallestX - biggestX) + 1;
		}
		if (smallestX == biggestX) {
			width = Math.abs(smallestZ - biggestZ) + 1;
		}
		return width;
	}

	public static int getHeight(List<Location> list) {
		int smallestY = Integer.MAX_VALUE;
		int biggestY = -Integer.MAX_VALUE;

		for (Location loc : list) {
			if (loc.getBlockY() < smallestY) {
				smallestY = loc.getBlockY();
			}
			if (loc.getBlockY() > biggestY) {
				biggestY = loc.getBlockY();
			}
		}
		return Math.abs(smallestY - biggestY) + 1;
	}

	public static int highestX(List<Location> locs) {
		int highest = -Integer.MAX_VALUE;

		for (Location loc : locs) {
			if (loc.getBlockX() > highest) {
				highest = loc.getBlockX();
			}
		}
		return highest;
	}

	public static int highestY(List<Location> locs) {
		int highest = -Integer.MAX_VALUE;

		for (Location loc : locs) {
			if (loc.getBlockY() > highest) {
				highest = loc.getBlockY();
			}
		}
		return highest;
	}

	public static int highestZ(List<Location> locs) {
		int highest = -Integer.MAX_VALUE;

		for (Location loc : locs) {
			if (loc.getBlockZ() > highest) {
				highest = loc.getBlockZ();
			}
		}
		return highest;
	}

	public static int lowestX(List<Location> locs) {
		int lowest = Integer.MAX_VALUE;

		for (Location loc : locs) {
			if (loc.getBlockX() < lowest) {
				lowest = loc.getBlockX();
			}
		}
		return lowest;
	}

	public static int lowestY(List<Location> locs) {
		int lowest = Integer.MAX_VALUE;

		for (Location loc : locs) {
			if (loc.getBlockY() < lowest) {
				lowest = loc.getBlockY();
			}
		}
		return lowest;
	}

	public static int lowestZ(List<Location> locs) {
		int lowest = Integer.MAX_VALUE;

		for (Location loc : locs) {
			if (loc.getBlockZ() < lowest) {
				lowest = loc.getBlockZ();
			}
		}
		return lowest;
	}

	public static final class SortData {

		public List<Location> locations;
		public int width;
		public int height;
		public int rotation;

		public SortData(List<Location> locations, int width, int height, int rotation) {
			this.locations = locations;
			this.width = Math.abs(width) + 1;
			this.height = Math.abs(height) + 1;
			this.rotation = rotation;
		}
	}

	public static SortData sort(List<Location> locs, BlockFace facing, BlockFace userFace) {
		Location rb = locs.get(0);
		List<Location> correctOrder = new ArrayList<>();

		Location startBlock;
		Location endBlock;

		switch (facing) {
		case SOUTH:
			// startblock has highest X and highest Y
			startBlock = new Location(rb.getWorld(), SizeUtil.highestX(locs), SizeUtil.highestY(locs), rb.getBlockZ());

			// endblock has lowest X and lowest Y
			endBlock = new Location(rb.getWorld(), SizeUtil.lowestX(locs), SizeUtil.lowestY(locs), rb.getBlockZ());

			// decreasing X, decreasing Y
			for (int x = startBlock.getBlockX(); x >= endBlock.getBlockX(); x--) {
				for (int y = startBlock.getBlockY(); y >= endBlock.getBlockY(); y--) {
					correctOrder.add(new Location(rb.getWorld(), x, y, rb.getBlockZ()));
				}
			}
			return new SortData(correctOrder, //
					startBlock.getBlockX() - endBlock.getBlockX(), //
					startBlock.getBlockY() - endBlock.getBlockY(), 0);
		case NORTH:
			startBlock = new Location(rb.getWorld(), SizeUtil.lowestX(locs), SizeUtil.highestY(locs), rb.getBlockZ());
			endBlock = new Location(rb.getWorld(), SizeUtil.highestX(locs), SizeUtil.lowestY(locs), rb.getBlockZ());

			for (int x = startBlock.getBlockX(); x <= endBlock.getBlockX(); x++) {
				for (int y = startBlock.getBlockY(); y >= endBlock.getBlockY(); y--) {
					correctOrder.add(new Location(rb.getWorld(), x, y, rb.getBlockZ()));
				}
			}
			return new SortData(correctOrder, //
					endBlock.getBlockX() - startBlock.getBlockX(), //
					endBlock.getBlockY() - startBlock.getBlockY(), 0);

		case WEST:
			startBlock = new Location(rb.getWorld(), rb.getBlockX(), SizeUtil.highestY(locs), SizeUtil.highestZ(locs));
			endBlock = new Location(rb.getWorld(), rb.getBlockX(), SizeUtil.lowestY(locs), SizeUtil.lowestZ(locs));

			for (int z = startBlock.getBlockZ(); z >= endBlock.getBlockZ(); z--) {
				for (int y = startBlock.getBlockY(); y >= endBlock.getBlockY(); y--) {
					correctOrder.add(new Location(rb.getWorld(), rb.getBlockX(), y, z));
				}
			}
			return new SortData(correctOrder, //
					endBlock.getBlockZ() - startBlock.getBlockZ(), //
					endBlock.getBlockY() - startBlock.getBlockY(), 0);

		case EAST:
			startBlock = new Location(rb.getWorld(), rb.getBlockX(), SizeUtil.highestY(locs), SizeUtil.lowestZ(locs));
			endBlock = new Location(rb.getWorld(), rb.getBlockX(), SizeUtil.lowestY(locs), SizeUtil.highestZ(locs));

			for (int z = startBlock.getBlockZ(); z <= endBlock.getBlockZ(); z++) {
				for (int y = startBlock.getBlockY(); y >= endBlock.getBlockY(); y--) {
					correctOrder.add(new Location(rb.getWorld(), rb.getBlockX(), y, z));
				}
			}
			return new SortData(correctOrder, //
					endBlock.getBlockZ() - startBlock.getBlockZ(), //
					endBlock.getBlockY() - startBlock.getBlockY(), 0);

		case UP:

			startBlock = new Location(rb.getWorld(), SizeUtil.lowestX(locs), rb.getBlockY(), SizeUtil.highestZ(locs));
			endBlock = new Location(rb.getWorld(), SizeUtil.highestX(locs), rb.getBlockY(), SizeUtil.lowestZ(locs));

			for (int x = startBlock.getBlockX(); x <= endBlock.getBlockX(); x++) {
				for (int z = startBlock.getBlockZ(); z >= endBlock.getBlockZ(); z--) {
					correctOrder.add(new Location(rb.getWorld(), x, rb.getBlockY(), z));
				}
			}

			int rotation;
			if (userFace == BlockFace.NORTH)
				rotation = 90;
			else if (userFace == BlockFace.EAST)
				rotation = 0;
			else if (userFace == BlockFace.SOUTH)
				rotation = -90;
			else if (userFace == BlockFace.WEST)
				rotation = 180;
			else
				throw new RuntimeException("Unknown user face " + userFace);

			return new SortData(correctOrder, //
					endBlock.getBlockX() - startBlock.getBlockX(), //
					endBlock.getBlockZ() - startBlock.getBlockZ(), rotation);

		case DOWN:
			startBlock = new Location(rb.getWorld(), SizeUtil.lowestX(locs), rb.getBlockY(), SizeUtil.lowestZ(locs));
			endBlock = new Location(rb.getWorld(), SizeUtil.highestX(locs), rb.getBlockY(), SizeUtil.highestZ(locs));

			for (int x = startBlock.getBlockX(); x <= endBlock.getBlockX(); x++) {
				for (int z = startBlock.getBlockZ(); z <= endBlock.getBlockZ(); z++) {
					correctOrder.add(new Location(rb.getWorld(), x, rb.getBlockY(), z));
				}
			}

			if (userFace == BlockFace.NORTH)
				rotation = -90;
			else if (userFace == BlockFace.EAST)
				rotation = 0;
			else if (userFace == BlockFace.SOUTH)
				rotation = 90;
			else if (userFace == BlockFace.WEST)
				rotation = 180;
			else
				throw new RuntimeException("Unknown user face " + userFace);

			return new SortData(correctOrder, //
					endBlock.getBlockX() - startBlock.getBlockX(), //
					endBlock.getBlockZ() - startBlock.getBlockZ(), rotation);
			
		default:
			throw new RuntimeException("Unknown block face");
		}
	}
}
