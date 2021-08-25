package com.shojabon.man10shopv2;

import com.shojabon.man10shopv2.Commands.Man10ShopV2Command;
import com.shojabon.man10shopv2.Menus.LargeSInventoryMenu;
import com.shojabon.man10shopv2.Utils.MySQL.MySQLQueue;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.VaultAPI;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import red.man10.man10bank.BankAPI;

import java.util.ArrayList;

public final class Man10ShopV2 extends JavaPlugin {

    public static MySQLQueue mysql;
    public Man10ShopV2API api;
    public static String prefix;
    public static BankAPI bank;
    public static VaultAPI vault;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        mysql = new MySQLQueue(1, 1, this);
        this.api = new Man10ShopV2API(this);
        prefix = getConfig().getString("prefix");
        bank = new BankAPI(this);
        vault = new VaultAPI();
        getCommand("man10shopv2").setExecutor(new Man10ShopV2Command(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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
