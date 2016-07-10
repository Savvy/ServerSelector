package com.timberdnd.ss;

import com.timberdnd.ss.listeners.EventListeners;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerSelector extends JavaPlugin {
    
    public static Plugin plugin;
    public void onEnable() {
	plugin = this;
	saveDefaultConfig();
	if(!(this.getConfig().getBoolean("enabled"))) {
	    setEnabled(false);
	    return;
	}
	Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
	this.getServer().getPluginManager().registerEvents(new EventListeners(), this);
    }
}
