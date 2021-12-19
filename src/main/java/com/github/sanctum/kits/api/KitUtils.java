package com.github.sanctum.kits.api;

import com.github.sanctum.kits.RetroKits;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import java.util.function.Consumer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KitUtils {

	public static Consumer<Entity> writeToStation(@NotNull KitStation station) {
		return entity -> {
			NamespacedKey key = new NamespacedKey(JavaPlugin.getPlugin(RetroKits.class), "station");
			entity.getPersistentDataContainer().set(key, PersistentDataType.STRING, station.getCategory());
		};
	}

	public static @Nullable String readFromStation(@NotNull Entity entity) {
		if (!LabyrinthProvider.getInstance().isLegacy()) {
			NamespacedKey key = new NamespacedKey(JavaPlugin.getPlugin(RetroKits.class), "station");
			if (entity.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
				return entity.getPersistentDataContainer().get(key, PersistentDataType.STRING);
			}
		}
		return null;
	}

}
