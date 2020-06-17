
package me.endureblackout.PowerBall;

import java.util.ArrayList;
import java.util.List;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CommandHandler implements CommandExecutor, Listener {

	PowerBall			core;
	YamlConfiguration	config;

	public CommandHandler(PowerBall instance, YamlConfiguration config) {
		this.core = instance;
		this.config = config;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			
			if (cmd.getName().equalsIgnoreCase("mc")) {
				if (args.length == 1) {
					if (args[0].equalsIgnoreCase("spawn") && p.hasPermission("powerball.spawn")) {
						ItemStack ball = new ItemStack(Material.DRAGON_EGG);
						ItemMeta bMeta = ball.getItemMeta();

						bMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("PBName")));

						List<String> lore = new ArrayList<String>();
						
						if (bMeta.getLore() == null) {
							lore.add(ChatColor.translateAlternateColorCodes('&', "&d/mc use"));
							lore.add(ChatColor.translateAlternateColorCodes('&', "&7&k/mccuse"));
						} else {
							lore.addAll(bMeta.getLore());
							lore.add(ChatColor.translateAlternateColorCodes('&', "&d/mc use"));
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

								double chance = Math.random();
								if (chance <= itemSec.getConfigurationSection(k).getDouble("Chance")) {
									if(isLeather(item.getName())) {
										pbMenu.setItem(pbMenu.firstEmpty(), createLeatherItem(item.getName()));
									} else if (item.getString("Item").equalsIgnoreCase("potion")) {
										pbMenu.setItem(pbMenu.firstEmpty(), getPotion(item.getString("Item"), item.getString("Name"), item.getStringList("Lore"), item.getStringList("Effects"), item.getInt("Amount")));
									} else {
										pbMenu.setItem(pbMenu.firstEmpty(), getItem(item.getString("Item"), item.getString("Name"), item.getStringList("Lore"), item.getStringList("Enchantments"), item.getInt("Amount")));
									}
								}
							}

							p.openInventory(pbMenu);
						}
					}
				} else if (args.length == 2) {
					if(args[0].equalsIgnoreCase("spawn") && p.hasPermission("powerball.spawn")) {
						ItemStack ball = new ItemStack(Material.DRAGON_EGG);
						ItemMeta bMeta = ball.getItemMeta();
						
						String sendTo = args[1];
						Player pTo = null;
						
						for(Player f : Bukkit.getOnlinePlayers()) {
							if (f.getName().equalsIgnoreCase(sendTo)) {
								pTo = f;
							}
						}
						
						if(pTo == null) {
							p.sendMessage(ChatColor.DARK_RED + "This Player is offline so the item could not be added to their inventory.");
							return true;
						}

						bMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("PBName")));

						List<String> lore = new ArrayList<String>();
						
						if (bMeta.getLore() == null) {
							lore.add(ChatColor.translateAlternateColorCodes('&', "&d/mc use"));
							lore.add(ChatColor.translateAlternateColorCodes('&', "&7/mccuse"));
						} else {
							lore.addAll(bMeta.getLore());
							lore.add(ChatColor.translateAlternateColorCodes('&', "&d/mc use"));
							lore.add(ChatColor.translateAlternateColorCodes('&', "&7/mccuse"));
						}

						bMeta.setLore(lore);
						ball.setItemMeta(bMeta);
						pTo.getInventory().addItem(ball);
					}
				}
			}
		} else {
			if(args[0].equalsIgnoreCase("spawn")) {
				ItemStack ball = new ItemStack(Material.DRAGON_EGG);
				ItemMeta bMeta = ball.getItemMeta();
				
				String sendTo = args[1];
				Player pTo = null;
				
				for(Player f : Bukkit.getOnlinePlayers()) {
					if (f.getName().equalsIgnoreCase(sendTo)) {
						pTo = f;
					}
				}
				
				if(pTo == null) {
					System.out.println("Could not find player " + sendTo);
					return true;
				}

				bMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("PBName")));

				List<String> lore = new ArrayList<String>();
				
				if (bMeta.getLore() == null) {
					lore.add(ChatColor.translateAlternateColorCodes('&', "&d/mc use"));
					lore.add(ChatColor.translateAlternateColorCodes('&', "&7/mccuse"));
				} else {
					lore.addAll(bMeta.getLore());
					lore.add(ChatColor.translateAlternateColorCodes('&', "&d/mc use"));
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
				if (item != null) {
					p.getInventory().addItem(item);
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

	public ItemStack getPotion(String type, String name, List<String> lore, List<String> effects, int amount) {
		ItemStack item = new ItemStack(Material.getMaterial(type.toUpperCase()));

		PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
		
		if(name != null) {
			potionMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		}

		List<String> itemLore = new ArrayList<>();

		for (int i = 0; i < lore.size(); i++) {
			itemLore.add(lore.get(i));
		}

		for (String e : effects) {
			potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(e.toUpperCase()), 25 * 20, 1), true);
		}

		item.setItemMeta(potionMeta);
		
		return item;
	}

	public ItemStack getItem(String type, String name, List<String> lore, List<String> enchants, int amount) {
		System.out.println(type);
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
	 
	public boolean isLeather (String item) {
		ConfigurationSection itemSection = config.getConfigurationSection("Items." + item);
		if(itemSection.getString("Item").toLowerCase().contains("leather")) {
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
		
		if(itemSection.getStringList("Lore") != null) {
			List<String> lore = itemSection.getStringList("Lore");
			
			for(String  l : lore) {
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
}
