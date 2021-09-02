package com.shojabon.man10shopv2.Menus.Permission;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.Man10ShopModerator;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.EditableShopSelectorMenu;
import com.shojabon.man10shopv2.Menus.ShopMainMenu;
import com.shojabon.man10shopv2.Utils.BannerDictionary;
import com.shojabon.man10shopv2.Utils.SInventory.ToolMenu.ConfirmationMenu;
import com.shojabon.man10shopv2.Utils.SInventory.ToolMenu.LargeSInventoryMenu;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SInventory.ToolMenu.OnlinePlayerSelectorMenu;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Mob;
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
            icon.addLore(new SStringBuilder().lightPurple().text("権限: ").yellow().text(mod.getPermissionString()).build());

            SInventoryItem item = new SInventoryItem(icon.build());
            item.clickable(false);
            item.setEvent(e -> {
                renderedCore.moveToMenu(player, new PermissionSettingsMenu(player, shop, mod, plugin));
            });
            items.add(item);

        }

        items.add(creteAddModeratorItem(renderedCore));

        renderedCore.setItems(items);

        renderedCore.setOnCloseEvent(e -> renderedCore.moveToMenu(player, new ShopMainMenu(player, shop, plugin)));
        return renderedCore;
    }

    public SInventoryItem creteAddModeratorItem(SInventory inventory){
        BannerDictionary dictionary = new BannerDictionary();
        SInventoryItem item = new SInventoryItem(new SItemStack(dictionary.getSymbol("plus")).setDisplayName("§a§l管理者を追加する").build());
        item.clickable(false);
        item.setEvent(e -> {

            OnlinePlayerSelectorMenu playerSelectorMenu = new OnlinePlayerSelectorMenu(player, plugin);
            for(UUID playerUUID: shop.moderators.keySet()){
                playerSelectorMenu.addException(playerUUID);
            }
            playerSelectorMenu.setOnClick(targetPlayer -> {

                if(shop.moderators.containsKey(targetPlayer.getUniqueId())){
                    player.sendMessage(Man10ShopV2.prefix + "§c§lこのプレイヤーはすでに管理者です");
                    return;
                }

                ConfirmationMenu menu = new ConfirmationMenu("§a" + targetPlayer.getName() + "を管理者にしますか？", plugin);
                menu.setOnConfirm(ee -> {
                    if(!shop.addModerator(new Man10ShopModerator(targetPlayer.getName(), targetPlayer.getUniqueId(), Man10ShopPermission.STORAGE_ACCESS, true))){
                        player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                        return;
                    }
                    player.sendMessage(Man10ShopV2.prefix + "§a§l管理者を追加しました");
                    menu.close(player);
                });

                playerSelectorMenu.moveToMenu(player, menu);


            });
            playerSelectorMenu.setOnCloseEvent(ee -> playerSelectorMenu.moveToMenu(player, new ShopMainMenu(player, shop, plugin)));

            inventory.moveToMenu(player, playerSelectorMenu);
        });
        return item;
    }

}
