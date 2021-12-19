package com.github.sanctum.kits;

import com.github.sanctum.kits.api.Kit;
import com.github.sanctum.kits.api.KitHolder;
import com.github.sanctum.kits.api.KitManager;
import com.github.sanctum.kits.api.KitStation;
import com.github.sanctum.kits.listeners.PlayerEventListener;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.data.Node;
import com.github.sanctum.labyrinth.data.container.LabyrinthCollection;
import com.github.sanctum.labyrinth.data.container.LabyrinthEntryMap;
import com.github.sanctum.labyrinth.data.container.LabyrinthMap;
import com.github.sanctum.labyrinth.data.service.PlayerSearch;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.formatting.string.SpecialID;
import com.github.sanctum.labyrinth.gui.unity.construct.Menu;
import com.github.sanctum.labyrinth.library.CommandUtils;
import com.github.sanctum.labyrinth.library.Deployable;
import com.github.sanctum.labyrinth.library.HFEncoded;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.StringUtils;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RetroKits extends JavaPlugin implements KitManager {

	static final LabyrinthMap<String, KitHolder> HOLDERS = new LabyrinthEntryMap<>();
	static final LabyrinthMap<String, KitStation> STATIONS = new LabyrinthEntryMap<>();
	static final LabyrinthMap<Player, KitStation> PLACEMENTS = new LabyrinthEntryMap<>();

	@Override
	public void onEnable() {
		// Plugin startup logic
		LabyrinthProvider.getInstance().getServicesManager().register(this, this, ServicePriority.High);
		LabyrinthProvider.getService(Service.VENT).subscribe(this, new PlayerEventListener());
		PlayerSearch.values().forEach(playerSearch -> getHolder(playerSearch.getName()));
		FileList files = FileList.search(this);
		FileManager data = files.get("stations", "data", FileType.JSON);
		FileManager config = files.get("Config");

		if (!config.getRoot().exists()) {
			files.copyYML("Config", config);
			config.getRoot().reload();
		}

		for (String sta : data.getRoot().getKeys(false)) {
			KitStation station = new KitStation() {

				final String category = data.getRoot().getNode(sta).getNode("name").toPrimitive().getString();
				final Inventory contents = Bukkit.createInventory(null, Menu.Rows.SIX.getSize(), StringUtils.use(MessageFormat.format(config.read(c -> c.getString("gui.station-title-edit")), category)).translate());

				{
					contents.setContents(new HFEncoded(data.getRoot().getNode(sta).getNode("content").toPrimitive().getString()).deserialize(ItemStack[].class));
				}

				@Override
				public @NotNull SpecialID getId() {
					return SpecialID.builder().setLength(33).build(category);
				}

				@Override
				public @NotNull String getCategory() {
					return category;
				}

				@Override
				public @NotNull Inventory getContents() {
					return contents;
				}

				@Override
				public @NotNull Inventory getInventory() {
					Inventory inv = Bukkit.createInventory(null, Menu.Rows.SIX.getSize(), StringUtils.use(MessageFormat.format(config.read(c -> c.getString("gui.station-title")), getCategory())).translate());
					inv.setContents(contents.getContents());
					return inv;
				}
			};
			add(station);
		}
		CommandUtils.read(entry -> {
			CommandMap map = entry.getKey();
			map.register("kit", getName(), new Command("kit") {

				public boolean player(Player p, String label, String[] args) {

					final Mailer msg = Mailer.empty(p).prefix().start("&7[").middle("&bKits").end("&7]").finish();
					final KitHolder holder = KitManager.getInstance().getHolder(p.getName());

					if (args.length == 0) {
						return true;
					}

					if (args.length == 1) {
						Kit test = holder.getKit(args[0]);
						if (test != null) {
							holder.apply(test).deploy(unused -> {
								msg.chat("&bYou received kit &e" + test.getName()).queue();
							});
						}
						return true;
					}

					if (args.length == 2) {
						if (args[0].equalsIgnoreCase("station")) {
							if (!p.hasPermission("kits.staff")) {
								msg.chat("&cYou don't have permission.");
								return true;
							}
							KitStation test = KitManager.getInstance().getStation(args[1]);
							if (test == null) {
								KitStation station = new KitStation() {

									final String category = args[1];
									final Inventory contents = Bukkit.createInventory(null, Menu.Rows.SIX.getSize(), StringUtils.use(MessageFormat.format(config.read(c -> c.getString("gui.station-title-edit")), args[1])).translate());

									@Override
									public @NotNull SpecialID getId() {
										return SpecialID.builder().setLength(33).build(category);
									}

									@Override
									public @NotNull String getCategory() {
										return category;
									}

									@Override
									public @NotNull Inventory getContents() {
										return contents;
									}

									@Override
									public @NotNull Inventory getInventory() {
										Inventory inv = Bukkit.createInventory(null, Menu.Rows.SIX.getSize(), StringUtils.use(MessageFormat.format(config.read(c -> c.getString("gui.station-title")), getCategory())).translate());
										inv.setContents(contents.getContents());
										return inv;
									}
								};
								KitManager.getInstance().add(station);
								p.openInventory(station.getContents());
							} else {
								p.openInventory(test.getContents());
							}
							return true;
						}
						if (args[0].equalsIgnoreCase("save")) {
							Kit test = holder.getKit(args[1]);
							if (test == null) {
								Kit n = Kit.newInstance(p, args[1]);
								holder.add(n);
								msg.chat("&3Saved new kit &e" + n.getName()).queue();
							} else {
								holder.remove(test);
								Kit n = Kit.newInstance(p, args[1]);
								holder.add(n);
								msg.chat("&bUpdated kit &6" + n.getName()).queue();
							}
							return true;
						}
						if (args[0].equalsIgnoreCase("delete")) {
							Kit test = holder.getKit(args[1]);
							if (test != null) {
								holder.remove(test);
								msg.chat("&cRemoved kit &6" + test.getName()).queue();
							} else {
								msg.chat("&cKit &6" + args[1] + " &cdoesn't exist.").queue();
							}
							return true;
						}
						return true;
					}

					if (args.length == 3) {
						if (args[0].equalsIgnoreCase("station")) {
							if (!p.hasPermission("kits.staff")) {
								msg.chat("&cYou don't have permission.");
								return true;
							}
							if (args[2].equalsIgnoreCase("build")) {
								KitStation test = KitManager.getInstance().getStation(args[1]);
								if (test != null) {
									KitManager.getInstance().setStation(p, test);
									msg.chat("&eKit station &5" + test.getCategory() + " &enow selected, right-click a block to build.").queue();
								}
								return true;
							}
							if (args[2].equalsIgnoreCase("delete")) {
								KitStation test = KitManager.getInstance().getStation(args[1]);
								if (test != null) {
									KitManager.getInstance().remove(test);
									msg.chat("&cKit station &d" + test.getCategory() + " &cdeleted.").queue();
								}
							}
						}
						return true;
					}

					return true;
				}

				public boolean console(CommandSender sender, String label, String[] args) {
					return false;
				}

				@NotNull
				@Override
				public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
					return SimpleTabCompletion.of(args)
							.then(TabCompletionIndex.ONE, () -> {
								List<String> completions = new ArrayList<>();
								if (sender.hasPermission("kits.staff")) {
									completions.add("station");
								}
								completions.add("save");
								completions.add("delete");
								completions.addAll(KitManager.getInstance().getHolder(sender.getName()).getKits().stream().map(Kit::getName).collect(Collectors.toList()));
								return completions;
							})
							.then(TabCompletionIndex.TWO, "station", TabCompletionIndex.ONE, () -> {
								List<String> completions = new ArrayList<>();
								if (sender.hasPermission("kits.staff")) {
									completions.addAll(STATIONS.values().stream().map(KitStation::getCategory).collect(Collectors.toList()));
								}
								return completions;
							})
							.then(TabCompletionIndex.TWO, "save", TabCompletionIndex.ONE, KitManager.getInstance().getHolder(sender.getName()).getKits().stream().map(Kit::getName).collect(Collectors.toList()))
							.then(TabCompletionIndex.TWO, "delete", TabCompletionIndex.ONE, KitManager.getInstance().getHolder(sender.getName()).getKits().stream().map(Kit::getName).collect(Collectors.toList()))
							.then(TabCompletionIndex.THREE, "station", TabCompletionIndex.ONE, () -> {
								List<String> completions = new ArrayList<>();
								if (sender.hasPermission("kits.staff")) {
									completions.add("build");
									completions.add("delete");
								}
								return completions;
							}).get();
				}

				@Override
				public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
					if (sender instanceof Player) {
						return player(((Player) sender), commandLabel, args);
					} else {
						return console(sender, commandLabel, args);
					}
				}
			});
			return null;
		});

	}

	@Override
	public void onDisable() {
		HOLDERS.values().forEach(KitHolder::save);
		STATIONS.values().forEach(KitStation::save);
	}

	@Override
	public @NotNull KitHolder getHolder(@NotNull String name) {
		return HOLDERS.computeIfAbsent(name, section -> {
			FileManager data = FileList.search(JavaPlugin.getPlugin(RetroKits.class)).get("holders", "data", FileType.JSON);
			KitHolder holder = new KitHolder() {

				final LabyrinthMap<String, Kit> kits = new LabyrinthEntryMap<>();
				final PlayerSearch parent = PlayerSearch.of(name);
				Kit current;
				Kit previous;

				@Override
				public @Nullable Kit getKit(@NotNull String name) {
					return kits.get(name);
				}

				@Override
				public @NotNull LabyrinthCollection<Kit> getKits() {
					return kits.values();
				}

				@Override
				public void add(@NotNull Kit kit) {
					kits.put(kit.getName(), kit);
				}

				@Override
				public void remove(@NotNull Kit kit) {
					kits.remove(kit.getName());
					data.getRoot().getNode(name).getNode(kit.getName()).set(null);
					data.getRoot().save();
				}

				@Override
				public @NotNull String getName() {
					return parent.getName();
				}

				@Override
				public @NotNull UUID getId() {
					return parent.getRecordedId();
				}

				@Override
				public @NotNull OfflinePlayer getPlayer() {
					return parent.getPlayer();
				}

				@Override
				public @Nullable Kit getCurrent() {
					return current;
				}

				@Override
				public @Nullable Kit getPrevious() {
					return previous;
				}

				@Override
				public @NotNull Deployable<Player> apply(Kit kit) {
					return Deployable.of(getPlayer().getPlayer(), player -> {
						if (player != null) {
							player.getInventory().clear();
							player.getInventory().setContents(kit.getInventory());
							player.getInventory().setArmorContents(kit.getArmor());
							previous = current;
							current = kit;
						} else
							JavaPlugin.getPlugin(RetroKits.class).getLogger().warning("- Unable to give kit to null player " + getName());
					});
				}
			};

			if (data.getRoot().isNode(name)) { // load their kits
				Node kits = data.getRoot().getNode(name);
				for (String kit : kits.getKeys(false)) {
					ItemStack[] armor = new HFEncoded(kits.getNode(kit).getNode("armor").toPrimitive().getString()).deserialize(ItemStack[].class);
					ItemStack[] inventory = new HFEncoded(kits.getNode(kit).getNode("inventory").toPrimitive().getString()).deserialize(ItemStack[].class);
					Kit k = new Kit() {

						final KitHolder hold;
						final ItemStack[] arm;
						final ItemStack[] inv;

						{
							this.hold = holder;
							this.arm = armor;
							this.inv = inventory;
						}

						@Override
						public @NotNull String getName() {
							return kit;
						}

						@Override
						public @NotNull KitHolder getOwner() {
							return hold;
						}

						@Override
						public @NotNull ItemStack[] getArmor() {
							return arm;
						}

						@Override
						public @NotNull ItemStack[] getInventory() {
							return inv;
						}
					};
					holder.add(k);
				}
			}
			return holder;
		});
	}

	@Override
	public @Nullable Kit getKit(@NotNull String name, @Nullable OfflinePlayer holder) {
		if (holder != null) {
			return getHolders().stream().filter(h -> h.getName().equals(holder.getName()) && h.getKit(name) != null).findFirst().map(kitHolder -> kitHolder.getKit(name)).orElse(null);
		} else
			return getHolders().stream().filter(h -> h.getKit(name) != null).findFirst().map(kitHolder -> kitHolder.getKit(name)).orElse(null);
	}

	@Override
	public @NotNull LabyrinthCollection<KitHolder> getHolders() {
		return HOLDERS.values();
	}

	@Override
	public @Nullable KitStation getStation(@NotNull String name) {
		return STATIONS.values().stream().filter(k -> k.getCategory().equals(name)).findFirst().orElse(null);
	}

	@Override
	public @Nullable KitStation getStation(@NotNull Player builder) {
		return PLACEMENTS.get(builder);
	}

	@Override
	public void setStation(@NotNull Player builder, KitStation station) {
		PLACEMENTS.put(builder, station);
	}

	@Override
	public void add(@NotNull KitStation station) {
		STATIONS.put(station.getCategory(), station);
	}

	@Override
	public void remove(@NotNull KitStation station) {
		STATIONS.remove(station.getCategory());
		FileManager data = FileList.search(this).get("stations", "data", FileType.JSON);
		data.getRoot().getNode(station.getId().toString()).set(null);
		data.getRoot().save();
	}
}
