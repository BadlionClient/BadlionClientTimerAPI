package net.badlion.timers.impl;

import net.badlion.timers.TimerPlugin;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class NmsManager {
	private static TimerPlugin plugin;

	private static String versionSuffix;

	private static Method getHandleMethod;

	private static Field playerConnectionField;

	private static Method sendPacketMethod;

	private static Constructor<?> packetPlayOutCustomPayloadConstructor;

	// Bukkit 1.8+ support
	private static Class<?> packetDataSerializerClass;
	private static Constructor<?> packetDataSerializerConstructor;

	private static Method wrappedBufferMethod;

	public static void init(TimerPlugin plugin) {

		NmsManager.plugin = plugin;

		// Get the v1_X_Y from the end of the package name, e.g. v_1_7_R4 or v_1_12_R1
		String packageName = plugin.getServer().getClass().getPackage().getName();
		String[] parts = packageName.split("\\.");

		if (parts.length > 0) {
			String suffix = parts[parts.length - 1];
			if (!suffix.startsWith("v")) {
				throw new RuntimeException("Failed to find version for running Minecraft server, got suffix " + suffix);
			}

			NmsManager.versionSuffix = suffix;

			plugin.getLogger().info("Found version " + NmsManager.versionSuffix);
		}

		// We need to use reflection because Bukkit by default handles plugin messages in a really silly way
		// Reflection stuff
		Class<?> craftPlayerClass = NmsManager.getClass("org.bukkit.craftbukkit." + NmsManager.versionSuffix + ".entity.CraftPlayer");
		if (craftPlayerClass == null) {
			throw new RuntimeException("Failed to find CraftPlayer class");
		}

		Class<?> nmsPlayerClass = NmsManager.getClass("net.minecraft.server." + NmsManager.versionSuffix + ".EntityPlayer");
		if (nmsPlayerClass == null) {
			throw new RuntimeException("Failed to find EntityPlayer class");
		}

		Class<?> playerConnectionClass = NmsManager.getClass("net.minecraft.server." + NmsManager.versionSuffix + ".PlayerConnection");
		if (playerConnectionClass == null) {
			throw new RuntimeException("Failed to find PlayerConnection class");
		}

		Class<?> packetPlayOutCustomPayloadClass = NmsManager.getClass("net.minecraft.server." + NmsManager.versionSuffix + ".PacketPlayOutCustomPayload");
		if (packetPlayOutCustomPayloadClass == null) {
			throw new RuntimeException("Failed to find PacketPlayOutCustomPayload class");
		}

		NmsManager.packetPlayOutCustomPayloadConstructor = NmsManager.getConstructor(packetPlayOutCustomPayloadClass, String.class, byte[].class);
		if (NmsManager.packetPlayOutCustomPayloadConstructor == null) {
			// Newer versions of Minecraft use a different custom packet system
			NmsManager.packetDataSerializerClass = NmsManager.getClass("net.minecraft.server." + NmsManager.versionSuffix + ".PacketDataSerializer");
			if (NmsManager.packetDataSerializerClass == null) {
				throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor or PacketDataSerializer class");
			}

			// Netty classes used by newer 1.8 and newer
			Class<?> byteBufClass = NmsManager.getClass("io.netty.buffer.ByteBuf");
			if (byteBufClass == null) {
				throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor or ByteBuf class");
			}

			NmsManager.packetDataSerializerConstructor = NmsManager.getConstructor(NmsManager.packetDataSerializerClass, byteBufClass);
			if (NmsManager.packetDataSerializerConstructor == null) {
				throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor or PacketDataSerializer constructor");
			}

			Class<?> unpooledClass = NmsManager.getClass("io.netty.buffer.Unpooled");
			if (unpooledClass == null) {
				throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor or Unpooled class");
			}

			NmsManager.wrappedBufferMethod = NmsManager.getMethod(unpooledClass, "wrappedBuffer", byte[].class);
			if (NmsManager.wrappedBufferMethod == null) {
				throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor or wrappedBuffer()");
			}

			// If we made it this far in theory we are on at least 1.8
			NmsManager.packetPlayOutCustomPayloadConstructor = NmsManager.getConstructor(packetPlayOutCustomPayloadClass, String.class, NmsManager.packetDataSerializerClass);
			if (NmsManager.packetPlayOutCustomPayloadConstructor == null) {
				throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor 2x");
			}
		}

		NmsManager.getHandleMethod = NmsManager.getMethod(craftPlayerClass, "getHandle");
		if (NmsManager.getHandleMethod == null) {
			throw new RuntimeException("Failed to find CraftPlayer.getHandle()");
		}

		NmsManager.playerConnectionField = NmsManager.getField(nmsPlayerClass, "playerConnection");
		if (NmsManager.playerConnectionField == null) {
			throw new RuntimeException("Failed to find EntityPlayer.playerConnection");
		}

		NmsManager.sendPacketMethod = NmsManager.getMethod(playerConnectionClass, "sendPacket");
		if (NmsManager.sendPacketMethod == null) {
			throw new RuntimeException("Failed to find PlayerConnection.sendPacket()");
		}
	}

	public static void sendPluginMessage(Player player, String channel, byte[] message) {
		try {
			Object packet;

			// Newer MC version, setup ByteBuf object
			if (NmsManager.packetDataSerializerClass != null) {
				Object byteBuf = NmsManager.wrappedBufferMethod.invoke(null, (Object) message);
				Object packetDataSerializer = NmsManager.packetDataSerializerConstructor.newInstance(byteBuf);

				packet = NmsManager.packetPlayOutCustomPayloadConstructor.newInstance(channel, packetDataSerializer);
			} else {
				// Work our magic to make the packet
				packet = NmsManager.packetPlayOutCustomPayloadConstructor.newInstance(channel, message);
			}

			// Work our magic to send the packet
			Object nmsPlayer = NmsManager.getHandleMethod.invoke(player);
			Object playerConnection = NmsManager.playerConnectionField.get(nmsPlayer);
			NmsManager.sendPacketMethod.invoke(playerConnection, packet);
		} catch (Exception ex) {
			NmsManager.plugin.getLogger().severe("Failed to send BLC CPS packet");
			ex.printStackTrace();
		}
	}

	private static Class<?> getClass(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	private static Constructor<?> getConstructor(Class<?> clazz, Class<?>... params) {
		for (final Constructor<?> constructor : clazz.getDeclaredConstructors()) {
			if (Arrays.equals(constructor.getParameterTypes(), params)) {
				constructor.setAccessible(true);
				return constructor;
			}
		}

		return null;
	}

	private static Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
		for (final Method method : clazz.getDeclaredMethods()) {
			if (method.getName().equals(methodName)) {
				if (params.length > 0) {
					if (Arrays.equals(method.getParameterTypes(), params)) {
						method.setAccessible(true);
						return method;
					}
				} else {
					method.setAccessible(true);
					return method;
				}
			}
		}

		return null;
	}

	private static Field getField(Class<?> clazz, String fieldName) {
		for (final Field field : clazz.getDeclaredFields()) {
			if (field.getName().equals(fieldName)) {
				field.setAccessible(true);
				return field;
			}
		}

		return null;
	}
}
