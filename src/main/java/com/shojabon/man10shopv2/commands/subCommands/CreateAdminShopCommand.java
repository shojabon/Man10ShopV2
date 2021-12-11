package com.shojabon.man10shopv2.commands.subCommands;

import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.mcutils.Utils.SItemStack;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CreateAdminShopCommand implements CommandExecutor {
    Man10ShopV2 plugin;

    public CreateAdminShopCommand(Man10ShopV2 plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!Man10ShopV2.config.getBoolean("pluginEnabled")){
            sender.sendMessage(Man10ShopV2.prefix + "§c§l現在このプラグインは停止中です");
            return false;
        }
        if(! (sender instanceof Player)){
            sender.sendMessage(Man10ShopV2.prefix + "§c§lこのコマンドはプレイヤーのみが実行可能です");
            return false;
        }
        UUID shopId = plugin.api.createShop(((Player) sender), args[1], 1000, new SItemStack(Material.DIAMOND), Man10ShopType.BUY, true);
        if(shopId == null){
            sender.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
            return false;
        }
        sender.sendMessage(Man10ShopV2.prefix + "§a§l管理者ショップを作りました");
        return true;
    }
}
