package com.shojabon.man10shopv2.Commands.SubCommands;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.LargeSInventoryMenu;
import com.shojabon.man10shopv2.Menus.Shop.ShopMainMenu;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ShopsCommand {
    Man10ShopV2 plugin;

    public ShopsCommand(Man10ShopV2 plugin){
        this.plugin = plugin;
    }

    public void execute(CommandSender sender, String[] args){
        if(! (sender instanceof Player)){
            sender.sendMessage(Man10ShopV2.prefix + "§c§lこのコマンドはプレイヤーのみが実行可能です");
            return;
        }

        Player p = ((Player)sender);

        ArrayList<SInventoryItem> items = new ArrayList<>();
        LargeSInventoryMenu menu = new LargeSInventoryMenu(new SStringBuilder().aqua().bold().text("管理可能ショップ一覧").build(), 5, plugin);

        SInventory inventory = menu.getInventory();
        ArrayList<Man10Shop> shops = plugin.api.getShopsWithPermission(p.getUniqueId());
        for(Man10Shop shop: shops){

            SItemStack icon = new SItemStack(shop.targetItem.getTypeItem());
            icon.setDisplayName(new SStringBuilder().green().bold().text(shop.name).build());
            icon.addLore(new SStringBuilder().lightPurple().bold().text("権限: ").yellow().bold().text(String.valueOf(shop.getPermission(p.getUniqueId()))).build());

            SInventoryItem item = new SInventoryItem(icon.build());
            item.clickable(false);
            item.setEvent(e -> inventory.moveToMenu(p, new ShopMainMenu(p, plugin.api.getShop(shop.shopId), plugin).getInventory()));

            items.add(item);
        }

        menu.open(p, items);


    }

}
