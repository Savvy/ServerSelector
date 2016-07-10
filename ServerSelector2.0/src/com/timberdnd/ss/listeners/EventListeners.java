package com.timberdnd.ss.listeners;


import com.timberdnd.ss.ServerSelector;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EventListeners implements Listener {
    FileConfiguration fc = ServerSelector.plugin.getConfig();
    protected Inventory inv = Bukkit.createInventory(
	    null, 54, ChatColor.translateAlternateColorCodes('&', fc.getString("inventory.name")));

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
	int slot = fc.getInt("selector.slot");
	ItemStack item = new ItemStack(Material.valueOf(fc.getString("selector.item")));
	ItemMeta meta = item.getItemMeta();
	item.setAmount(fc.getInt("selector.amount"));
	meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', fc.getString("selector.name")));
	ArrayList<String> arrayList = new ArrayList<String>();
	fc.getStringList("selector.lore").stream().forEach(s -> {
	    arrayList.add(ChatColor.translateAlternateColorCodes('&', s));
	});
	meta.setLore(arrayList);
	item.setItemMeta(meta);
	event.getPlayer().getInventory().setItem(slot, item);
	event.getPlayer().updateInventory();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
	if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
	    if(event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(
		    ChatColor.translateAlternateColorCodes('&', fc.getString("selector.name")))) {
		event.setCancelled(true);
		setupInventory(event.getPlayer());
	    }
	}
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
	if(event.getInventory().getTitle().equalsIgnoreCase(
		ChatColor.translateAlternateColorCodes('&', fc.getString("inventory.name")))) {
	    event.setCancelled(true);
	    if(event.getCurrentItem() == null || 
		    event.getCurrentItem().getType() == null || event.getCurrentItem().getType() == Material.STAINED_GLASS_PANE
		    || event.getCurrentItem().getType() == Material.AIR) {
		return;
	    }
	    for(String s: fc.getConfigurationSection("inventory.items").getKeys(false)) {
		s = "inventory.items." + s;
		ItemStack item = new ItemStack(Material.valueOf(fc.getString(s + ".item").toUpperCase()));
		ItemMeta meta = item.getItemMeta();
		item.setAmount(fc.getInt(s + ".amount"));
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', fc.getString(s + ".name")));
		ArrayList<String> arrayList = new ArrayList<String>();
		fc.getStringList(s + ".lore").stream().forEach(strings -> {
		    arrayList.add(ChatColor.translateAlternateColorCodes('&', strings));
		});
		meta.setLore(arrayList);
		item.setItemMeta(meta);
		if(isSimilar(event.getCurrentItem(), item)) {
		    Random r = new Random();
		    int random = r.nextInt(fc.getStringList(s + ".bungee_servers").size());
		    sendPlayerToServer((Player) event.getWhoClicked(),
			    fc.getStringList(s + ".bungee_servers").get(random));
		}
	    }
	}
    }


    private void setupInventory(Player player) {
	for(String s: fc.getConfigurationSection("inventory.items").getKeys(false)) {
	    s = "inventory.items." + s;
	    ItemStack item = new ItemStack(Material.valueOf(fc.getString(s + ".item").toUpperCase()));
	    int slots = fc.getInt(s + ".slot");
	    ItemMeta meta = item.getItemMeta();
	    item.setAmount(fc.getInt(s + ".amount"));
	    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', fc.getString(s + ".name")));
	    ArrayList<String> arrayList = new ArrayList<String>();
	    fc.getStringList(s + ".lore").stream().forEach(strings -> {
		arrayList.add(ChatColor.translateAlternateColorCodes('&', strings));
	    });
	    meta.setLore(arrayList);
	    item.setItemMeta(meta);
	    inv.setItem(slots, item);
	}
	ArrayList<Integer> glass = (ArrayList<Integer>) fc.getIntegerList("inventory.glassPanes.slots");
	ItemStack glassItem = new ItemStack(Material.STAINED_GLASS_PANE);
	ItemMeta meta = glassItem.getItemMeta();
	meta.setDisplayName(" ");
	glassItem.setDurability(Short.valueOf(fc.get("inventory.glassPanes.color").toString()));
	glassItem.setItemMeta(meta);
	for(int i: glass) {
	    inv.setItem(i, glassItem);
	}
	player.closeInventory();
	player.openInventory(inv);
    }

    private void sendPlayerToServer(Player player, String serverName) {
	ByteArrayOutputStream b = new ByteArrayOutputStream();
	DataOutputStream out = new DataOutputStream(b);
	try{
	    out.writeUTF("Connect");
	    out.writeUTF(serverName);
	    player.sendMessage(ChatColor.translateAlternateColorCodes('&', fc.getString("player_connected")
		    .replace("%server%", serverName)));
	    player.sendPluginMessage(ServerSelector.plugin, "BungeeCord", b.toByteArray());
	}catch (IOException localIOException) {
	    System.out.println("There was a problem connecting player (" + player.getName() + ") to server (" + serverName + ").");
	    System.out.println("If this problem persists please contact @Nix.");
	}
    }

    private boolean isSimilar(ItemStack item1, ItemStack item2) {
	if(item1.hasItemMeta() && item2.hasItemMeta()) {
	    if(item1.getType() == item2.getType()
		    && item1.getItemMeta().getDisplayName().equalsIgnoreCase(item2.getItemMeta().getDisplayName())) {
		return true;
	    }
	}
	return false;
    }
}