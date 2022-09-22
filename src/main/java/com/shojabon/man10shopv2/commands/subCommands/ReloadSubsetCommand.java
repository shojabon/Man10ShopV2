package com.shojabon.man10shopv2.commands.subCommands;

import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.mcutils.Utils.MySQL.ThreadedMySQLAPI;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadSubsetCommand implements CommandExecutor {
    Man10ShopV2 plugin;

    public ReloadSubsetCommand(Man10ShopV2 plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        SInventory.closeAllSInventories();

        Man10ShopV2.api.clearSubset();

        sender.sendMessage(Man10ShopV2.prefix + "§a§lサブセットがリロードされました");
        return true;
    }
}
