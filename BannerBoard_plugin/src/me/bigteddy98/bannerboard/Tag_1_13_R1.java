package me.bigteddy98.bannerboard;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.inventory.ItemStack;

public class Tag_1_13_R1 {

	private static final Constructor<?> nmsItemStack;
	private static final Method getOrCreate;
	private static final Method setInt;
	private static final Method toBukkitStack;
	private static final Object filledMap;

	static {
		try {
			nmsItemStack = PacketManager.getNMS("ItemStack").getConstructor(PacketManager.getNMS("IMaterial"));
			getOrCreate = PacketManager.getNMS("ItemStack").getMethod("getOrCreateTag");
			setInt = PacketManager.getNMS("NBTTagCompound").getMethod("setInt", String.class, Integer.TYPE);
			toBukkitStack = PacketManager.getCraft("inventory.CraftItemStack").getMethod("asBukkitCopy", PacketManager.getNMS("ItemStack"));
			filledMap = PacketManager.getNMS("Items").getDeclaredField("FILLED_MAP").get(null);
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	// call itemstack.getOrCreateTag().setInt("map", i2);
	public static ItemStack buildMap(short id) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, ClassNotFoundException, NoSuchFieldException, SecurityException {
		final Object nmsStack = nmsItemStack.newInstance(filledMap);
		final Object nbtTag = getOrCreate.invoke(nmsStack);
		setInt.invoke(nbtTag, "map", id);

		final ItemStack stack = (ItemStack) toBukkitStack.invoke(null, nmsStack);
		stack.setDurability(id);

		return stack;
	}
}
