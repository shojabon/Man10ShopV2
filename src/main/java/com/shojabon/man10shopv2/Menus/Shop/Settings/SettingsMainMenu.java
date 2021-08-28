package com.shojabon.man10shopv2.Menus.Shop.Settings;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.Man10ShopModerator;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.BooleanInputMenu;
import com.shojabon.man10shopv2.Menus.ConfirmationMenu;
import com.shojabon.man10shopv2.Menus.LargeSInventoryMenu;
import com.shojabon.man10shopv2.Menus.NumericInputMenu;
import com.shojabon.man10shopv2.Menus.Shop.Permission.PermissionSettingsMenu;
import com.shojabon.man10shopv2.Menus.Shop.Settings.InnerSettings.ShopTypeSelectorMenu;
import com.shojabon.man10shopv2.Menus.Shop.ShopMainMenu;
import com.shojabon.man10shopv2.Utils.BaseUtils;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class SettingsMainMenu {
    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;
    SInventory inventory;

    public SettingsMainMenu(Player p, Man10Shop shop, Man10ShopV2 plugin){
        this.player = p;
        this.shop = shop;
        this.plugin = plugin;
    }

    public SInventory getInventory(){
        ArrayList<SInventoryItem> items = new ArrayList<>();
        LargeSInventoryMenu renderedCore = new LargeSInventoryMenu(new SStringBuilder().darkGray().text("ショップ設定").build(), 5, plugin);

        //define items here

        items.add(shopEnabledItem());
        items.add(sellPriceItem());
        items.add(shopTypeSelectItem());
        items.add(setNameItem());
        items.add(buyStorageItem());
        items.add(sellCapItem());
        items.add(setDeleteShopItem());

        renderedCore.setItems(items);

        inventory = renderedCore.getInventory();
        inventory.setOnCloseEvent(e -> inventory.moveToMenu(player, new ShopMainMenu(player, shop, plugin).getInventory()));
        return inventory;
    }

    public SInventoryItem setNameItem(){
        SItemStack item = new SItemStack(Material.NAME_TAG).setDisplayName(new SStringBuilder().gold().text("ショップの名前を変更する").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(shop.name).build());

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(null);

        return inventoryItem;
    }

    public SInventoryItem sellPriceItem(){
        SItemStack item = new SItemStack(Material.EMERALD).setDisplayName(new SStringBuilder().green().text("取引価格設定").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(shop.price).text("円").build());

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(e -> {

            //number input menu
            NumericInputMenu menu = new NumericInputMenu(new SStringBuilder().green().text("取引値段設定").build(), plugin);
            menu.setOnClose(ee -> menu.inventory.moveToMenu(player, new SettingsMainMenu(player, shop, plugin).getInventory()));
            menu.setOnCancel(ee -> menu.inventory.moveToMenu(player, new SettingsMainMenu(player, shop, plugin).getInventory()));
            menu.setOnConfirm(newValue -> {

                shop.setPrice(newValue);
                menu.inventory.moveToMenu(player, new SettingsMainMenu(player, shop, plugin).getInventory());
            });
            inventory.moveToMenu(player, menu.getInventory());

        });

        return inventoryItem;
    }

    public SInventoryItem buyStorageItem(){
        SItemStack item = new SItemStack(Material.CHEST).setDisplayName(new SStringBuilder().gray().text("ショップの倉庫を拡張する").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の倉庫サイズ: ").yellow().text(shop.storageSize).text("個").build());
        item.addLore("");
        if(shop.calculateNextUnitPrice() != -1){
            item.addLore(new SStringBuilder().red().text("次のサイズ: ").text(shop.calculateCurrentStorageSize(1)).text("個").build());
            item.addLore(new SStringBuilder().yellow().text("価格: ").text(shop.calculateNextUnitPrice()).text("円").build());
            item.addLore(new SStringBuilder().white().bold().text("左クリックで購入").build());
        }
        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(e -> {
            //confirmation menu
            ConfirmationMenu menu = new ConfirmationMenu("確認", plugin);
            menu.setOnClose(ee -> menu.getInventory().moveToMenu(player, new SettingsMainMenu(player, shop, plugin).getInventory()));
            menu.setOnCancel(ee -> menu.getInventory().moveToMenu(player, new SettingsMainMenu(player, shop, plugin).getInventory()));
            menu.setOnConfirm(ee -> {
                shop.buyStorageSpace(player, 1);
                menu.getInventory().moveToMenu(player, new SettingsMainMenu(player, shop, plugin).getInventory());
            });

            inventory.moveToMenu(player, menu.getInventory());

        });


        return inventoryItem;
    }

    public SInventoryItem sellCapItem(){
        SItemStack item = new SItemStack(Material.HOPPER).setDisplayName(new SStringBuilder().green().text("購入数制限").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(shop.settings.getStorageCap()).build());

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(e -> {

            //number input menu
            NumericInputMenu menu = new NumericInputMenu(new SStringBuilder().green().text("購入制限設定").build(), plugin);
            menu.setMaxValue(shop.storageSize);
            menu.setOnClose(ee -> menu.inventory.moveToMenu(player, new SettingsMainMenu(player, shop, plugin).getInventory()));
            menu.setOnCancel(ee -> menu.inventory.moveToMenu(player, new SettingsMainMenu(player, shop, plugin).getInventory()));
            menu.setOnConfirm(newValue -> {
                if(newValue > shop.storageSize){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l購入制限は倉庫以上の数にはできません");
                    return;
                }
                if(newValue < 0){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l購入制限は正の数でなくてはならない");
                    return;
                }

                shop.settings.setSetting("storage.sell.cap", newValue);
                menu.inventory.moveToMenu(player, new SettingsMainMenu(player, shop, plugin).getInventory());
            });
            inventory.moveToMenu(player, menu.getInventory());

        });

        return inventoryItem;
    }

    public SInventoryItem setDeleteShopItem(){
        SItemStack item = new SItemStack(Material.LAVA_BUCKET).setDisplayName(new SStringBuilder().yellow().obfuscated().text("OO")
                .darkRed().bold().text("ショップを削除")
                .yellow().obfuscated().text("OO")
                .build());

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(e -> {
            if(!shop.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.OWNER)){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //confirmation menu
            ConfirmationMenu menu = new ConfirmationMenu("確認", plugin);
            menu.setOnCancel(ee -> menu.getInventory().moveToMenu(player, new SettingsMainMenu(player, shop, plugin).getInventory()));
            menu.setOnConfirm(ee -> {
                //delete shop
                shop.deleteShop();
                player.closeInventory();
            });

            inventory.moveToMenu(player, menu.getInventory());
        });

        return inventoryItem;
    }

    public SInventoryItem shopEnabledItem(){
        SItemStack item = new SItemStack(Material.LEVER).setDisplayName(new SStringBuilder().gray().text("ショップ取引有効").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(BaseUtils.booleanToJapaneseText(shop.settings.getShopEnabled())).build());
        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(e -> {
            //confirmation menu
            BooleanInputMenu menu = new BooleanInputMenu(shop.settings.getShopEnabled(), "ショップ有効か設定", plugin);
            menu.setOnClose(ee -> menu.getInventory().moveToMenu(player, new SettingsMainMenu(player, shop, plugin).getInventory()));
            menu.setOnCancel(ee -> menu.getInventory().moveToMenu(player, new SettingsMainMenu(player, shop, plugin).getInventory()));
            menu.setOnConfirm(bool -> {
                player.sendMessage(String.valueOf(bool));
                shop.settings.setShopEnabled(bool);
                menu.getInventory().moveToMenu(player, new SettingsMainMenu(player, shop, plugin).getInventory());
            });

            inventory.moveToMenu(player, menu.getInventory());

        });


        return inventoryItem;
    }

    public SInventoryItem shopTypeSelectItem(){
        SItemStack item = new SItemStack(Material.OAK_FENCE_GATE).setDisplayName(new SStringBuilder().yellow().text("ショップタイプ設定").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(shop.shopType.name()).build());
        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);

        inventoryItem.setEvent(e -> {
            if(!shop.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //confirmation menu
            inventory.moveToMenu(player, new ShopTypeSelectorMenu(player, shop, plugin).getInventory());

        });


        return inventoryItem;
    }
}
