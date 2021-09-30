package com.shojabon.man10shopv2.Menus.Settings;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.PerMinuteCoolDownSelectorMenu;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.ShopTypeSelectorMenu;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.WeekdayShopToggleMenu;
import com.shojabon.man10shopv2.Utils.SInventory.ToolMenu.BooleanInputMenu;
import com.shojabon.man10shopv2.Utils.SInventory.ToolMenu.ConfirmationMenu;
import com.shojabon.man10shopv2.Utils.SInventory.ToolMenu.LargeSInventoryMenu;
import com.shojabon.man10shopv2.Utils.SInventory.ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.Menus.ShopMainMenu;
import com.shojabon.man10shopv2.Utils.BaseUtils;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SLongTextInput;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;

public class SettingsMainMenu extends LargeSInventoryMenu{
    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;


    public SettingsMainMenu(Player p, Man10Shop shop, Man10ShopV2 plugin) {
        super(new SStringBuilder().darkGray().text("ショップ設定").build(), plugin);
        this.player = p;
        this.shop = shop;
        this.plugin = plugin;
    }

    public void renderMenu(){
        ArrayList<SInventoryItem> items = new ArrayList<>();

        //set items from function
        for(ShopFunction func: shop.functions){
            SInventoryItem item = func.getSettingItem(player, this, plugin);
            if(item == null)continue;
            items.add(item);
        }

        //admin items
        for(ShopFunction func: shop.functions){
            if(!shop.isAdminShop()) continue;
            SInventoryItem item = func.getAdminSettingItem(player, this, plugin);
            if(item == null)continue;
            items.add(item);
        }

        items.add(sellPriceItem());
        items.add(shopTypeSelectItem());
        items.add(setNameItem());




        items.add(setDeleteShopItem());

        setItems(items);
        setOnCloseEvent(e -> moveToMenu(player, new ShopMainMenu(player, shop, plugin)));
    }

    public SInventoryItem setNameItem(){
        SItemStack item = new SItemStack(Material.NAME_TAG).setDisplayName(new SStringBuilder().gold().text("ショップの名前を変更する").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(shop.name).build());

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setAsyncEvent(e -> {

            //text input
            SLongTextInput textInput = new SLongTextInput("§d§lショップ名を入力してください", plugin);
            textInput.setOnConfirm(shopName -> {
                if(shopName.length() > 64 || shopName.length() == 0){
                    player.sendMessage(Man10ShopV2.prefix + "§c§lショップ名は64文字以内でなくてはなりません");
                    return;
                }
                threadPool.execute(() -> {
                    if(!shop.setName(shopName)){
                        player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                        return;
                    }
                    player.sendMessage(Man10ShopV2.prefix + "§a§lショップ名を変更しました");
                });
            });

            textInput.setOnCancel(ee -> player.sendMessage(Man10ShopV2.prefix + "§c§lキャンセルしました"));


            textInput.open(player);
            close(player);
        });

        return inventoryItem;
    }

    public SInventoryItem sellPriceItem(){
        SItemStack item = new SItemStack(Material.EMERALD).setDisplayName(new SStringBuilder().green().text("取引価格設定").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(BaseUtils.priceString(shop.price)).text("円").build());

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(e -> {

            //number input menu
            NumericInputMenu menu = new NumericInputMenu(new SStringBuilder().green().text("取引値段設定").build(), plugin);
            menu.setOnClose(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));
            menu.setOnCancel(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));
            menu.setOnConfirm(newValue -> {
                if(shop.setPrice(newValue)){
                    Man10ShopV2API.log(shop.shopId, "setPrice", newValue, player.getName(), player.getUniqueId()); //log
                }
                plugin.api.updateAllSigns(shop);
                menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin));
            });
            moveToMenu(player, menu);

        });

        return inventoryItem;
    }

    public SInventoryItem shopTypeSelectItem(){
        SItemStack item = new SItemStack(Material.OAK_FENCE_GATE).setDisplayName(new SStringBuilder().yellow().text("ショップタイプ設定").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(BaseUtils.buySellToString(shop.shopType)).build());
        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);

        inventoryItem.setEvent(e -> {
            if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //confirmation menu
            moveToMenu(player, new ShopTypeSelectorMenu(player, shop, plugin));

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
        inventoryItem.setAsyncEvent(e -> {
            if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.OWNER)){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //confirmation menu
            ConfirmationMenu menu = new ConfirmationMenu("確認", plugin);
            menu.setOnCancel(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));
            menu.setOnConfirm(ee -> {
                //delete shop
                shop.deleteShop();
                plugin.getServer().getScheduler().runTask(plugin, () -> plugin.api.destroyAllSigns(shop));
                Man10ShopV2API.log(shop.shopId, "deleteShop", null, player.getName(), player.getUniqueId()); //log
                menu.close(player);
            });

            moveToMenu(player, menu);
        });

        return inventoryItem;
    }
}
