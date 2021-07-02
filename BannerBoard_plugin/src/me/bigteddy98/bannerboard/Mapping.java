package me.bigteddy98.bannerboard;

import me.bigteddy98.bannerboard.util.VersionUtil;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class Mapping {

	private final static Method getHandle;
	private static Field worldMaps;
	private static Field mapField;
	private static Object dimensionManager;
	private static Method registerBase;
	private final static Constructor<?> worldMapConstructor;

	// 1.14 and higher
	private static Method getWorldPersistentData;
	// private static Method registerFunction;
	// private static Method fileFunction;
	private static Field dataMapField;

	static {
		try {
			getHandle = PacketManager.getCraft("CraftWorld").getMethod("getHandle"); // gives us WorldServer

			if (VersionUtil.isHigherThan("v1_13_R2")) {
				dimensionManager = PacketManager.getNMS("DimensionManager").getField("OVERWORLD").get(null);
				mapField = PacketManager.getNMS("WorldMap").getField("map");
			}

			if (VersionUtil.isHigherThan("v1_14_R1")) {
				// invoke getWorldPersistentData on WorldServer to obtain WorldPersistentData
				getWorldPersistentData = PacketManager.getNMS("WorldServer").getMethod("getWorldPersistentData");
				// registerFunction = find(PacketManager.getNMS("WorldPersistentData"), Supplier.class, String.class);
				// fileFunction = find(PacketManager.getNMS("WorldPersistentData"), String.class);
				// fileFunction.setAccessible(true);

				// obtain the map called data
				dataMapField = PacketManager.getNMS("WorldPersistentData").getDeclaredField("data");
			} else {

				worldMaps = PacketManager.getNMS("World").getField("worldMaps");
				if (VersionUtil.isHigherThan("v1_13_R2")) {
					registerBase = find(PacketManager.getNMS("PersistentCollection"), PacketManager.getNMS("DimensionManager"), String.class, PacketManager.getNMS("PersistentBase"));
				} else {
					registerBase = find(PacketManager.getNMS("PersistentCollection"), String.class, PacketManager.getNMS("PersistentBase"));
				}
			}

			worldMapConstructor = PacketManager.getNMS("WorldMap").getConstructor(String.class);
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private static Method find(Class<?> nms, Class<?>... params) {
		for (Method m : nms.getDeclaredMethods()) {
			if (equals(m.getParameterTypes(), params)) {
				return m;
			}
		}
		throw new NullPointerException("Could not find the method we were looking for");
	}

	private static boolean equals(Class<?>[] a, Class<?>[] b) {
		if (a.length != b.length)
			return false;
		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}

	private static Object getWorldMaps() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return worldMaps.get(getHandle.invoke(Bukkit.getServer().getWorlds().get(0)));
	}

	public static void claimId(short id) {
		String name = "map_" + id;
		try {
			final Object worldMap = worldMapConstructor.newInstance(name);

			if (VersionUtil.isHigherThan("v1_13_R2")) {
				mapField.set(worldMap, dimensionManager);
			}

			if (VersionUtil.isHigherThan("v1_14_R1")) {

				// invoke getWorldPersistentData on WorldServer to obtain WorldPersistentData
				final Object worldServer = getHandle.invoke(Bukkit.getServer().getWorlds().get(0));
				final Object worldPersistentData = getWorldPersistentData.invoke(worldServer);
				@SuppressWarnings("unchecked")
				final Map<String, Object> map = (Map<String, Object>) dataMapField.get(worldPersistentData);
				map.put(name, worldMap);

				/*
				// create file so that this method actually registers
				final File f = (File) fileFunction.invoke(worldPersistentData, name);
				f.createNewFile();
				
				registerFunction.invoke(worldPersistentData, new Supplier<Object>() {
				
					@Override
					public Object get() {
						return worldMap;
					}
				}, name);*/
			} else {
				if (VersionUtil.isHigherThan("v1_13_R2")) {
					// set the map field
					registerBase.invoke(getWorldMaps(), dimensionManager, name, worldMap);
				} else {
					registerBase.invoke(getWorldMaps(), name, worldMap);
				}
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
