package com.shojabon.man10shopv2.Menus.Shop.Settings;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.Man10ShopModerator;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.LargeSInventoryMenu;
import com.shojabon.man10shopv2.Menus.NumericInputMenu;
import com.shojabon.man10shopv2.Menus.Shop.Permission.PermissionSettingsMenu;
import com.shojabon.man10shopv2.Menus.Shop.ShopMainMenu;
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

        items.add(setNameItem());
        items.add(buyStorageItem());
        items.add(sellCapItem());

        renderedCore.setItems(items);

        inventory = renderedCore.getInventory();
        inventory.setOnCloseEvent(e -> inventory.moveToMenu(player, new ShopMainMenu(player, shop, plugin).getInventory()));
        return inventory;
    }

    public SInventoryItem setNameItem(){
        SItemStack item = new SItemStack(Material.NAME_TAG).setDisplayName(new SStringBuilder().gold().text("ショップの名前を変更する").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在設定: ").yellow().text(shop.name).build());

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(null);

        return inventoryItem;
    }

    public SInventoryItem buyStorageItem(){
        SItemStack item = new SItemStack(Material.CHEST).setDisplayName(new SStringBuilder().gray().text("ショップの倉庫を拡張する").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在設定: ").yellow().text(shop.storageSize).build());

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(null);

        return inventoryItem;
    }

    public SInventoryItem sellCapItem(){
        SItemStack item = new SItemStack(Material.CHEST).setDisplayName(new SStringBuilder().green().text("購入数制限").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在設定: ").yellow().text(shop.settings.getStorageCap()).build());

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

}
