
package me.endureblackout.PowerBall;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
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
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import de.tr7zw.nbtapi.NBTItem;
import io.lumine.mythic.bukkit.adapters.BukkitItemStack;
import io.lumine.mythic.core.items.MythicItem;
import me.endureblackout.VanityGear.VanityGear;
import me.endureblackout.VanityGear.Helpers.Armor;
import me.endureblackout.VanityGear.Helpers.Weapon;
import su.nightexpress.excellentcrates.key.CrateKey;

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
							Firework firework = (Firework) p.getLocation().getWorld().spawnEntity(p.getLocation(),
									EntityType.FIREWORK);
							FireworkMeta fMeta = firework.getFireworkMeta();

							Builder fireworkEffect = FireworkEffect.builder().with(Type.BURST).withColor(Color.RED)
									.withColor(Color.BLUE).withColor(Color.WHITE).withColor(Color.PURPLE)
									.withColor(Color.YELLOW).flicker(true);
							
							fMeta.addEffect(fireworkEffect.build());
							fMeta.setPower(0);
							fMeta.setDisplayName("powerball firework");
							
							firework.setFireworkMeta(fMeta);
							firework.setGlowing(true);
							firework.detonate();							

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
									} else if (item.isSet("KeyId")) {
										CrateKey key = PowerBall.CRATES_API.getKeyManager()
												.getKeyById(item.getString("KeyId"));

										ItemStack keyItem = key.getItem();
										keyItem.setAmount(item.getInt("Amount"));

										pbMenu.setItem(pbMenu.firstEmpty(), keyItem);
									} else if (item.isSet("MythicName")) {
										Collection<MythicItem> mItems = PowerBall.MM.getItemManager().getItems();

										for (MythicItem mItem : mItems) {
											if (mItem.getInternalName()
													.equalsIgnoreCase(item.getString("MythicName"))) {
												BukkitItemStack bItem = (BukkitItemStack) mItem
														.generateItemStack(item.getInt("Amount"));
												ItemStack pbItem = bItem.build();

												pbMenu.setItem(pbMenu.firstEmpty(), pbItem);
											}
										}

									} else if (item.getString("Item").toLowerCase().contains("potion")) {
										pbMenu.setItem(pbMenu.firstEmpty(),
												getPotion(item.getString("Item"), item.getString("Name"),
														item.getStringList("Lore"), item.getString("Effect"),
														item.getInt("Amount")));
									} else {
										pbMenu.setItem(pbMenu.firstEmpty(),
												getItem(item.getString("Item"), item.getString("Name"),
														item.getStringList("Lore"), item.getStringList("Enchantments"),
														item.getInt("Amount"),
														item.isSet("Voucher") ? item.getString("Voucher") : null));
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
							if (PowerBall.MM.getItemManager().isMythicItem(item)) {
								String mItemName = PowerBall.MM.getItemManager().getMythicTypeFromItem(item);

								if (mItemName == null) {
									sender.sendMessage(ChatColor.RED + "Mythic Item not found.");

									return true;
								}

								items.set(name + ".MythicName", mItemName);
								items.set(name + ".Amount", item.getAmount());
								items.set(name + ".Chance", chance);

								config.set("Items", items);

								core.getConfig().set("Items", items);
								core.saveConfig();

								sender.sendMessage(ChatColor.GREEN + "MythicMobs item added successfully.");

								return true;
							}

							if (PowerBall.CRATES_API.getKeyManager().isKey(item)) {
								CrateKey actualKey = PowerBall.CRATES_API.getKeyManager().getKeyByItem(item);
								Map<String, CrateKey> keys = PowerBall.CRATES_API.getKeyManager().getKeysMap();
								String keyId = null;

								for (Entry<String, CrateKey> key : keys.entrySet()) {
									if (key.getValue().getItem().equals(actualKey.getItem())) {
										keyId = key.getKey();
									}
								}

								if (keyId == null) {
									sender.sendMessage(ChatColor.RED + "Key not found.");

									return true;
								}

								items.set(name + ".KeyId", keyId);
								items.set(name + ".Amount", item.getAmount());
								items.set(name + ".Chance", chance);

								config.set("Items", items);

								core.getConfig().set("Items", items);
								core.saveConfig();

								sender.sendMessage(ChatColor.GREEN + "Key added successfully.");

								return true;
							}

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
											items.set(name + ".Piece", Material.LEATHER_HELMET.toString());
											break;
										case LEATHER_CHESTPLATE:
											items.set(name + ".Piece", Material.LEATHER_CHESTPLATE.toString());
											break;
										case LEATHER_LEGGINGS:
											items.set(name + ".Piece", Material.LEATHER_LEGGINGS.toString());
											break;
										case LEATHER_BOOTS:
											items.set(name + ".Piece", Material.LEATHER_BOOTS.toString());
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

										NBTItem nbtItem = new NBTItem(item);

										if (nbtItem.hasTag("voucher")) {
											items.set(name + ".Voucher", nbtItem.getString("voucher"));
										}

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
							} else {
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

					if (args[0].equalsIgnoreCase("spawn")) {
						ItemStack ball = new ItemStack(Material.DRAGON_EGG);
						ItemMeta bMeta = ball.getItemMeta();
		
						String sendTo = args[1];
						int amount = args[2] != null ? Integer.parseInt(args[2]) : 1;
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
						ball.setAmount(amount);
		
						pTo.getInventory().addItem(ball);
					}
				}
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

	public ItemStack getItem(String type, String name, List<String> lore, List<String> enchants, int amount,
			String voucher) {
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

		if (voucher != null) {
			NBTItem nbtItem = new NBTItem(item);
			nbtItem.setString("voucher", voucher);

			item = nbtItem.getItem();
		}

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

			return armor.getPiece(Material.getMaterial(piece.toUpperCase()), false);
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
		int eggSlot = p.getInventory().first(Material.DRAGON_EGG);

		if(inv.getItem(eggSlot).getAmount() == 1) {
			inv.setItem(eggSlot, null);
		} else {
			inv.getItem(eggSlot).setAmount(inv.getItem(eggSlot).getAmount() - 1);
		}
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
