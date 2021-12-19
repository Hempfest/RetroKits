package com.github.sanctum.kits.api;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.container.LabyrinthCollection;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface KitManager {

	static KitManager getInstance() {
		return LabyrinthProvider.getInstance().getServicesManager().load(KitManager.class);
	}

	@NotNull KitHolder getHolder(@NotNull String name);

	@Nullable Kit getKit(@NotNull String name, @Nullable OfflinePlayer holder);

	@NotNull LabyrinthCollection<KitHolder> getHolders();

	@Nullable KitStation getStation(@NotNull String name);

	@Nullable KitStation getStation(@NotNull Player builder);

	void setStation(@NotNull Player builder, KitStation station);

	void add(@NotNull KitStation station);

	void remove(@NotNull KitStation station);

}
