package com.shojabon.man10shopv2;

import com.shojabon.man10shopv2.commands.Man10ShopV2Command;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.listeners.SignListeners;
import com.shojabon.mcutils.Utils.MySQL.ThreadedMySQLAPI;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.VaultAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Man10ShopV2 extends JavaPlugin implements @NotNull Listener {

    public static ThreadedMySQLAPI mysql;
    public static ExecutorService threadPool = Executors.newCachedThreadPool();
    public static Man10ShopV2API api;
    public static String prefix;
    public static String gachaPrefix = "§e§l[§6§lMan10Gacha§e§l]";
    public static VaultAPI vault;
    public static FileConfiguration config;

    public static ArrayList<UUID> ved = new ArrayList<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        for(UUID uuid: ved){
            Player vp = Bukkit.getPlayer(uuid);
            if(vp == null){
                ved.remove(uuid);
                continue;
            }
            e.getPlayer().hidePlayer(this, vp);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        if(ved.contains(e.getPlayer().getUniqueId())) e.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
        ved.remove(e.getPlayer().getUniqueId());
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        mysql = new ThreadedMySQLAPI(this);
        config = getConfig();
        prefix = getConfig().getString("prefix");
        vault = new VaultAPI();
        getServer().getPluginManager().registerEvents(new SignListeners(this), this);
        getServer().getPluginManager().registerEvents(this, this);
        Man10ShopV2.api = new Man10ShopV2API(this);


        Man10ShopV2Command command = new Man10ShopV2Command(this);
        getCommand("mshop").setExecutor(command);
        getCommand("mshop").setTabCompleter(command);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Man10ShopV2.api.stopTransactionThread();
        for(Man10Shop shop : Man10ShopV2API.shopCache.values()){
            shop.executeUnload();
        }
        SInventory.closeAllSInventories();
    }

}
