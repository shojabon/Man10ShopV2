package com.shojabon.man10shopv2.Commands.SubCommands;

import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Utils.MySQL.ThreadedMySQLAPI;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

public class ReloadConfigCommand implements CommandExecutor {
    Man10ShopV2 plugin;

    public ReloadConfigCommand(Man10ShopV2 plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        plugin.reloadConfig();
        Man10ShopV2.config = plugin.getConfig();

        SInventory.closeAllSInventories();

        plugin.api.clearCache();

        Man10ShopV2.mysql = new ThreadedMySQLAPI(plugin);
        Man10ShopV2.prefix = Man10ShopV2.config.getString("prefix");

        sender.sendMessage(Man10ShopV2.prefix + "§a§lプラグインがリロードされました");
        return true;
    }
}
