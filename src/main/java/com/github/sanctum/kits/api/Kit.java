package com.github.sanctum.kits.api;

import com.github.sanctum.labyrinth.interfacing.Nameable;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface Kit extends Nameable {

	@Override
	@NotNull String getName();

	@NotNull KitHolder getOwner();

	@NotNull ItemStack[] getArmor();

	@NotNull ItemStack[] getInventory();

	static @NotNull Kit newInstance(@NotNull Player player, @NotNull String name) {
		return new Kit() {

			final ItemStack[] armor;
			final ItemStack[] inventory;
			final KitHolder holder;

			{
				this.armor = copy(player.getInventory().getArmorContents());
				this.inventory = copy(player.getInventory().getContents());
				this.holder = KitManager.getInstance().getHolder(player.getName());
			}

			ItemStack[] copy(ItemStack[] ar) {
				ItemStack[] n = new ItemStack[ar.length];
				for (int i = 0; i < ar.length; i++) {
					if (ar[i] == null) {
						n[i] = new ItemStack(Material.AIR);
					} else {
						n[i] = new ItemStack(ar[i]);
					}
				}
				return n;
			}

			@Override
			public @NotNull String getName() {
				return name;
			}

			@Override
			public @NotNull KitHolder getOwner() {
				return holder;
			}

			@Override
			public @NotNull ItemStack[] getArmor() {
				return armor;
			}

			@Override
			public @NotNull ItemStack[] getInventory() {
				return inventory;
			}
		};
	}

}
