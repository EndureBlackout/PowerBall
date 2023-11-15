package me.endureblackout.PowerBall;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class PowerBall extends JavaPlugin {

	public void onEnable() {
		if (!getDataFolder().exists()) {
			getConfig().options().copyDefaults(true);
			saveConfig();
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
