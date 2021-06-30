package me.bigteddy98.bannerboard;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import io.netty.channel.Channel;

public class PacketManager {

	private final static String GAME_NAME = "MINECRAFT";

	private static Constructor<?> constructor18;
	private static Constructor<?> constructor19;
	private static Constructor<?> constructor1_14;

	private final static Method getPlayerHandle;
	private final static Field playerConnection;

	private final static Field networkManager;
	private static Field channel;

	static {
		try {

			try {
				constructor1_14 = getNMS("PacketPlayOutMap").getConstructor(Integer.TYPE, Byte.TYPE, Boolean.TYPE,
						Boolean.TYPE, Collection.class, byte[].class, Integer.TYPE, Integer.TYPE, Integer.TYPE,
						Integer.TYPE);
			} catch (NoSuchMethodException e2) {
				try {
					constructor19 = getNMS("PacketPlayOutMap").getConstructor(Integer.TYPE, Byte.TYPE, Boolean.TYPE,
							Collection.class, byte[].class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
				} catch (NoSuchMethodException e) {
					constructor18 = getNMS("PacketPlayOutMap").getConstructor(Integer.TYPE, Byte.TYPE, Collection.class,
							byte[].class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
				}
			}

			getPlayerHandle = getCraft("entity.CraftPlayer").getMethod("getHandle", new Class[0]);
			playerConnection = getNMS("EntityPlayer").getDeclaredField("playerConnection");

			networkManager = getNMS("PlayerConnection").getDeclaredField("networkManager");
			for (Field field : getNMS("NetworkManager").getDeclaredFields()) {
				if (field.getType() == Channel.class) {
					channel = field;
				}
			}
			if (channel == null) {
				throw new RuntimeException("Could not find field with type channel in class NetworkManager");
			}
			channel.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	public static Class<?> getNMS(String name) throws ClassNotFoundException {
		// org.bukkit.craftbukkit.v1_9_R1
		// 0 1 2 3
		String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		return Class.forName("net." + GAME_NAME.toLowerCase() + ".server." + version + "." + name, true,
				Bukkit.class.getClassLoader());
	}

	public static Class<?> getCraft(String name) throws ClassNotFoundException {
		// org.bukkit.craftbukkit.v1_9_R1
		// 0 1 2 3
		String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		return Class.forName("org.bukkit.craftbukkit." + version + "." + name, true, Bukkit.class.getClassLoader());
	}

	public static Object getPacket(short mapId, byte[] buffer)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (constructor19 != null) {
			return constructor19.newInstance(mapId, (byte) 4, false, new ArrayList<>(), buffer, 0, 0, 128, 128);
		}
		if (constructor1_14 != null) {
			return constructor1_14.newInstance(mapId, (byte) 4, false, false, new ArrayList<>(), buffer, 0, 0, 128,
					128);
		}
		return constructor18.newInstance(mapId, (byte) 4, new ArrayList<>(), buffer, 0, 0, 128, 128);
	}

	public static Channel getChannel(Player p)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object entityPlayer = getPlayerHandle.invoke(p, new Object[0]);
		Object connection = playerConnection.get(entityPlayer);
		Object network = networkManager.get(connection);
		return (Channel) channel.get(network);
	}
}
