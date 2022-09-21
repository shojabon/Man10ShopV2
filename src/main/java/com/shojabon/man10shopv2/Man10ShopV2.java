package com.shojabon.man10shopv2;

import com.shojabon.man10shopv2.commands.Man10ShopV2Command;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.listeners.SignListeners;
import com.shojabon.mcutils.Utils.MySQL.ThreadedMySQLAPI;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.VaultAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Man10ShopV2 extends JavaPlugin {

    public static ThreadedMySQLAPI mysql;
    public static ExecutorService threadPool = Executors.newCachedThreadPool();
    public static Man10ShopV2API api;
    public static String prefix;
    public static String gachaPrefix = "§e§l[§6§lMan10Gacha§e§l]";
    public static VaultAPI vault;
    public static FileConfiguration config;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        mysql = new ThreadedMySQLAPI(this);
        config = getConfig();
        prefix = getConfig().getString("prefix");
        vault = new VaultAPI();
        getServer().getPluginManager().registerEvents(new SignListeners(this), this);
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
