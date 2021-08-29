package com.shojabon.man10shopv2.Menus.Shop.Permission;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.Man10ShopModerator;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.LargeSInventoryMenu;
import com.shojabon.man10shopv2.Menus.Shop.ShopMainMenu;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class PermissionSettingsMainMenu {
    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;

    public PermissionSettingsMainMenu(Player p, Man10Shop shop, Man10ShopV2 plugin){
        this.player = p;
        this.shop = shop;
        this.plugin = plugin;
    }

    public SInventory renderInventory(){
        ArrayList<SInventoryItem> items = new ArrayList<>();

        LargeSInventoryMenu renderedCore = new LargeSInventoryMenu(new SStringBuilder().darkRed().text("権限設定").build(), plugin);

        for(UUID modUUID: shop.moderators.keySet()){
            Man10ShopModerator mod = shop.moderators.get(modUUID);

            SItemStack icon = new SItemStack(Material.PLAYER_HEAD).setHeadOwner(mod.uuid);
            icon.setDisplayName(new SStringBuilder().gold().bold().text(mod.name).build());
            icon.addLore(new SStringBuilder().lightPurple().text("権限: ").yellow().text(mod.permission.name()).build());

            SInventoryItem item = new SInventoryItem(icon.build());
            item.clickable(false);
            item.setEvent(e -> {
                renderedCore.moveToMenu(player, new PermissionSettingsMenu(player, shop, mod, plugin));
            });
            items.add(item);

        }

        renderedCore.setItems(items);

        renderedCore.setOnCloseEvent(e -> renderedCore.moveToMenu(player, new ShopMainMenu(player, shop, plugin)));
        return renderedCore;
    }

}
