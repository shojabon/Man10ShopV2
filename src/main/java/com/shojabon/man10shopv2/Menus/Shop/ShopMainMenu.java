package com.shojabon.man10shopv2.Menus.Shop;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.Shop.Permission.PermissionSettingsMainMenu;
import com.shojabon.man10shopv2.Menus.Shop.Settings.SettingsMainMenu;
import com.shojabon.man10shopv2.Menus.Shop.Storage.StorageTypeSelector;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ShopMainMenu {

    SInventory inventory;
    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;

    public ShopMainMenu(Player p, Man10Shop shop, Man10ShopV2 plugin){
        this.player = p;
        this.shop = shop;
        this.plugin = plugin;
        inventory = new SInventory(new SStringBuilder().green().text(shop.name + "設定").build(), 3, plugin);
    }

    public SInventory getInventory() {
        renderMenu();
        return inventory;
    }

    public void renderMenu(){
        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        inventory.fillItem(background);

        //shop info
        inventory.setItem(13, getShopInfoItem());

        //shop settings
        inventory.setItem(12, getShopSettingsItem());

        //storage
        inventory.setItem(14, getStorageSettingsItem());

        //permission settings
        inventory.setItem(16, getPermissionSettingsItem());

        //target item setting settings
        SInventoryItem targetItemSetting = new SInventoryItem(new SItemStack(Material.BELL).setDisplayName(new SStringBuilder().darkRed().bold().text("取引アイテム設定").build()).build());
        targetItemSetting.clickable(false);
        inventory.setItem(10, getTargetItemSettingsItem());
    }

    public SInventoryItem getShopInfoItem(){
        String iconName = new SStringBuilder().gold().bold().text("ショップ情報").build();
        SItemStack icon = new SItemStack(Material.OAK_SIGN).setDisplayName(iconName);
        SInventoryItem shopInfo = new SInventoryItem(icon.build());
        shopInfo.clickable(false);

        return shopInfo;
    }

    public SInventoryItem getShopSettingsItem(){
        SStringBuilder iconName = new SStringBuilder();

        if(shop.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
            iconName.gray().bold().text("ショップ設定");
        }else{
            iconName.gray().bold().strike().text("ショップ設定");
        }

        SItemStack icon = new SItemStack(Material.IRON_DOOR).setDisplayName(iconName.build());

        if(!shop.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
            icon.addLore(new SStringBuilder().red().text("権限がありません").build());
            icon.addLore("");
        }
        icon.addLore(new SStringBuilder().white().text("ショップの設定メニュー").build());
        SInventoryItem item = new SInventoryItem(icon.build());

        item.clickable(false);


        item.setEvent(e -> {
            if(!shop.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
                player.sendMessage(Man10ShopV2.prefix + "§c§lこの項目を開く権限がありません");
                return;
            }
            inventory.moveToMenu(player, new SettingsMainMenu(player, shop, plugin).getInventory());
        });


        return item;
    }

    public SInventoryItem getStorageSettingsItem(){
        SStringBuilder iconName = new SStringBuilder();

        if(shop.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.STORAGE_ACCESS)){
            iconName.gray().bold().text("アイテム倉庫").build();
        }else{
            iconName.gray().bold().strike().text("アイテム倉庫");
        }

        SItemStack icon = new SItemStack(Material.CHEST).setDisplayName(iconName.build());

        if(!shop.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.STORAGE_ACCESS) || shop.hasPermission(player.getUniqueId(), Man10ShopPermission.ACCOUNTANT)){
            icon.addLore(new SStringBuilder().red().text("権限がありません").build());
            icon.addLore("");
        }
        icon.addLore(new SStringBuilder().white().text("アイテムの出しいれをすることができます").build());
        SInventoryItem item = new SInventoryItem(icon.build());

        item.clickable(false);

        item.setEvent(e -> {
            if(!shop.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.STORAGE_ACCESS)){
                player.sendMessage(Man10ShopV2.prefix + "§c§lこの項目を開く権限がありません");
                return;
            }
            inventory.moveToMenu(player, new StorageTypeSelector(player, shop, plugin).getInventory());
        });



        return item;
    }

    public SInventoryItem getPermissionSettingsItem(){
        SStringBuilder iconName = new SStringBuilder();

        if(shop.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
            iconName.darkRed().bold().text("権限設定").build();
        }else{
            iconName.gray().bold().strike().text("権限設定");
        }

        SItemStack icon = new SItemStack(Material.BELL).setDisplayName(iconName.build());

        if(!shop.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
            icon.addLore(new SStringBuilder().red().text("権限がありません").build());
            icon.addLore("");
        }
        icon.addLore(new SStringBuilder().white().text("ショップの設定をできる人などを設定することができます").build());
        SInventoryItem item = new SInventoryItem(icon.build());

        item.clickable(false);

        item.setEvent(e -> {
            if(!shop.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
                player.sendMessage(Man10ShopV2.prefix + "§c§lこの項目を開く権限がありません");
                return;
            }
            PermissionSettingsMainMenu menu = new PermissionSettingsMainMenu(player, shop, plugin);
            inventory.moveToMenu(player, menu.renderInventory());
        });


        return item;
    }

    public SInventoryItem getTargetItemSettingsItem(){
        SStringBuilder iconName = new SStringBuilder();

        if(shop.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
            iconName.aqua().bold().text("取引アイテム設定").build();
        }else{
            iconName.gray().bold().strike().text("取引アイテム設定");
        }

        SItemStack icon = new SItemStack(Material.LECTERN).setDisplayName(iconName.build());

        if(!shop.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
            icon.addLore(new SStringBuilder().red().text("権限がありません").build());
            icon.addLore("");
        }
        icon.addLore(new SStringBuilder().white().text("取引する対象のアイテム種を設定することができます").build());
        SInventoryItem item = new SInventoryItem(icon.build());

        item.clickable(false);


        item.setEvent(e -> {
            if(!shop.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
                player.sendMessage(Man10ShopV2.prefix + "§c§lこの項目を開く権限がありません");
                return;
            }
            inventory.moveToMenu(player, new TargetItemSelectorMenu(player, shop, plugin).getInventory());
        });


        return item;
    }
}
