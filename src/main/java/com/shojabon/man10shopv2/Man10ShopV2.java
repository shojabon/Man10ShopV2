package com.shojabon.man10shopv2;

import com.shojabon.man10shopv2.Commands.Man10ShopV2Command;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Listeners.SignListeners;
import com.shojabon.mcutils.Utils.MySQL.ThreadedMySQLAPI;
import com.shojabon.mcutils.Utils.SCommandRouter.SCommandArgument;
import com.shojabon.mcutils.Utils.SCommandRouter.SCommandObject;
import com.shojabon.mcutils.Utils.SCommandRouter.SCommandRouter;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.VaultAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Man10ShopV2 extends JavaPlugin {

    public static ThreadedMySQLAPI mysql;
    public static ExecutorService threadPool = Executors.newCachedThreadPool();
    public static Man10ShopV2API api;
    public static String prefix;
    public static VaultAPI vault;
    public static FileConfiguration config;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        mysql = new ThreadedMySQLAPI(this);
        config = getConfig();
        Man10ShopV2.api = new Man10ShopV2API(this);
        prefix = getConfig().getString("prefix");
        vault = new VaultAPI();
        getServer().getPluginManager().registerEvents(new SignListeners(this), this);


        Man10ShopV2Command command = new Man10ShopV2Command(this);
        getCommand("mshop").setExecutor(command);
        getCommand("mshop").setTabCompleter(command);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Man10ShopV2.api.stopTransactionThread();
        SInventory.closeAllSInventories();
    }

}
