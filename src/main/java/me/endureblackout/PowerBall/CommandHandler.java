
package me.endureblackout.PowerBall;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import me.endureblackout.VanityGear.VanityGear;
import me.endureblackout.VanityGear.Helpers.Armor;
import me.endureblackout.VanityGear.Helpers.Weapon;

public class CommandHandler implements CommandExecutor, Listener {

	PowerBall core;
	YamlConfiguration config;

	public CommandHandler(PowerBall instance, YamlConfiguration config) {
		this.core = instance;
		this.config = config;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (cmd.getName().equalsIgnoreCase("pb")) {
				if (args.length == 1) {
					if (args[0].equalsIgnoreCase("spawn") && p.hasPermission("powerball.spawn")) {
						ItemStack ball = new ItemStack(Material.DRAGON_EGG);
						ItemMeta bMeta = ball.getItemMeta();

						bMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("PBName")));

						List<String> lore = new ArrayList<String>();

						if (bMeta.getLore() == null) {
							lore.add(ChatColor.translateAlternateColorCodes('&', "&d/pb use"));
							lore.add(ChatColor.translateAlternateColorCodes('&', "&7&k/mccuse"));
						} else {
							lore.addAll(bMeta.getLore());
							lore.add(ChatColor.translateAlternateColorCodes('&', "&d/pb use"));
							lore.add(ChatColor.translateAlternateColorCodes('&', "&7&k/mccuse"));
						}

						bMeta.setLore(lore);
						ball.setItemMeta(bMeta);
						p.getInventory().addItem(ball);
					} else if (args[0].equalsIgnoreCase("use")) {
						ConfigurationSection itemSec = config.getConfigurationSection("Items");
						Set<String> items = itemSec.getKeys(false);
						Inventory pbMenu = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "Prize Egg");

						if (p.getInventory().getItemInMainHand().getType().equals(Material.DRAGON_EGG)) {
							for (String k : items) {
								ConfigurationSection item = itemSec.getConfigurationSection(k);

								Random rand = new Random();
								float chance = rand.nextFloat();
								if (chance <= Float.parseFloat(item.getString("Chance"))) {
									if (item.isSet("VanityName")) {
										pbMenu.setItem(pbMenu.firstEmpty(),
												getVanityArmor(item.getString("VanityName"), item.getString("Piece")));
									} else if (item.isSet("VanityWeaponName")) {
										pbMenu.setItem(pbMenu.firstEmpty(),
												getVanityWeapon(item.getString("VanityWeaponName")));
									} else if (item.isSet("Pattern") && item.isSet("Trim")) {
										pbMenu.setItem(pbMenu.firstEmpty(),
												getArmorWithTrim(item.getString("Item"), item.getString("Name"),
														item.getStringList("Lore"), item.getStringList("Enchantments"),
														item.getString("Trim"), item.getString("Pattern"),
														item.getInt("Amount")));
									} else if (isLeather(item.getName())) {
										pbMenu.setItem(pbMenu.firstEmpty(), createLeatherItem(item.getName()));
									} else if (item.getString("Item").toLowerCase().contains("potion")) {
										pbMenu.setItem(pbMenu.firstEmpty(),
												getPotion(item.getString("Item"), item.getString("Name"),
														item.getStringList("Lore"), item.getString("Effect"),
														item.getInt("Amount")));
									} else {
										pbMenu.setItem(pbMenu.firstEmpty(),
												getItem(item.getString("Item"), item.getString("Name"),
														item.getStringList("Lore"), item.getStringList("Enchantments"),
														item.getInt("Amount")));
									}
								}
							}

							p.openInventory(pbMenu);
						}
					}
				} else if (args.length == 2) {
					if (args[0].equalsIgnoreCase("spawn") && p.hasPermission("powerball.spawn")) {
						ItemStack ball = new ItemStack(Material.DRAGON_EGG);
						ItemMeta bMeta = ball.getItemMeta();

						String sendTo = args[1];
						Player pTo = null;

						for (Player f : Bukkit.getOnlinePlayers()) {
							if (f.getName().equalsIgnoreCase(sendTo)) {
								pTo = f;
							}
						}

						if (pTo == null) {
							p.sendMessage(ChatColor.DARK_RED
									+ "This Player is offline so the item could not be added to their inventory.");
							return true;
						}

						bMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("PBName")));

						List<String> lore = new ArrayList<String>();

						if (bMeta.getLore() == null) {
							lore.add(ChatColor.translateAlternateColorCodes('&', "&d/pb use"));
							lore.add(ChatColor.translateAlternateColorCodes('&', "&7/mccuse"));
						} else {
							lore.addAll(bMeta.getLore());
							lore.add(ChatColor.translateAlternateColorCodes('&', "&d/pb use"));
							lore.add(ChatColor.translateAlternateColorCodes('&', "&7/mccuse"));
						}

						bMeta.setLore(lore);
						ball.setItemMeta(bMeta);
						pTo.getInventory().addItem(ball);
					}
				} else if (args.length == 3) {
					if (args[0].equalsIgnoreCase("add") && sender.hasPermission("powerball.admin")) {
						String name = args[1];
						double chance = Float.parseFloat(args[2]);

						ItemStack item = p.getInventory().getItemInMainHand();

						ConfigurationSection items = config.getConfigurationSection("Items");

						if (items.getKeys(false).contains(name)) {
							p.sendMessage(ChatColor.RED + "An item by that name already existing in the config");

							return true;
						} else {
							if (item.hasItemMeta()) {
								ItemMeta iMeta = item.getItemMeta();

								if (item.getType().toString().toLowerCase().contains("potion")) {
									PotionMeta pMeta = (PotionMeta) iMeta;

									items.set(name + ".Item", item.getType().toString().toLowerCase());
									items.set(name + ".Amount", item.getAmount());
									items.set(name + ".Effect",
											pMeta.getBasePotionData().getType().toString().toLowerCase());
									items.set(name + ".Chance", chance);

									config.set("Items", items);

									core.getConfig().set("Items", items);
									core.saveConfig();

									p.sendMessage(ChatColor.GREEN + "Item added successfully");
								} else {
									String displayName = iMeta.getDisplayName();

									if (VanityGear.ARMOR.containsKey(
											ChatColor.stripColor(displayName.replace("Armor", "")).trim())) {
										items.set(name + ".Chance", chance);
										items.set(name + ".Amount", item.getAmount());
										items.set(name + ".VanityName",
												ChatColor.stripColor(displayName.replace("Armor", "")).trim());

										switch (item.getType()) {
										case LEATHER_HELMET:
											items.set(name + ".Piece", "helmet");
											break;
										case LEATHER_CHESTPLATE:
											items.set(name + ".Piece", "chest");
											break;
										case LEATHER_LEGGINGS:
											items.set(name + ".Piece", "legs");
											break;
										case LEATHER_BOOTS:
											items.set(name + ".Piece", "boots");
											break;
										default:
											p.sendMessage(ChatColor.RED + "There was a problem adding this item");

										}

										config.set("Items", items);

										core.getConfig().set("Items", items);
										core.saveConfig();

										p.sendMessage(ChatColor.GREEN + "Item added successfully");
									} else if (VanityGear.WEAPON
											.containsKey(ChatColor.stripColor(displayName).trim())) {
										items.set(name + ".VanityWeaponName", ChatColor.stripColor(displayName).trim());
										items.set(name + ".Amount", item.getAmount());
										items.set(name + ".Type", item.getType().toString());
										items.set(name + ".Chance", chance);

										config.set("Items", items);
										core.getConfig().set("Items", items);
										core.saveConfig();

										p.sendMessage(ChatColor.GREEN + "Item added successfully");
									} else {
										items.set(name + ".Item", item.getType().toString().toLowerCase());
										items.set(name + ".Amount", item.getAmount());
										items.set(name + ".Chance", chance);

										if (iMeta.hasLore()) {
											List<String> loreList = iMeta.getLore();

											if (iMeta.hasEnchants()) {
												Bukkit.getLogger().info("" + loreList.size());
												for (Map.Entry<Enchantment, Integer> e : iMeta.getEnchants()
														.entrySet()) {
													for (ListIterator<String> it = loreList.listIterator(); it
															.hasNext();) {
														String value = it.next();
														Bukkit.getLogger().info(value);
														if (value.toLowerCase()
																.contains(e.getKey().getKey().toString().split(":")[1]
																		.toLowerCase())) {
															it.remove();
														}
													}
												}
											}

											for (String line : loreList) {
												line.replace("§", "&");
											}

											items.set(name + ".Lore", loreList);

											item.setItemMeta(iMeta);
										}

										if (isArmor(item.getType().toString().toLowerCase())) {
											ArmorMeta aMeta = (ArmorMeta) iMeta;

											if (aMeta.hasTrim()) {
												ArmorTrim trim = aMeta.getTrim();
												TrimPattern pattern = trim.getPattern();

												items.set(name + ".Trim", trim.getMaterial().getKey().toString());
												items.set(name + ".Pattern", pattern.getKey().toString());
											}
										}

										if (iMeta.hasDisplayName()) {
											items.set(name + ".Name", iMeta.getDisplayName());
										}

										if (iMeta.hasEnchants()) {
											Map<Enchantment, Integer> enchants = iMeta.getEnchants();
											List<String> configEnchants = new ArrayList<String>();

											for (Map.Entry<Enchantment, Integer> e : enchants.entrySet()) {
												String enchantName = e.getKey().getName().toLowerCase();
												String level = e.getValue().toString();

												configEnchants.add(enchantName + "," + level);
											}

											items.set(name + ".Enchantments", configEnchants);
										}

										config.set("Items", items);
										core.getConfig().set("Items", items);
										core.saveConfig();

										p.sendMessage(ChatColor.GREEN + "Item added successfully");
									}
								}
							} else  {
								items.set(name + ".Item", item.getType().toString().toLowerCase());
								items.set(name + ".Amount", item.getAmount());
								items.set(name + ".Chance", chance);
								
								config.set("Items", items);
								core.getConfig().set("Items", items);
								core.saveConfig();

								p.sendMessage(ChatColor.GREEN + "Item added successfully");
							}
						}
					}
				}
			}
		} else {
			if (args[0].equalsIgnoreCase("spawn")) {
				ItemStack ball = new ItemStack(Material.DRAGON_EGG);
				ItemMeta bMeta = ball.getItemMeta();

				String sendTo = args[1];
				Player pTo = null;

				for (Player f : Bukkit.getOnlinePlayers()) {
					if (f.getName().equalsIgnoreCase(sendTo)) {
						pTo = f;
					}
				}

				if (pTo == null) {
					System.out.println("Could not find player " + sendTo);
					return true;
				}

				bMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("PBName")));

				List<String> lore = new ArrayList<String>();

				if (bMeta.getLore() == null) {
					lore.add(ChatColor.translateAlternateColorCodes('&', "&d/pb use"));
					lore.add(ChatColor.translateAlternateColorCodes('&', "&7/mccuse"));
				} else {
					lore.addAll(bMeta.getLore());
					lore.add(ChatColor.translateAlternateColorCodes('&', "&d/pb use"));
					lore.add(ChatColor.translateAlternateColorCodes('&', "&7/mccuse"));
				}

				bMeta.setLore(lore);
				ball.setItemMeta(bMeta);
				pTo.getInventory().addItem(ball);
			}
		}

		return true;
	}

	@EventHandler
	public void onPbPlace(BlockPlaceEvent e) {
		if (e.getBlock().getType().equals(Material.DRAGON_EGG)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void invClose(InventoryCloseEvent e) {
		if (ChatColor.stripColor(e.getView().getTitle()).equalsIgnoreCase("Prize Egg")) {
			Player p = (Player) e.getPlayer();
			ItemStack[] items = e.getInventory().getContents();

			for (ItemStack item : items) {
				if (item != null && p.getInventory().firstEmpty() != -1) {
					p.getInventory().addItem(item);
				} else if (item != null && p.getInventory().firstEmpty() == -1
						&& p.getInventory().contains(item.getType())) {
					for (ItemStack i : p.getInventory().getContents()) {
						if (i != null && i.getType().equals(item.getType())) {
							p.getInventory().addItem(item);
						}
					}
				} else if (item != null && p.getInventory().firstEmpty() == -1) {
					p.getWorld().dropItem(p.getLocation(), item);
				}
			}

			removePb(p);
		}
	}

	@EventHandler
	public void itemClick(InventoryClickEvent e) {
		if (ChatColor.stripColor(e.getView().getTitle()).equalsIgnoreCase("Prize Egg")) {
			e.setCancelled(true);
		}
	}

	public ItemStack getPotion(String type, String name, List<String> lore, String effect, int amount) {
		ItemStack item = new ItemStack(Material.getMaterial(type.toUpperCase()));

		PotionMeta potionMeta = (PotionMeta) item.getItemMeta();

		if (name != null) {
			potionMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		}

		List<String> itemLore = new ArrayList<>();

		for (int i = 0; i < lore.size(); i++) {
			itemLore.add(lore.get(i));
		}

		potionMeta.setBasePotionData(new PotionData(PotionType.valueOf(effect.toUpperCase())));

		item.setItemMeta(potionMeta);

		return item;
	}

	public ItemStack getItem(String type, String name, List<String> lore, List<String> enchants, int amount) {
		ItemStack item = new ItemStack(Material.getMaterial(type.toUpperCase()));
		ItemMeta itemMeta = item.getItemMeta();

		ArrayList<String> itemLore = new ArrayList<>();

		if (name != null) {
			itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		}

		for (String l : lore) {
			itemLore.add(ChatColor.translateAlternateColorCodes('&', l));
		}

		for (String k : enchants) {
			String[] list = k.split(",");

			String enchant = list[0];
			int level = Integer.parseInt(list[1]);

			itemMeta.addEnchant(Enchantment.getByName(enchant.toUpperCase()), level, true);
		}

		itemMeta.setLore(itemLore);
		item.setItemMeta(itemMeta);
		item.setAmount(amount);
		return item;
	}

	public ItemStack getArmorWithTrim(String type, String name, List<String> lore, List<String> enchants, String trim,
			String pattern, int amount) {
		ItemStack item = new ItemStack(Material.getMaterial(type.toUpperCase()));
		ArmorMeta itemMeta = (ArmorMeta) item.getItemMeta();

		ArrayList<String> itemLore = new ArrayList<>();

		if (name != null) {
			itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		}

		for (String l : lore) {
			itemLore.add(ChatColor.translateAlternateColorCodes('&', l));
		}

		ArmorTrim armorTrim = new ArmorTrim(getMaterialFromKey(trim), getPatternFromKey(pattern));

		itemMeta.setTrim(armorTrim);

		for (String k : enchants) {
			String[] list = k.split(",");

			String enchant = list[0];
			int level = Integer.parseInt(list[1]);

			itemMeta.addEnchant(Enchantment.getByName(enchant.toUpperCase()), level, true);
		}

		itemMeta.setLore(itemLore);
		item.setItemMeta(itemMeta);
		item.setAmount(amount);
		return item;
	}

	public ItemStack getVanityArmor(String displayName, String piece) {
		if (VanityGear.ARMOR.containsKey(ChatColor.stripColor(displayName.replace("Armor", "")).trim())) {
			Armor armor = VanityGear.ARMOR.get(ChatColor.stripColor(displayName.replace("Armor", "")).trim());

			return armor.getPiece(piece);
		}

		return null;
	}

	public ItemStack getVanityWeapon(String displayName) {
		if (VanityGear.WEAPON.containsKey(ChatColor.stripColor(displayName).trim())) {
			Weapon weap = VanityGear.WEAPON.get(ChatColor.stripColor(displayName).trim());

			return weap.getWeapon();
		}

		return null;
	}

	public void removePb(Player p) {
		PlayerInventory inv = p.getInventory();
		for (ItemStack i : inv.getContents()) {
			if (i != null) {
				if (i.getType().equals(Material.DRAGON_EGG)) {
					if (i.getAmount() == 1) {
						inv.remove(i);
					}

					int amount = i.getAmount() - 1;

					i.setAmount(amount);
					break;
				}
			}
		}
	}

	public boolean isLeather(String item) {
		ConfigurationSection itemSection = config.getConfigurationSection("Items." + item);
		if (itemSection.getString("Item").toLowerCase().contains("leather")) {
			return true;
		}

		return false;
	}

	public ItemStack createLeatherItem(String item) {
		ConfigurationSection itemSection = config.getConfigurationSection("Items." + item);

		String itemString = itemSection.getString("Item");
		String colorString = itemSection.getString("Color");
		int amount = itemSection.getInt("Amount");

		ItemStack leatherArmor = new ItemStack(Material.getMaterial(itemString.toUpperCase()), amount);
		LeatherArmorMeta armorMeta = (LeatherArmorMeta) leatherArmor.getItemMeta();

		List<String> loreList = new ArrayList<String>();

		if (itemSection.getStringList("Lore") != null) {
			List<String> lore = itemSection.getStringList("Lore");

			for (String l : lore) {
				loreList.add(ChatColor.translateAlternateColorCodes('&', l));
			}

			armorMeta.setLore(loreList);
		}

		Colors color = new Colors(colorString);

		armorMeta.setColor(color.getColor());

		armorMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemSection.getString("Name")));

		leatherArmor.setItemMeta(armorMeta);

		return leatherArmor;
	}

	private boolean isArmor(String blockType) {
		boolean result = false;

		if (blockType.toLowerCase().contains("helmet") || blockType.toLowerCase().contains("chestplate")
				|| blockType.toLowerCase().contains("leggings") || blockType.toLowerCase().contains("boots")) {
			result = true;
		}

		return result;
	}

	private TrimMaterial getMaterialFromKey(String key) {
		TrimMaterial material = null;

		if (TrimMaterial.AMETHYST.getKey().toString().equalsIgnoreCase(key)) {
			material = TrimMaterial.AMETHYST;
		}

		if (TrimMaterial.COPPER.getKey().toString().equalsIgnoreCase(key)) {
			material = TrimMaterial.COPPER;
		}

		if (TrimMaterial.DIAMOND.getKey().toString().equalsIgnoreCase(key)) {
			material = TrimMaterial.DIAMOND;
		}

		if (TrimMaterial.EMERALD.getKey().toString().equalsIgnoreCase(key)) {
			material = TrimMaterial.EMERALD;
		}

		if (TrimMaterial.GOLD.getKey().toString().equalsIgnoreCase(key)) {
			material = TrimMaterial.GOLD;
		}

		if (TrimMaterial.IRON.getKey().toString().equalsIgnoreCase(key)) {
			material = TrimMaterial.IRON;
		}

		if (TrimMaterial.LAPIS.getKey().toString().equalsIgnoreCase(key)) {
			material = TrimMaterial.LAPIS;
		}

		if (TrimMaterial.NETHERITE.getKey().toString().equalsIgnoreCase(key)) {
			material = TrimMaterial.NETHERITE;
		}

		if (TrimMaterial.QUARTZ.getKey().toString().equalsIgnoreCase(key)) {
			material = TrimMaterial.QUARTZ;
		}

		if (TrimMaterial.REDSTONE.getKey().toString().equalsIgnoreCase(key)) {
			material = TrimMaterial.REDSTONE;
		}

		return material;
	}

	private TrimPattern getPatternFromKey(String key) {
		TrimPattern pattern = null;

		if (TrimPattern.COAST.getKey().toString().equalsIgnoreCase(key)) {
			pattern = TrimPattern.COAST;
		}

		if (TrimPattern.DUNE.getKey().toString().equalsIgnoreCase(key)) {
			pattern = TrimPattern.DUNE;
		}

		if (TrimPattern.EYE.getKey().toString().equalsIgnoreCase(key)) {
			pattern = TrimPattern.EYE;
		}

		if (TrimPattern.HOST.getKey().toString().equalsIgnoreCase(key)) {
			pattern = TrimPattern.HOST;
		}

		if (TrimPattern.RAISER.getKey().toString().equalsIgnoreCase(key)) {
			pattern = TrimPattern.RAISER;
		}

		if (TrimPattern.RIB.getKey().toString().equalsIgnoreCase(key)) {
			pattern = TrimPattern.RIB;
		}

		if (TrimPattern.SENTRY.getKey().toString().equalsIgnoreCase(key)) {
			pattern = TrimPattern.SENTRY;
		}

		if (TrimPattern.SHAPER.getKey().toString().equalsIgnoreCase(key)) {
			pattern = TrimPattern.SHAPER;
		}

		if (TrimPattern.SILENCE.getKey().toString().equalsIgnoreCase(key)) {
			pattern = TrimPattern.SILENCE;
		}

		if (TrimPattern.SNOUT.getKey().toString().equalsIgnoreCase(key)) {
			pattern = TrimPattern.SNOUT;
		}

		if (TrimPattern.SPIRE.getKey().toString().equalsIgnoreCase(key)) {
			pattern = TrimPattern.SPIRE;
		}

		if (TrimPattern.TIDE.getKey().toString().equalsIgnoreCase(key)) {
			pattern = TrimPattern.TIDE;
		}

		if (TrimPattern.VEX.getKey().toString().equalsIgnoreCase(key)) {
			pattern = TrimPattern.VEX;
		}

		if (TrimPattern.WARD.getKey().toString().equalsIgnoreCase(key)) {
			pattern = TrimPattern.WARD;
		}

		if (TrimPattern.WAYFINDER.getKey().toString().equalsIgnoreCase(key)) {
			pattern = TrimPattern.WAYFINDER;
		}

		if (TrimPattern.WILD.getKey().toString().equalsIgnoreCase(key)) {
			pattern = TrimPattern.WILD;
		}

		return pattern;
	}
}
