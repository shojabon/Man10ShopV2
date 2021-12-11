package com.shojabon.man10shopv2.commands.subCommands;

import ToolMenu.ConfirmationMenu;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SItemStack;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CreateShopCommand implements CommandExecutor {
    Man10ShopV2 plugin;

    public CreateShopCommand(Man10ShopV2 plugin){
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
        if(args[1].length() > 64){
            sender.sendMessage(Man10ShopV2.prefix + "§c§lショップ名は64文字以内でなければいけません");
            return false;
        }
        Player p = ((Player)sender);
        int shopPrice = Man10ShopV2.config.getInt("shop.creationPrice");
        if(shopPrice == 0){
            UUID shopId = plugin.api.createShop(p, args[1], 1000, new SItemStack(Material.DIAMOND), Man10ShopType.BUY, false);

            if(shopId == null){
                sender.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return false;
            }
            sender.sendMessage(Man10ShopV2.prefix + "§a§lショップを作成しました /" + label + " shopsで管理することができます");
            return true;
        }


        //balance check
        if(Man10ShopV2.vault.getBalance(p.getUniqueId()) < shopPrice){
            sender.sendMessage(Man10ShopV2.prefix + "§c§l所持金が" + BaseUtils.priceString(shopPrice) + "円に達してません");
            return false;
        }

        ConfirmationMenu menu = new ConfirmationMenu(BaseUtils.priceString(shopPrice) + "円支払いますか？", plugin);
        menu.setOnConfirm(e -> {
            if(!Man10ShopV2.vault.withdraw(p.getUniqueId(), shopPrice)){
                sender.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return;
            }

            UUID shopId = plugin.api.createShop(p, args[1], 1000, new SItemStack(Material.DIAMOND), Man10ShopType.BUY, false);

            if(shopId == null){
                sender.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return;
            }
            Man10ShopV2API.log(shopId, "shopCreate", "", p.getName(), p.getUniqueId());
            sender.sendMessage(Man10ShopV2.prefix + "§a§lショップを作成しました \n/" + label + " shopsで管理することができます");
            menu.close(p);
        });
        menu.setOnCancel(e -> menu.close(p));
        menu.open(p);

        return true;
    }
}
