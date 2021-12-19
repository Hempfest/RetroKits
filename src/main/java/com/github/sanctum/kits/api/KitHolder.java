package com.github.sanctum.kits.api;

import com.github.sanctum.kits.RetroKits;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.data.Node;
import com.github.sanctum.labyrinth.data.container.LabyrinthCollection;
import com.github.sanctum.labyrinth.interfacing.Nameable;
import com.github.sanctum.labyrinth.library.Deployable;
import com.github.sanctum.labyrinth.library.HFEncoded;
import java.io.NotSerializableException;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface KitHolder extends Nameable {

	@Nullable Kit getKit(@NotNull String name);

	@NotNull LabyrinthCollection<Kit> getKits();

	void add(@NotNull Kit kit);

	void remove(@NotNull Kit kit);

	@Override
	@NotNull String getName();

	@NotNull UUID getId();

	@NotNull OfflinePlayer getPlayer();

	@Nullable Kit getCurrent();

	@Nullable Kit getPrevious();

	@NotNull Deployable<Player> apply(Kit kit);

	default void save() {
		FileManager data = FileList.search(JavaPlugin.getPlugin(RetroKits.class)).get("holders", "data", FileType.JSON);
		getKits().forEach(k -> {
			Node kit = data.getRoot().getNode(getName()).getNode(k.getName());
			try {
				kit.getNode("armor").set(new HFEncoded(k.getArmor()).serialize());
				kit.getNode("inventory").set(new HFEncoded(k.getInventory()).serialize());
			} catch (NotSerializableException ex) {
				ex.printStackTrace();
			}
		});
		data.getRoot().save();
	}

}
