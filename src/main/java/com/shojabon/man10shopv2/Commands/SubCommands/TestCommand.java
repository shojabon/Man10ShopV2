package com.shojabon.man10shopv2.Commands.SubCommands;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.Man10ShopModerator;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.NumericInputMenu;
import com.shojabon.man10shopv2.Utils.SItemStack;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TestCommand {
    Man10ShopV2 plugin;

    public TestCommand(Man10ShopV2 plugin){
        this.plugin = plugin;
    }

    public void execute(CommandSender sender, String[] args){
        if(! (sender instanceof Player)){
            sender.sendMessage(Man10ShopV2.prefix + "§c§lこのコマンドはプレイヤーのみが実行可能です");
            return;
        }

        Player p = ((Player)sender);
        Man10Shop shop = plugin.api.getShop(UUID.fromString("6d524add-d0f9-4c46-af1e-1307e2bab7f4"));
        p.sendMessage(String.valueOf(shop.calculateNextUnitPrice()));
//        shop.addModerator(new Man10ShopModerator(p.getName(), p.getUniqueId(), Man10ShopPermission.OWNER));
//        new PermissionSettingsMenu(p, shop, shop.moderators.get(p.getUniqueId()), plugin).renderInventory().open(p);




    }

}
