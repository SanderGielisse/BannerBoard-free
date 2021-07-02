package me.bigteddy98.bannerboard;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.bigteddy98.bannerboard.util.VersionUtil;

public class BannerBoardInjector extends ChannelDuplexHandler {

	private static Field itemList_19;
	private static Method getPresent_19;
	private static Class<?> present_19;

	private static Field objectField18;

	private final static Field datawatcher;
	private final static Class<?> itemStack;
	private final static Field damage;
	private final static Field item;
	private final static Method getName;
	private final static Method getDamage;

	private final static Class<?> packetPlayOutEntityMetadata;
	private final static Class<?> packetPlayOutMap;

	private final static Field packetPlayOutMapId;
	static final String VERSION;

	static {
		try {
			VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

			// has two fields
			if (VERSION.startsWith("v1_9_") || VersionUtil.isHigherThan("v1_10_R1")) {
				// 1.9 related
				itemList_19 = PacketManager.getNMS("DataWatcher$Item").getDeclaredField("b");
				itemList_19.setAccessible(true);

				present_19 = Class.forName("com.google.common.base.Present");
				getPresent_19 = Class.forName("com.google.common.base.Optional").getMethod("get");
			}

			if (VERSION.startsWith("v1_8_")) {

				try {
					// 1.8 related
					objectField18 = PacketManager.getNMS("DataWatcher$WatchableObject").getDeclaredField("c");
					objectField18.setAccessible(true);
				} catch (ClassNotFoundException e) {
					objectField18 = PacketManager.getNMS("WatchableObject").getDeclaredField("c");
					objectField18.setAccessible(true);
				}
			}

			// global
			datawatcher = PacketManager.getNMS("PacketPlayOutEntityMetadata").getDeclaredField("b");
			datawatcher.setAccessible(true);

			itemStack = PacketManager.getNMS("ItemStack");

			if (VersionUtil.isHigherThan("v1_13_R1")) {
				getDamage = PacketManager.getNMS("ItemStack").getMethod("getDamage");
				damage = null;
			} else {
				damage = PacketManager.getNMS("ItemStack").getDeclaredField("damage");
				damage.setAccessible(true);
				getDamage = null;
			}

			item = PacketManager.getNMS("ItemStack").getDeclaredField("item");
			item.setAccessible(true);

			getName = PacketManager.getNMS("Item").getMethod("getName");

			packetPlayOutEntityMetadata = PacketManager.getNMS("PacketPlayOutEntityMetadata");
			packetPlayOutMap = PacketManager.getNMS("PacketPlayOutMap");

			packetPlayOutMapId = packetPlayOutMap.getDeclaredField("a");
			packetPlayOutMapId.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException | ClassNotFoundException | NoSuchMethodException e) {
			System.out.println("The Error:");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private final Object mapLock = new Object();
	private final Map<Short, Object> packets = new HashMap<>();
	private final Set<Short> banners = new HashSet<>();

	private ChannelHandlerContext ctx;
	private final Object ctxLock = new Object();

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {

		try {
			synchronized (this.ctxLock) {
				this.ctx = ctx;
				this.ctxLock.notifyAll();
			}

			if (packetPlayOutMap.isInstance(msg)) {
				// check if it's one of our maps, then return
				short id = (short) ((int) packetPlayOutMapId.get(msg));
				if (banners.contains(id)) {
					return;
				}
			}
			super.write(ctx, msg, promise);

			if (packetPlayOutEntityMetadata.isInstance(msg)) {
				Short dam = this.getMapId(msg);
				if (dam != null) { // it's not an itemframe
					synchronized (mapLock) {
						if (banners.contains(dam)) {
							Object packet = packets.get(dam);
							if (packet != null) {
								ctx.write(packet);
							}
						}
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public short getDamage(Object itemstack)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (VersionUtil.isHigherThan("v1_13_R1")) {
			// getDamage is initialized as null and could therefore be null. Just in case.
			if (getDamage == null) {
				return 0;
			}
			
			return (short) ((int) getDamage.invoke(itemstack));
		}
		
		// damage is initialized as null and could therefore be null. Just in case.
		if (damage == null) {
			return 0;
		}

		return (short) ((int) damage.get(itemstack));
	}

	public Short getMapId(Object packetPlayOutEntityMetadata)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

		// has two fields
		if (VersionUtil.isHigherThan("v1_11_R1")) {
			List<?> data = (List<?>) datawatcher.get(packetPlayOutEntityMetadata);
			if (data != null) {
				for (Object o : data) {
					// grab field b
					Object val = itemList_19.get(o);
					if (itemStack.isInstance(val)) {
						// we have an item stack

						Object it = item.get(val); // this is null when no item
						if (it != null) {
							String name = (String) getName.invoke(it);
							if (name.equalsIgnoreCase("item.minecraft.filled_map")
									|| name.equalsIgnoreCase("item.map")) {
								return getDamage(val);
							}
						}
					}
				}
			}
			return null;
		} else if (version.startsWith("v1_9_") || version.equals("v1_10_R1")) {
			List<?> data = (List<?>) datawatcher.get(packetPlayOutEntityMetadata);
			for (Object o : data) {
				// grab field b
				Object b = itemList_19.get(o);
				if (present_19.isInstance(b)) {
					Object val = getPresent_19.invoke(b);
					if (itemStack.isInstance(val)) {
						// we have an item stack
						String name = (String) getName.invoke(item.get(val));
						if (name.equalsIgnoreCase("item.map")) {
							return getDamage(val);
						}
					}
				}
			}
			return null;
		} else if (version.startsWith("v1_8_")) {
			List<?> data = (List<?>) datawatcher.get(packetPlayOutEntityMetadata);
			for (Object o : data) {
				// DataWatcher.WatchableObject
				Object val = objectField18.get(o); // field c
				if (itemStack.isInstance(val)) {
					// we have an itemstack
					String name = (String) getName.invoke(item.get(val));
					if (name.equalsIgnoreCase("item.map")) {
						return getDamage(val);
					}
				}
			}
			return null;
		}
		throw new RuntimeException("Unknown version " + version);
	}

	// here the mapId is set for every player
	public void addFrame(short mapId, byte[] data) {
		try {
			synchronized (mapLock) {
				final Object packet = PacketManager.getPacket(mapId, data);
				this.packets.put(mapId, packet);
				Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
					try {
						synchronized (ctxLock) {
							while (ctx == null) {
								ctxLock.wait();
							}
							ctx.write(packet);
						}
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				});
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public void clearBoards() {
		synchronized (mapLock) {
			this.packets.clear();
		}
	}

	public void addMapId(short durability) {
		synchronized (mapLock) {
			this.banners.add(durability);
		}
	}
}
