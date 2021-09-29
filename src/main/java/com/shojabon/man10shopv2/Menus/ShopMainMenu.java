package com.shojabon.man10shopv2.Menus;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.Settings.SettingsMainMenu;
import com.shojabon.man10shopv2.Menus.Storage.ItemStorageMenu;
import com.shojabon.man10shopv2.Utils.SInventory.ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.Menus.Permission.PermissionSettingsMainMenu;
import com.shojabon.man10shopv2.Utils.BaseUtils;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ShopMainMenu extends SInventory {
    
    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;

    public ShopMainMenu(Player p, Man10Shop shop, Man10ShopV2 plugin){
        super(new SStringBuilder().green().text(shop.name + "設定").build(), 3, plugin);
        this.player = p;
        this.shop = shop;
        this.plugin = plugin;
    }

    public void renderMenu(){
        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        fillItem(background);

        //shop info
        setItem(13, getShopInfoItem());

        //shop settings
        setItem(12, getShopSettingsItem());

        //storage
        setItem(14, getStorageSettingsItem());

        //permission settings
        setItem(16, getPermissionSettingsItem());

        setItem(22, getMoneySelectorMenu());

        //target item setting settings
        SInventoryItem targetItemSetting = new SInventoryItem(new SItemStack(Material.BELL).setDisplayName(new SStringBuilder().darkRed().bold().text("取引アイテム設定").build()).build());
        targetItemSetting.clickable(false);
        setItem(10, getTargetItemSettingsItem());

        setOnCloseEvent(e -> {
            if(shop.admin){
                AdminShopSelectorMenu menu = new AdminShopSelectorMenu(player, plugin);
                menu.setOnClick(shop -> menu.moveToMenu(player, new ShopMainMenu(player, plugin.api.getShop(shop.shopId), plugin)));
                moveToMenu(player, menu);
            }else{
                EditableShopSelectorMenu menu = new EditableShopSelectorMenu(player, plugin);
                menu.setOnClick(shop -> menu.moveToMenu(player, new ShopMainMenu(player, plugin.api.getShop(shop.shopId), plugin)));
                moveToMenu(player, menu);
            }
        });

        renderInventory();
    }

    public SInventoryItem getShopInfoItem(){
        String iconName = new SStringBuilder().gold().bold().text("ショップ情報").build();
        SItemStack icon = new SItemStack(Material.OAK_SIGN).setDisplayName(iconName);

        icon.addLore("§aショップ口座残高:§e " + BaseUtils.priceString(shop.money) + "円");
        icon.addLore("§7アイテム数:§e " + BaseUtils.priceString(shop.itemCount));

        SInventoryItem shopInfo = new SInventoryItem(icon.build());
        shopInfo.clickable(false);

        return shopInfo;
    }

    public SInventoryItem getShopSettingsItem(){
        SStringBuilder iconName = new SStringBuilder();

        if(shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
            iconName.gray().bold().text("ショップ設定");
        }else{
            iconName.gray().bold().strike().text("ショップ設定");
        }

        SItemStack icon = new SItemStack(Material.IRON_DOOR).setDisplayName(iconName.build());

        if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
            icon.addLore(new SStringBuilder().red().text("権限がありません").build());
            icon.addLore("");
        }
        icon.addLore(new SStringBuilder().white().text("ショップの設定メニュー").build());
        SInventoryItem item = new SInventoryItem(icon.build());

        item.clickable(false);


        item.setEvent(e -> {
            if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
                player.sendMessage(Man10ShopV2.prefix + "§c§lこの項目を開く権限がありません");
                return;
            }
            moveToMenu(player, new SettingsMainMenu(player, shop, plugin));
        });


        return item;
    }

    public SInventoryItem getStorageSettingsItem(){
        SStringBuilder iconName = new SStringBuilder();

        if(shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.STORAGE_ACCESS)){
            iconName.gray().bold().text("アイテム倉庫").build();
        }else{
            iconName.gray().bold().strike().text("アイテム倉庫");
        }

        SItemStack icon = new SItemStack(Material.CHEST).setDisplayName(iconName.build());

        if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.STORAGE_ACCESS) || shop.permission.hasPermission(player.getUniqueId(), Man10ShopPermission.ACCOUNTANT)){
            icon.addLore(new SStringBuilder().red().text("権限がありません").build());
            icon.addLore("");
        }
        icon.addLore(new SStringBuilder().white().text("アイテムの出しいれをすることができます").build());
        SInventoryItem item = new SInventoryItem(icon.build());

        item.clickable(false);

        item.setEvent(e -> {
            if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.STORAGE_ACCESS) || shop.permission.hasPermission(player.getUniqueId(), Man10ShopPermission.ACCOUNTANT)){
                player.sendMessage(Man10ShopV2.prefix + "§c§lこの項目を開く権限がありません");
                return;
            }

            InOutSelectorMenu menu = new InOutSelectorMenu(player, shop, plugin);
            menu.setOnClose(ee -> menu.moveToMenu(player, new ShopMainMenu(player, shop, plugin)));
            menu.setOnInClicked(ee -> {
                //editing storage
                if(shop.currentlyEditingStorage){
                    player.sendMessage(Man10ShopV2.prefix + "§c§lこのショップは現在倉庫編集中です");
                    return;
                }
                menu.moveToMenu(player, new ItemStorageMenu(false, player, shop, plugin));
            });
            menu.setOnOutClicked(ee -> {
                //editing storage
                if(shop.currentlyEditingStorage){
                    player.sendMessage(Man10ShopV2.prefix + "§c§lこのショップは現在倉庫編集中です");
                    return;
                }
                menu.moveToMenu(player, new ItemStorageMenu(true, player, shop, plugin));
            });
            menu.setInText("倉庫にアイテムを入れる");
            menu.setOutText("倉庫からアイテムを出す");

            moveToMenu(player, menu);
        });



        return item;
    }

    public SInventoryItem getPermissionSettingsItem(){
        SStringBuilder iconName = new SStringBuilder();

        if(shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
            iconName.darkRed().bold().text("権限設定").build();
        }else{
            iconName.gray().bold().strike().text("権限設定");
        }

        SItemStack icon = new SItemStack(Material.BELL).setDisplayName(iconName.build());

        if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
            icon.addLore(new SStringBuilder().red().text("権限がありません").build());
            icon.addLore("");
        }
        icon.addLore(new SStringBuilder().white().text("ショップの設定をできる人などを設定することができます").build());
        SInventoryItem item = new SInventoryItem(icon.build());

        item.clickable(false);

        item.setEvent(e -> {
            if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
                player.sendMessage(Man10ShopV2.prefix + "§c§lこの項目を開く権限がありません");
                return;
            }
            PermissionSettingsMainMenu menu = new PermissionSettingsMainMenu(player, shop, plugin);
            moveToMenu(player, menu.renderInventory());
        });


        return item;
    }

    public SInventoryItem getTargetItemSettingsItem(){
        SStringBuilder iconName = new SStringBuilder();

        if(shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
            iconName.aqua().bold().text("取引アイテム設定").build();
        }else{
            iconName.gray().bold().strike().text("取引アイテム設定");
        }

        SItemStack icon = new SItemStack(Material.LECTERN).setDisplayName(iconName.build());

        if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
            icon.addLore(new SStringBuilder().red().text("権限がありません").build());
            icon.addLore("");
        }
        icon.addLore(new SStringBuilder().white().text("取引する対象のアイテム種を設定することができます").build());
        SInventoryItem item = new SInventoryItem(icon.build());

        item.clickable(false);


        item.setEvent(e -> {
            if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
                player.sendMessage(Man10ShopV2.prefix + "§c§lこの項目を開く権限がありません");
                return;
            }
            moveToMenu(player, new TargetItemSelectorMenu(player, shop, plugin));
        });


        return item;
    }

    public SInventoryItem getMoneySelectorMenu(){
        SStringBuilder iconName = new SStringBuilder();

        if(shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.STORAGE_ACCESS)){
            iconName.green().bold().text("現金出し入れ").build();
        }else{
            iconName.gray().bold().strike().text("現金出し入れ");
        }

        SItemStack icon = new SItemStack(Material.EMERALD).setDisplayName(iconName.build());

        if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.ACCOUNTANT) || shop.permission.hasPermission(player.getUniqueId(), Man10ShopPermission.STORAGE_ACCESS)){
            icon.addLore(new SStringBuilder().red().text("権限がありません").build());
            icon.addLore("");
        }
        icon.addLore(new SStringBuilder().white().text("現金出し入れを行うことができます").build());
        icon.addLore(new SStringBuilder().white().text("取引は電子マネーが使われます").build());
        SInventoryItem item = new SInventoryItem(icon.build());

        item.clickable(false);

        item.setEvent(e -> {
            if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.STORAGE_ACCESS) || shop.permission.hasPermission(player.getUniqueId(), Man10ShopPermission.STORAGE_ACCESS)){
                player.sendMessage(Man10ShopV2.prefix + "§c§lこの項目を開く権限がありません");
                return;
            }

            InOutSelectorMenu menu = new InOutSelectorMenu(player, shop, plugin);
            menu.setInText("口座に入金する");
            menu.setOutText("口座から出金をする");

            menu.setOnInClicked(ee -> menu.moveToMenu(player, generateMoneyEvent(true)));
            menu.setOnOutClicked(ee -> menu.moveToMenu(player, generateMoneyEvent(false)));

            menu.setOnClose(ee -> menu.moveToMenu(player, new ShopMainMenu(player, shop, plugin)));


            moveToMenu(player, menu);
        });



        return item;
    }

    public SInventory generateMoneyEvent(boolean deposit) {
        String title = "出金額を入力してください";
        if(deposit) title = "入金額を入力してください";

        NumericInputMenu menu = new NumericInputMenu( title, plugin);
        if (deposit) {
            menu.setMaxValue((int) Man10ShopV2.vault.getBalance(player.getUniqueId()));
        }
        menu.setAllowZero(false);
        menu.setOnCancel(e -> menu.moveToMenu(player, new ShopMainMenu(player, shop, plugin)));
        menu.setOnClose(e -> menu.moveToMenu(player, new ShopMainMenu(player, shop, plugin)));
        menu.setOnConfirm(integer -> {
            if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.ACCOUNTANT) || shop.permission.hasPermission(player.getUniqueId(), Man10ShopPermission.STORAGE_ACCESS)){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限がありません");
                return;
            }
            if (deposit) {
                if (Man10ShopV2.vault.getBalance(player.getUniqueId()) < integer) {
                    player.sendMessage(Man10ShopV2.prefix + "§c§l現金が不足しています");
                    return;
                }
                Man10ShopV2.vault.withdraw(player.getUniqueId(), integer);
                shop.addMoney(integer);
                Man10ShopV2API.log(shop.shopId, "depositMoney", integer, player.getName(), player.getUniqueId());
                player.sendMessage(Man10ShopV2.prefix + "§a§l" + BaseUtils.priceString(integer) + "円入金しました");
            } else {
                if (shop.money < integer) {
                    player.sendMessage(Man10ShopV2.prefix + "§c§l現金が不足しています");
                    return;
                }
                Man10ShopV2.vault.deposit(player.getUniqueId(), integer);
                shop.removeMoney(integer);
                Man10ShopV2API.log(shop.shopId, "withdrawMoney", integer, player.getName(), player.getUniqueId());
                player.sendMessage(Man10ShopV2.prefix + "§a§l" + BaseUtils.priceString(integer) + "円出金しました");
            }
            menu.moveToMenu(player, new ShopMainMenu(player, shop, plugin));
        });
        return menu;
    }
}
