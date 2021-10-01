package com.shojabon.man10shopv2.Menus.Settings;

import ToolMenu.ConfirmationMenu;
import ToolMenu.LargeSInventoryMenu;
import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.ShopMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SLongTextInput;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;

public class SettingsMainMenu extends LargeSInventoryMenu {
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




        items.add(setDeleteShopItem());

        setItems(items);
        setOnCloseEvent(e -> moveToMenu(player, new ShopMainMenu(player, shop, plugin)));
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
