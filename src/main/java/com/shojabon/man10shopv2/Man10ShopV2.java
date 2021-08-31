package com.shojabon.man10shopv2;

import com.shojabon.man10shopv2.Commands.Man10ShopV2Command;
import com.shojabon.man10shopv2.Listeners.SignListeners;
import com.shojabon.man10shopv2.Utils.MySQL.ThreadedMySQLAPI;
import com.shojabon.man10shopv2.Utils.SCommandRouter.SCommandArgument;
import com.shojabon.man10shopv2.Utils.SCommandRouter.SCommandObject;
import com.shojabon.man10shopv2.Utils.SCommandRouter.SCommandRouter;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.VaultAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class Man10ShopV2 extends JavaPlugin {

    public static ThreadedMySQLAPI mysql;
    public Man10ShopV2API api;
    public static String prefix;
    public static VaultAPI vault;
    public static FileConfiguration config;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        mysql = new ThreadedMySQLAPI(this);
        this.api = new Man10ShopV2API(this);
        prefix = getConfig().getString("prefix");
        vault = new VaultAPI();
        config = getConfig();
        getServer().getPluginManager().registerEvents(new SignListeners(this), this);


        Man10ShopV2Command command = new Man10ShopV2Command(this);
        getCommand("mshop").setExecutor(command);
        getCommand("mshop").setTabCompleter(command);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for(UUID uuid: SInventory.playersInInventoryGlobal){
            Player p = getServer().getPlayer(uuid);
            if(p != null) p.closeInventory();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
//        Man10Shop shop = this.api.getShop("6d524add-d0f9-4c46-af1e-1307e2bab7f4");
//        //shop.addModerator(((Player)sender), Man10ShopPermission.OWNER);
          Player p = ((Player) sender);
//
//        shop.setTargetItem(p, new SItemStack(Material.DIAMOND).setAmount(2).build());
//        shop.performAction(p, 10);


        return false;
    }
}
