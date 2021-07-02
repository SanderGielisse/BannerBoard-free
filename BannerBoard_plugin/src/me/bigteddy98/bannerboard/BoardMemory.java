package me.bigteddy98.bannerboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;

import io.netty.channel.Channel;
import me.bigteddy98.bannerboard.api.Action;
import me.bigteddy98.bannerboard.api.BannerBoardRenderer;
import me.bigteddy98.bannerboard.api.InteractHandler;

public class BoardMemory implements Listener {

	// ONLY access from Bukkit mainthread
	private final List<BannerBoard> loadedBannerBoards = new ArrayList<>();
	private Main plugin;

	public Collection<? extends BannerBoard> getLoadedBannerBoards() {
		return this.loadedBannerBoards;
	}

	public void load(BannerBoard board) {
		if (!Bukkit.isPrimaryThread()) {
			throw new UnsupportedOperationException("Can only register new BannerBoard from main Bukkit thread");
		}
		this.loadedBannerBoards.add(board);
		Bukkit.getLogger().info("Successfully loaded BannerBoard [" + board.getId() + "]");
	}

	public Listener init(Main plugin) {
		this.plugin = plugin;
		return this;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(final PlayerJoinEvent event) {
		join(event);
	}

	public void join(final PlayerJoinEvent event) {
		try {
			// hook Netty
			Channel playerChannel = PacketManager.getChannel(event.getPlayer());
			boolean exists = playerChannel.pipeline().get("BannerBoard_hook") != null;

			if (playerChannel.pipeline().get("packet_handler") == null) {
				// some weird bug?
				return;
			}

			if (!exists) {
				playerChannel.pipeline().addBefore("packet_handler", "BannerBoard_hook", new BannerBoardInjector());
			}

			final BannerBoardInjector inject = (BannerBoardInjector) playerChannel.pipeline().get("BannerBoard_hook");
			inject.clearBoards();
			if (this.loadedBannerBoards.size() <= 0) {
				return;
			}

			for (final BannerBoard board : loadedBannerBoards) {
				for (int i = 0; i < board.getSlides(); i++) {
					final int slide = i;
					for (Short bl : board.getMapIds(slide)) {
						inject.addMapId(bl);
					}
					plugin.executorManager.render(slide, event.getPlayer(), plugin, board, data -> {
						if (!event.getPlayer().isOnline()) {
							return;
						}
						if (data.length != board.buildItemFrameList().size()) {
							throw new IndexOutOfBoundsException("Itemframe missing for banner with ID " + board.getId() + ", remove the banner from your config or place the itemframe back.");
						}
						for (int i1 = 0; i1 < data.length; i1++) {
							short id = board.getMapIds(slide).get(i1);
							inject.addFrame(id, data[i1]);
						}
					});
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	// make sure it won't be broken
	@EventHandler
	private void onFrame(PlayerInteractEntityEvent event) {
		if (isBannerFrame(event.getRightClicked())) {
			event.setCancelled(true);
		}
	}

	// make sure it won't be broken
	@EventHandler
	private void onFrame(PlayerInteractAtEntityEvent event) {
		if (isBannerFrame(event.getRightClicked())) {
			event.setCancelled(true);

			this.click(Action.RIGHT_CLICK, (ItemFrame) event.getRightClicked(), event.getPlayer());
		}
	}

	// make sure it won't be broken
	@EventHandler
	private void onFrame(EntityDamageByEntityEvent event) {
		if (isBannerFrame(event.getEntity())) {
			event.setCancelled(true);

			if (event.getDamager() instanceof Player) {
				this.click(Action.LEFT_CLICK, (ItemFrame) event.getEntity(), (Player) event.getDamager());
			}
		}
	}

	// make sure it won't be broken
	@EventHandler
	private void onFrame(EntityDamageEvent event) {
		if (isBannerFrame(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	// make sure it won't be broken
	@EventHandler
	private void onFrame(HangingBreakByEntityEvent event) {
		if (isBannerFrame(event.getEntity())) {
			event.setCancelled(true);

			if (event.getRemover() instanceof Player) {
				this.click(Action.LEFT_CLICK, (ItemFrame) event.getEntity(), (Player) event.getRemover());
			}
		}
	}

	// make sure it won't be broken
	@EventHandler
	private void onFrame(HangingBreakEvent event) {
		if (isBannerFrame(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (event.getAction() != org.bukkit.event.block.Action.PHYSICAL) {
			// check if version is 1.10
			String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

			// has two fields
			if (version.startsWith("v1_10_")) {
				// player interacted
				if (event.getHand() == EquipmentSlot.HAND && event.getClickedBlock() != null) {
					Action action = null;

					if (event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_AIR || event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) {
						action = Action.LEFT_CLICK;
					} else if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR || event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
						action = Action.RIGHT_CLICK;
					}

					if (action != null) {
						// find the itemframe belonging to it
						Location clicked = event.getClickedBlock().getLocation();

						double nearest = Double.MAX_VALUE;
						ItemFrame frame = null;
						BannerBoard b = null;

						for (BannerBoard board : this.loadedBannerBoards) {
							for (ItemFrame itemFrame : board.buildItemFrameList()) {
								if (this.equals(itemFrame.getLocation(), clicked)) {

									double dist = itemFrame.getLocation().distanceSquared(event.getPlayer().getLocation());
									if (frame == null || dist < nearest) {
										frame = itemFrame;
										b = board;
										nearest = dist;
									}
								}
							}
						}

						if (b != null) {
							// bannerboard found
							for (BannerBoardRenderer<?> renderer : b.getReadOnlyRenderers(b.getCurrentSlide())) {
								if (renderer instanceof InteractHandler) {
									int loc = indexOf(b.buildItemFrameList(), frame); // find out where we are
									((InteractHandler<?>) renderer).handle(action, event.getPlayer(), b.getWidth(), b.getHeight(), loc, b.getId());
								}
							}
						}
					}
				}
			}
		}
	}
	
	private boolean isBannerFrame(Entity entity){
		return (entity instanceof ItemFrame) && this.isMapFrame((ItemFrame) entity);
	}

	private boolean equals(Location l1, Location l2) {
		return l1.getWorld().equals(l2.getWorld()) && l1.getBlock().getLocation().distanceSquared(l2.getBlock().getLocation()) <= 1.1;
	}

	private void click(Action action, ItemFrame rightClicked, Player player) {
		for (BannerBoard board : this.loadedBannerBoards) {
			List<ItemFrame> frame = board.buildItemFrameList();
			for (ItemFrame f : frame) {
				if (f.getUniqueId().equals(rightClicked.getUniqueId())) {
					// check if we have an interact handler
					for (BannerBoardRenderer<?> renderer : board.getReadOnlyRenderers(board.getCurrentSlide())) {
						if (renderer instanceof InteractHandler) {
							int loc = indexOf(frame, rightClicked); // find out where we are
							((InteractHandler<?>) renderer).handle(action, player, board.getWidth(), board.getHeight(), loc, board.getId());
						}
					}
					return;
				}
			}
		}
	}

	private int indexOf(List<ItemFrame> itemFrameList, ItemFrame rightClicked) {
		for (int i = 0; i < itemFrameList.size(); i++) {
			if (itemFrameList.get(i).getUniqueId().equals(rightClicked.getUniqueId())) {
				return i;
			}
		}
		return -1;
	}

	public boolean isMapFrame(ItemFrame frame) {
		for (BannerBoard l1 : this.loadedBannerBoards) {
			for (ItemFrame f : l1.buildItemFrameList()) {
				if (f.getUniqueId().equals(frame.getUniqueId())) {
					return true;
				}
			}
		}
		return false;
	}
}
