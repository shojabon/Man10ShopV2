package com.shojabon.man10shopv2.Commands.SubCommands;

import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.Shop.EditableShopSelectorMenu;
import com.shojabon.man10shopv2.Menus.Shop.ShopMainMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShopsCommand implements CommandExecutor {
    Man10ShopV2 plugin;

    public ShopsCommand(Man10ShopV2 plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(! (sender instanceof Player)){
            sender.sendMessage(Man10ShopV2.prefix + "§c§lこのコマンドはプレイヤーのみが実行可能です");
            return false;
        }

        Player p = ((Player)sender);

        EditableShopSelectorMenu menu = new EditableShopSelectorMenu(p, plugin);
        menu.setOnClick(shop -> menu.moveToMenu(p, new ShopMainMenu(p, plugin.api.getShop(shop.shopId), plugin)));
        menu.open(p);
        return true;
    }
}
