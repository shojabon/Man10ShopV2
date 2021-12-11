package com.shojabon.man10shopv2.commands.subCommands;

import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.menus.AdminShopSelectorMenu;
import com.shojabon.man10shopv2.menus.ShopMainMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdminShopsCommand implements CommandExecutor {
    Man10ShopV2 plugin;

    public AdminShopsCommand(Man10ShopV2 plugin){
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

        Player p = ((Player)sender);

        AdminShopSelectorMenu menu = new AdminShopSelectorMenu(p, "その他", plugin);
        menu.setOnClick(shop -> menu.moveToMenu(p, new ShopMainMenu(p, plugin.api.getShop(shop.shopId), plugin)));
        menu.open(p);
        return true;
    }
}
