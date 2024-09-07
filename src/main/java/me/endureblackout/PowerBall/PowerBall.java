package me.endureblackout.PowerBall;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import io.lumine.mythic.api.MythicPlugin;
import io.lumine.mythic.api.MythicProvider;
import su.nightexpress.excellentcrates.CratesAPI;
import su.nightexpress.excellentcrates.CratesPlugin;

public class PowerBall extends JavaPlugin {
	public static CratesPlugin CRATES_API;
	public static MythicPlugin MM;

	public void onEnable() {
		if (!getDataFolder().exists()) {
			getConfig().options().copyDefaults(true);
			saveConfig();
		}
		
		CRATES_API = CratesAPI.PLUGIN;
		
		try {
			MM = MythicProvider.get();
		} catch (Exception e) {
			Bukkit.getLogger().severe("MythicMobs was not able to be loaded");
		}

		YamlConfiguration config = (YamlConfiguration) getConfig();

		Bukkit.getPluginManager().registerEvents(new CommandHandler(this, config), this);

		getCommand("pb").setExecutor(new CommandHandler(this, config));

		if (Bukkit.getPluginManager().getPlugin("VanityGear") == null) {
			Bukkit.getLogger().log(Level.SEVERE, getName());
			onDisable();
		}
	}
}
