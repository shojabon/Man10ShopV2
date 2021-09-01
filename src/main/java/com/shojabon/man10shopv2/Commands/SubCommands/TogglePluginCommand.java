package com.shojabon.man10shopv2.Commands.SubCommands;

import com.shojabon.man10shopv2.Man10ShopV2;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TogglePluginCommand implements CommandExecutor {
    Man10ShopV2 plugin;

    public TogglePluginCommand(Man10ShopV2 plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1){
            if(Man10ShopV2.config.getBoolean("pluginEnabled")){
                sender.sendMessage(Man10ShopV2.prefix + "§a§lプラグインは現在有効です");
            }else{
                sender.sendMessage(Man10ShopV2.prefix + "§c§lプラグインは現在無効です");
            }
            return true;
        }else if(args.length == 2){
            boolean current = Boolean.parseBoolean(args[1]);
            if(!current){
                sender.sendMessage(Man10ShopV2.prefix + "§c§lプラグインは現在無効化されました");
            }else{
                sender.sendMessage(Man10ShopV2.prefix + "§a§lプラグインは現在有効化されました");
            }
            plugin.getConfig().set("pluginEnabled", current);
            plugin.saveConfig();
            Man10ShopV2.config = plugin.getConfig();
        }
        return true;
    }
}
