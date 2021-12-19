package com.github.sanctum.kits.api;

import com.github.sanctum.kits.RetroKits;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.formatting.string.SpecialID;
import com.github.sanctum.labyrinth.library.HFEncoded;
import java.io.NotSerializableException;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public interface KitStation {

	@NotNull SpecialID getId();

	@NotNull String getCategory();

	@NotNull Inventory getContents();

	@NotNull Inventory getInventory();

	default void save() {
		FileManager data = FileList.search(JavaPlugin.getPlugin(RetroKits.class)).get("stations", "data", FileType.JSON);
		try {
			data.getRoot().getNode(getId().toString()).getNode("content").set(new HFEncoded(getContents().getContents()).serialize());
			data.getRoot().getNode(getId().toString()).getNode("name").set(getCategory());
		} catch (NotSerializableException e) {
			e.printStackTrace();
		}
		data.getRoot().save();
	}

}
