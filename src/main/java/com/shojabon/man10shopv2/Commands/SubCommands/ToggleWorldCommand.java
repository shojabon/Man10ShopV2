package com.shojabon.man10shopv2.Commands.SubCommands;

import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.Shop.EditableShopSelectorMenu;
import com.shojabon.man10shopv2.Menus.Shop.ShopMainMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ToggleWorldCommand implements CommandExecutor {
    Man10ShopV2 plugin;

    public ToggleWorldCommand(Man10ShopV2 plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1){
            sender.sendMessage("§l========[有効化されているワールド]========");
            sender.sendMessage("");
            for(String world: Man10ShopV2.config.getStringList("enabledWorlds")){
                sender.sendMessage(world);
            }
            sender.sendMessage("");
            sender.sendMessage("§l================================");
            return true;
        }else if(args.length == 2){
            List<String> worlds = Man10ShopV2.config.getStringList("enabledWorlds");
            if(worlds.contains(args[1])){
                worlds.remove(args[1]);
                sender.sendMessage(Man10ShopV2.prefix + "§c§l" + args[1] + "を削除しました");
            }else{
                worlds.add(args[1]);
                sender.sendMessage(Man10ShopV2.prefix + "§a§l" + args[1] + "を追加しました");
            }
            plugin.getConfig().set("enabledWorlds", worlds);
            plugin.saveConfig();
            Man10ShopV2.config = plugin.getConfig();
        }
        return true;
    }
}
