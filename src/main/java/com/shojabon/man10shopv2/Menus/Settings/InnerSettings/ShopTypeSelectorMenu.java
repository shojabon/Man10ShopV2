package com.shojabon.man10shopv2.Menus.Settings.InnerSettings;

import ToolMenu.AutoScaledMenu;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.Settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import io.papermc.paper.util.TransformingRandomAccessList;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ShopTypeSelectorMenu extends AutoScaledMenu {

    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;

    public ShopTypeSelectorMenu(Player p, Man10Shop shop, Man10ShopV2 plugin){
        super("ショップタイプ選択", plugin);
        this.shop = shop;
        this.player = p;
        this.plugin = plugin;


        SInventoryItem sellMode = new SInventoryItem(new SItemStack(Material.DROPPER).setDisplayName(new SStringBuilder().green().text("販売モード").build()).build());
        sellMode.clickable(false);
        sellMode.setAsyncEvent(e -> {
            if(!shop.shopType.setShopType(Man10ShopType.BUY)){
                player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return;
            }

            Man10ShopV2API.log(shop.shopId, "setShopType", "BUY", player.getName(), player.getUniqueId()); //log
            player.sendMessage(Man10ShopV2.prefix + "§a§lショップタイプが設定されました");
            player.getServer().getScheduler().runTask(plugin, ()-> {
                player.closeInventory();
                Man10ShopV2.api.updateAllSigns(shop);
            });
        });
        addItem(sellMode);

        SInventoryItem buyMode = new SInventoryItem(new SItemStack(Material.HOPPER).setDisplayName(new SStringBuilder().green().text("買取モード").build()).build());
        buyMode.clickable(false);
        buyMode.setAsyncEvent(e -> {
            if(!shop.shopType.setShopType(Man10ShopType.SELL)){
                player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return;
            }
            Man10ShopV2API.log(shop.shopId, "setShopType", "SELL", player.getName(), player.getUniqueId()); //log
            player.sendMessage(Man10ShopV2.prefix + "§a§lショップタイプが設定されました");
            player.getServer().getScheduler().runTask(plugin, ()-> {
                player.closeInventory();
                Man10ShopV2.api.updateAllSigns(shop);
            });
        });
        setOnCloseEvent(e -> moveToMenu(player, new SettingsMainMenu(player, shop, shop.shopType.settingCategory(), plugin)));
        addItem(buyMode);

        if(shop.isAdminShop()){
            //barter
            SInventoryItem barterMode = new SInventoryItem(new SItemStack(Material.VILLAGER_SPAWN_EGG).setDisplayName(new SStringBuilder().green().text("トレードモード").build()).build());
            barterMode.clickable(false);
            barterMode.setAsyncEvent(e -> {
                if(!shop.shopType.setShopType(Man10ShopType.BARTER)){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                    return;
                }
                Man10ShopV2API.log(shop.shopId, "setShopType", "BARTER", player.getName(), player.getUniqueId()); //log
                player.sendMessage(Man10ShopV2.prefix + "§a§lショップタイプが設定されました");
                player.getServer().getScheduler().runTask(plugin, ()-> {
                    player.closeInventory();
                    Man10ShopV2.api.updateAllSigns(shop);
                });
            });
            addItem(barterMode);

            SInventoryItem lootBoxMode = new SInventoryItem(new SItemStack(Material.CHEST).setDisplayName(new SStringBuilder().green().text("トレードモード").build()).build());
            lootBoxMode.clickable(false);
            lootBoxMode.setAsyncEvent(e -> {
                if(!shop.shopType.setShopType(Man10ShopType.LOOT_BOX)){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                    return;
                }
                Man10ShopV2API.log(shop.shopId, "setShopType", "LOOTBOX", player.getName(), player.getUniqueId()); //log
                player.sendMessage(Man10ShopV2.prefix + "§a§lショップタイプが設定されました");
                player.getServer().getScheduler().runTask(plugin, ()-> {
                    player.closeInventory();
                    Man10ShopV2.api.updateAllSigns(shop);
                });
            });
            addItem(lootBoxMode);


            SInventoryItem questMode = new SInventoryItem(new SItemStack(Material.OAK_SIGN).setDisplayName(new SStringBuilder().green().text("クエストモード").build()).build());
            questMode.clickable(false);
            questMode.setAsyncEvent(e -> {
                if(!shop.shopType.setShopType(Man10ShopType.QUEST)){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                    return;
                }
                Man10ShopV2API.log(shop.shopId, "setShopType", "QUEST", player.getName(), player.getUniqueId()); //log
                player.sendMessage(Man10ShopV2.prefix + "§a§lショップタイプが設定されました");
                player.getServer().getScheduler().runTask(plugin, ()-> {
                    player.closeInventory();
                    Man10ShopV2.api.updateAllSigns(shop);
                });
            });
            addItem(questMode);
        }

    }



}
