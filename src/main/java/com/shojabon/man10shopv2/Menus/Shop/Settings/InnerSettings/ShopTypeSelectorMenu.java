package com.shojabon.man10shopv2.Menus.Shop.Settings.InnerSettings;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.Man10ShopModerator;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.ConfirmationMenu;
import com.shojabon.man10shopv2.Menus.Shop.Permission.PermissionSettingsMainMenu;
import com.shojabon.man10shopv2.Menus.Shop.Settings.SettingsMainMenu;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.function.Consumer;

public class ShopTypeSelectorMenu {

    Man10Shop shop;
    Man10ShopV2 plugin;
    SInventory inventory;
    Player player;

    public ShopTypeSelectorMenu(Player p, Man10Shop shop, Man10ShopV2 plugin){
        this.shop = shop;
        this.player = p;
        this.plugin = plugin;

        inventory = new SInventory("ショップタイプ選択",4, plugin);
    }

    public SInventory getInventory() {
        renderInventory();
        return inventory;
    }

    public void renderButtons(){
        SInventoryItem buyMode = new SInventoryItem(new SItemStack(Material.RED_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().red().text("このモードを選択").build()).build());
        buyMode.clickable(false);
        buyMode.setEvent(e -> {
            if(!shop.setShopType(Man10ShopType.BUY)){
                player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return;
            }
            player.sendMessage(Man10ShopV2.prefix + "§a§lショップタイプが設定されました");
            renderButtons();
        });
        inventory.setItem(21, buyMode);

        SInventoryItem sellMode = new SInventoryItem(new SItemStack(Material.RED_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().red().text("このモードを選択").build()).build());
        sellMode.clickable(false);
        sellMode.setEvent(e -> {
            if(!shop.setShopType(Man10ShopType.SELL)){
                player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return;
            }
            player.sendMessage(Man10ShopV2.prefix + "§a§lショップタイプが設定されました");
            renderButtons();
        });
        inventory.setItem(23, sellMode);



        SInventoryItem current = new SInventoryItem(new SItemStack(Material.LIME_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().green().text("現在の設定").build()).build());
        current.clickable(false);
        if(shop.shopType == Man10ShopType.BUY){
            inventory.setItem(21, current);
        }else{
            inventory.setItem(23, current);
        }

        inventory.renderInventory();

    }

    public void renderInventory(){
        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        inventory.fillItem(background);

        SInventoryItem sellMode = new SInventoryItem(new SItemStack(Material.DROPPER).setDisplayName(new SStringBuilder().green().text("販売モード").build()).build());
        sellMode.clickable(false);
        inventory.setItem(12, sellMode);

        SInventoryItem buyMode = new SInventoryItem(new SItemStack(Material.HOPPER).setDisplayName(new SStringBuilder().green().text("買取モード").build()).build());
        buyMode.clickable(false);
        inventory.setItem(14, buyMode);
        inventory.setOnCloseEvent(e -> inventory.moveToMenu(player, new SettingsMainMenu(player, shop, plugin).getInventory()));

        renderButtons();
    }


}
