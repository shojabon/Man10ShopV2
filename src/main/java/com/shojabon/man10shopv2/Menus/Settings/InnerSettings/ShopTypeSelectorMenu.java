package com.shojabon.man10shopv2.Menus.Settings.InnerSettings;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.Settings.SettingsMainMenu;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ShopTypeSelectorMenu extends SInventory{

    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;

    public ShopTypeSelectorMenu(Player p, Man10Shop shop, Man10ShopV2 plugin){
        super("ショップタイプ選択",4, plugin);
        this.shop = shop;
        this.player = p;
        this.plugin = plugin;

    }

    public void renderButtons(){
        SInventoryItem buyMode = new SInventoryItem(new SItemStack(Material.RED_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().red().text("このモードを選択").build()).build());
        buyMode.clickable(false);
        buyMode.setAsyncEvent(e -> {
            if(!shop.shopType.setShopType(Man10ShopType.BUY)){
                player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return;
            }

            Man10ShopV2API.log(shop.shopId, "setShopType", "BUY", player.getName(), player.getUniqueId()); //log
            player.sendMessage(Man10ShopV2.prefix + "§a§lショップタイプが設定されました");
            player.getServer().getScheduler().runTask(plugin, ()-> {
                player.closeInventory();
                plugin.api.updateAllSigns(shop);
            });
        });
        setItem(21, buyMode);

        SInventoryItem sellMode = new SInventoryItem(new SItemStack(Material.RED_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().red().text("このモードを選択").build()).build());
        sellMode.clickable(false);
        sellMode.setAsyncEvent(e -> {
            if(!shop.shopType.setShopType(Man10ShopType.SELL)){
                player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return;
            }
            Man10ShopV2API.log(shop.shopId, "setShopType", "SELL", player.getName(), player.getUniqueId()); //log
            player.sendMessage(Man10ShopV2.prefix + "§a§lショップタイプが設定されました");
            player.getServer().getScheduler().runTask(plugin, ()-> plugin.api.updateAllSigns(shop));
            player.getServer().getScheduler().runTask(plugin, ()-> {
                player.closeInventory();
                plugin.api.updateAllSigns(shop);
            });
        });
        setItem(23, sellMode);



        SInventoryItem current = new SInventoryItem(new SItemStack(Material.LIME_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().green().text("現在の設定").build()).build());
        current.clickable(false);
        if(shop.shopType.getShopType() == Man10ShopType.BUY){
            setItem(21, current);
        }else{
            setItem(23, current);
        }

        renderInventory();

    }

    public void renderMenu(){
        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        fillItem(background);

        SInventoryItem sellMode = new SInventoryItem(new SItemStack(Material.DROPPER).setDisplayName(new SStringBuilder().green().text("販売モード").build()).build());
        sellMode.clickable(false);
        setItem(12, sellMode);

        SInventoryItem buyMode = new SInventoryItem(new SItemStack(Material.HOPPER).setDisplayName(new SStringBuilder().green().text("買取モード").build()).build());
        buyMode.clickable(false);
        setItem(14, buyMode);
        setOnCloseEvent(e -> moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));

        renderButtons();
    }


}
