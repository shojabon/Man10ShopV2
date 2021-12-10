package com.shojabon.man10shopv2.Commands.SubCommands;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.EditableShopSelectorMenu;
import com.shojabon.man10shopv2.Menus.ShopMainMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class OpenShopCommand implements CommandExecutor {
    Man10ShopV2 plugin;

    public OpenShopCommand(Man10ShopV2 plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!Man10ShopV2.config.getBoolean("pluginEnabled")){
            sender.sendMessage(Man10ShopV2.prefix + "§c§l現在このプラグインは停止中です");
            return false;
        }
        if(!sender.hasPermission("man10shopv2.admin")){
            sender.sendMessage(Man10ShopV2.prefix + "§c§lあなたには権限がありません");
            return false;
        }

        Player p = Bukkit.getPlayer(args[1]);
        if(p == null || !p.isOnline()){
            sender.sendMessage(Man10ShopV2.prefix + "§c§lプレイヤーが存在しません");
            return false;
        }
        try{
            UUID shopId = UUID.fromString(args[2]);
            Man10Shop shop = Man10ShopV2.api.getShop(shopId);
            if(shop == null){
                sender.sendMessage(Man10ShopV2.prefix + "§c§lショップが存在しません");
                return false;
            }
            p.closeInventory();
            shop.getActionMenu(p).open(p);
        }catch (Exception e){
            sender.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
            return false;
        }
        return true;
    }
}
