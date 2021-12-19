package com.github.sanctum.kits.listeners;

import com.github.sanctum.kits.api.KitManager;
import com.github.sanctum.kits.api.KitStation;
import com.github.sanctum.kits.api.KitUtils;
import com.github.sanctum.labyrinth.library.Entities;
import com.github.sanctum.labyrinth.library.Mailer;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerEventListener implements Listener {

	@EventHandler
	public void onInteract(PlayerInteractEntityEvent e) {
		if (e.getRightClicked() instanceof ItemFrame) {
			String station = KitUtils.readFromStation(e.getRightClicked());
			if (station != null) {
				KitStation s = KitManager.getInstance().getStation(station);
				if (s != null) {
					if (e.getPlayer().hasPermission("kits.staff")) {
						e.getPlayer().openInventory(e.getPlayer().isSneaking() ? s.getContents() : s.getInventory());
					} else {
						e.getPlayer().openInventory(s.getInventory());
					}
					e.setCancelled(true);
				} else {
					e.getRightClicked().remove();
				}
			}
		}
	}

	@EventHandler
	public void onHit(HangingBreakByEntityEvent e) {
		if (e.getRemover() instanceof Player && e.getEntity() instanceof ItemFrame) {
			String station = KitUtils.readFromStation(e.getEntity());
			if (station != null) {
				KitStation s = KitManager.getInstance().getStation(station);
				if (s != null) {
					if (e.getRemover().hasPermission("kits.staff") && ((Player) e.getRemover()).isSneaking()) {
						ItemFrame frame = (ItemFrame) e.getEntity();
						if (!((Player) e.getRemover()).getInventory().getItemInMainHand().getType().isAir()) {
							frame.setItem(((Player) e.getRemover()).getInventory().getItemInMainHand(), true);
							if (new Random().nextInt(3) == 2) {
								Mailer.empty(e.getRemover()).prefix().start("&7[").middle("&bKits").end("&7]").finish().chat("&c[Tip] &fCrouch left-click with nothing in your hand to break me.").queue();
							}
							e.setCancelled(true);
							return;
						}
					}
					if (!e.getRemover().hasPermission("kits.staff")) {
						e.setCancelled(true);
					} else {
						if (!((Player) e.getRemover()).isSneaking()) {
							e.setCancelled(true);
						}
					}
				} else {
					e.getEntity().remove();
				}
			}
		}
	}

	@EventHandler
	public void onHit(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player && e.getEntity() instanceof ItemFrame) {
			String station = KitUtils.readFromStation(e.getEntity());
			if (station != null) {
				KitStation s = KitManager.getInstance().getStation(station);
				if (s != null) {
					if (e.getDamager().hasPermission("kits.staff") && ((Player) e.getDamager()).isSneaking()) {
						ItemFrame frame = (ItemFrame) e.getEntity();
						if (!((Player) e.getDamager()).getInventory().getItemInMainHand().getType().isAir()) {
							frame.setItem(((Player) e.getDamager()).getInventory().getItemInMainHand(), true);
							if (new Random().nextInt(3) == 2) {
								Mailer.empty(e.getDamager()).prefix().start("&7[").middle("&bKits").end("&7]").finish().chat("&c[Tip] &fCrouch left-click with nothing in your hand to break me.").queue();
							}
							e.setCancelled(true);
							return;
						}
					}
					if (!e.getDamager().hasPermission("kits.staff")) {
						e.setCancelled(true);
					} else {
						if (!((Player) e.getDamager()).isSneaking()) {
							e.setCancelled(true);
						}
					}
				} else {
					e.getEntity().remove();
				}
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			KitStation station = KitManager.getInstance().getStation(e.getPlayer());
			if (station != null) {
				Block front = e.getClickedBlock().getRelative(e.getBlockFace(), 1);
				if (front.getType() == Material.AIR) {
					ItemFrame frame = Entities.ITEM_FRAME.spawn(front.getLocation(), itemFrame -> {
						Location spawn = itemFrame.getLocation().getBlock().getRelative(BlockFace.UP, 1).getLocation();
						e.getPlayer().getWorld().spawnParticle(Particle.HEART, spawn, 1);
					});
					KitUtils.writeToStation(station).accept(frame);
					KitManager.getInstance().setStation(e.getPlayer(), null);
				}
			}
		}
	}

}
