package me.bigteddy98.bannerboard;

import me.bigteddy98.bannerboard.util.FrameManager;
import me.bigteddy98.bannerboard.util.SizeUtil;
import me.bigteddy98.bannerboard.util.SizeUtil.SortData;
import me.bigteddy98.bannerboard.util.VersionUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class BoardManager implements Listener {

	private Main plugin;

	public Listener init(Main plugin) {
		this.plugin = plugin;
		return this;
	}

	private static final String BANNERBOARD_CREATOR = ChatColor.GRAY + ">> " + ChatColor.RED + "BannerBoard Creator" + ChatColor.GRAY + " <<";

	public void createBoard(Player player) {
		player.getInventory().addItem(buildItem());
		Main.msg(player, "&DThe &YBannerBoard creation toolkit &Dhas been added to your inventory.");
	}

	private ItemStack buildItem() {
		ItemStack stack = new ItemStack(Material.REDSTONE_BLOCK, 2, (short) 0);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(BANNERBOARD_CREATOR);
		stack.setItemMeta(meta);
		return stack;
	}

	private final Map<Player, Location> previousBlock = new HashMap<>();

	@EventHandler(priority = EventPriority.MONITOR)
	private void onBreak(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Iterator<Location> it = this.previousBlock.values().iterator();
		while (it.hasNext()) {
			if (it.next().equals(event.getBlock().getLocation())) {
				it.remove();
				Main.msg(event.getPlayer(), "&DFirst location removed.");
			}
		}
	}

	// north/south = Z direction
	// east/west = X direction

	@EventHandler(priority = EventPriority.MONITOR)
	private void onPlace(BlockPlaceEvent event) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (event.isCancelled()) {
			return;
		}

		if (event.getItemInHand() != null && event.getItemInHand().hasItemMeta() && event.getItemInHand().getItemMeta().hasDisplayName()) {
			String name = event.getItemInHand().getItemMeta().getDisplayName();

			if (name.equals(BANNERBOARD_CREATOR)) {
				event.setCancelled(true);

				if (!(event.getPlayer().isOp() || event.getPlayer().hasPermission("bannerboard.create"))) {
					Main.msg(event.getPlayer(), "&DYou are not allowed to do that. Please contact a server administrator if you think this might be a mistake.");
					return;
				}

				if (!this.previousBlock.containsKey(event.getPlayer())) {
					// first block placed
					final Location loc = event.getBlock().getLocation();
					this.previousBlock.put(event.getPlayer(), loc);

					Main.msg(event.getPlayer(), "&DFirst location set at &YX=" + loc.getBlockX() + " Y=" + loc.getBlockY() + " Z=" + loc.getBlockZ() + "&D.");
				} else {
					Location loc1 = this.previousBlock.remove(event.getPlayer());
					Location loc2 = event.getBlock().getLocation();

					Main.msg(event.getPlayer(), "&DSetting up a new bannerboard, please wait...");

					if (!loc1.getWorld().equals(loc2.getWorld())) {
						Main.msg(event.getPlayer(), "&DCannot generate a &YBannerBoard &Dwithin two different worlds.");
						return;
					}

					if (loc1.distanceSquared(loc2) > 16 * 16) {
						Main.msg(event.getPlayer(), ChatColor.RED + "The BannerBoard you are currently creating is very big! Your players will most likely have a dropped framerate when they are close to this banner. Make sure you know what you are doing, this might result in something bad!");
					}

					if (loc1.getBlockX() != loc2.getBlockX() && //
							loc1.getBlockZ() != loc2.getBlockZ() && //
							loc1.getBlockY() != loc2.getBlockY()) {
						Main.msg(event.getPlayer(), "&DFailed to create BannerBoard, error &Y[The thinkness of the selection has to be 1 in at least one dimension]");
						event.setCancelled(true);
						return;
					}

					// EXAMPLE BUILD FACING NORTH and is in two different X
					// columns

					boolean northSouth = loc1.getBlockX() != loc2.getBlockX();

					final List<BlockFace> faces = new ArrayList<>();
					if (loc1.getBlockX() == loc2.getBlockX() && loc1.getBlockZ() == loc2.getBlockZ()) {
						faces.addAll(Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST));
					} else {
						if (northSouth) {
							faces.addAll(Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH));
						} else { // else eastWest
							faces.addAll(Arrays.asList(BlockFace.EAST, BlockFace.WEST));
						}
					}

					// TODO as of 1.13, maps can be placed on the floor
					final boolean higherThanV1_13_R1 = VersionUtil.isHigherThan("v1_13_R1");
					if (higherThanV1_13_R1 && (loc1.getBlockY() == loc2.getBlockY())) {
						faces.add(BlockFace.UP);
						faces.add(BlockFace.DOWN);
					}

					List<Location> bannerBoard = this.getSelection(loc1, loc2);
					// check if the BannerBoard has a wall behind it in one of
					// the two directions
					BlockFace hasWall = null;
					for (BlockFace allowedOption : faces) {
						boolean goodSoFar = true;

						for (Location loc : bannerBoard) {
							if (loc.getBlock().getRelative(allowedOption).getType() == Material.AIR) {
								goodSoFar = false;
								break;
							}
						}

						if (goodSoFar) {
							hasWall = allowedOption;
						}
					}

					if (hasWall == null) {
						Main.msg(event.getPlayer(), "&DFailed to create BannerBoard, error &Y[There is no solid wall behind the BannerBoard]");
						event.setCancelled(true);
						return;
					}

					if (bannerBoard.isEmpty())
						throw new RuntimeException("BannerBoard cant be empty here");

					SortData data = SizeUtil.sort(bannerBoard, hasWall, getDirection(event.getPlayer().getLocation()));
					bannerBoard = data.locations;

					if (bannerBoard.isEmpty())
						throw new RuntimeException("Sorted BannerBoard cant be empty here");

					for (Entity e : bannerBoard.get(0).getBlock().getWorld().getEntities()) {
						if (e instanceof Hanging) {
							for (Location frame : bannerBoard) {
								if (equals(frame, e.getLocation())) {
									Main.msg(event.getPlayer(), "&DFailed to create BannerBoard, error &Y[Surface may not contain any hanging entities]");
									event.setCancelled(true);
									return;
								}
							}
						}
					}

					for (Location loc : bannerBoard) {
						if (loc.getBlock().getType() != Material.AIR && !loc.equals(loc1) && !loc.equals(loc2)) {
							Main.msg(event.getPlayer(), "&DFailed to create BannerBoard, error &Y[There were blocks in the way]");
							event.setCancelled(true);
							return;
						}
					}

					loc1.getBlock().setType(Material.AIR);
					loc2.getBlock().setType(Material.AIR);

					for (Location loc : bannerBoard) {
						// loc.setYaw(toYaw(hasWall));
						if (VersionUtil.isHigherThan("v1_13_R1")) {
							FrameManager.spawn(loc, hasWall); // .getBlock().getRelative(hasWall).getLocation()
						} else {
							loc.getWorld().spawn(loc, ItemFrame.class);
						}
					}
					int newId = plugin.configurationManager.newId();

					plugin.configurationManager.saveBannerBoard(new BannerBoard(newId, bannerBoard, hasWall, data.width, data.height, data.rotation));

					Main.msg(event.getPlayer(), "&DSuccessfully generated board &Y" + newId + " &Dbetween points &Y[" + loc1.getBlockX() + "," + loc1.getBlockY() + "," + loc1.getBlockZ() + "] and [" + loc2.getBlockX() + "," + loc2.getBlockY() + "," + loc2.getBlockZ()
							+ "] &DPlease go to your configuration file and follow the instructions on the &YBannerBoard SpigotMC &Dpage.");

					plugin.reload();
				}
			}
		}
	}

	public static BlockFace getDirection(Location loc) {
		double rotation = (loc.getYaw() - 90) % 360;
		if (rotation < 0)
			rotation += 360.0;

		if ((0 <= rotation && rotation < 45) || (315 <= rotation && rotation < 360)) {
			return BlockFace.NORTH;
		} else if (45 <= rotation && rotation < 135) {
			return BlockFace.EAST;
		} else if (135 <= rotation && rotation < 225) {
			return BlockFace.SOUTH;
		} else if (225 <= rotation && rotation < 315) {
			return BlockFace.WEST;
		}
		throw new RuntimeException("Failed to calculate direction of yaw " + loc);
	}

	private boolean equals(Location l1, Location l2) {
		return l1.getBlockX() == l2.getBlockX() && l1.getBlockY() == l2.getBlockY() && l1.getBlockZ() == l2.getBlockZ();
	}

	private List<Location> getSelection(Location loc1, Location loc2) {
		if (!loc1.getWorld().equals(loc2.getWorld())) {
			throw new UnsupportedOperationException("Cannot measure distance between " + loc1.getWorld().getName() + " and " + loc2.getWorld().getName());
		}
		World w = loc1.getWorld();

		int x1 = Math.min(loc1.getBlockX(), loc2.getBlockX());
		int y1 = Math.min(loc1.getBlockY(), loc2.getBlockY());
		int z1 = Math.min(loc1.getBlockZ(), loc2.getBlockZ());

		int x2 = Math.max(loc1.getBlockX(), loc2.getBlockX());
		int y2 = Math.max(loc1.getBlockY(), loc2.getBlockY());
		int z2 = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

		List<Location> tmp = new ArrayList<>();

		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				for (int z = z1; z <= z2; z++) {
					tmp.add(new Location(w, x, y, z));
				}
			}
		}
		return Collections.unmodifiableList(tmp);
	}
}
