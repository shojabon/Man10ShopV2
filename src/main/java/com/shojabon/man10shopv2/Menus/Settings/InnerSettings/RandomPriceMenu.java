package com.shojabon.man10shopv2.Menus.Settings.InnerSettings;

import ToolMenu.NumericInputMenu;
import ToolMenu.TimeSelectorMenu;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.Settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RandomPriceMenu extends SInventory{

    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;

    public RandomPriceMenu(Player p, Man10Shop shop, Man10ShopV2 plugin){
        super("分間毎ごとの値段ランダム決定設定",3, plugin);
        this.shop = shop;
        this.player = p;
        this.plugin = plugin;

    }


    public void renderMenu(){
        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        fillItem(background);

        SInventoryItem timeSetting = new SInventoryItem(new SItemStack(Material.CLOCK).setDisplayName(new SStringBuilder().green().text("時間設定").build()).build());
        timeSetting.clickable(false);
        timeSetting.setEvent(e -> {

            NumericInputMenu menu = new NumericInputMenu("時間を入力してください 0はoff", plugin);
            menu.setOnConfirm(number -> {
                if(!shop.randomPrice.setRandomPickMinute(number)){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                    return;
                }
                player.sendMessage(Man10ShopV2.prefix + "§a§l時間を設定しました");
                moveToMenu(player, new RandomPriceMenu(player, shop, plugin));
            });
            menu.setOnCancel(ee -> moveToMenu(player, new RandomPriceMenu(player, shop, plugin)));
            menu.setOnClose(ee -> moveToMenu(player, new RandomPriceMenu(player, shop, plugin)));

            moveToMenu(player, menu);
        });
        setItem(11, timeSetting);


        SInventoryItem priceGroup = new SInventoryItem(new SItemStack(Material.EMERALD_BLOCK).setDisplayName(new SStringBuilder().green().text("値段群を設定").build()).build());
        priceGroup.clickable(false);
        priceGroup.setEvent(e -> {

            RandomPricePriceSelector menu = new RandomPricePriceSelector(player, shop, plugin);
            moveToMenu(player, menu);
        });
        setItem(15, priceGroup);


        SInventoryItem setRefillStartingTime = new SInventoryItem(new SItemStack(Material.COMPASS)
                .setDisplayName(new SStringBuilder().green().text("最終値段選択時間を設定する").build())
                .addLore("§d§l現在設定: §e§l" + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(shop.randomPrice.getLastPickedTime()*1000L)))
                .build());
        setRefillStartingTime.clickable(false);
        setItem(13, setRefillStartingTime);
        setRefillStartingTime.setEvent(e -> {
            TimeSelectorMenu menu = new TimeSelectorMenu(System.currentTimeMillis()/1000L, "最終値段選択時間を設定してくださ", plugin);
            menu.setOnConfirm(lastRefillTime -> {
                if(lastRefillTime == -1L){
                    shop.randomPrice.deleteSetting("shop.randomPrice.lastRefillTime");
                }else{
                    if(!shop.randomPrice.setLastPickedTime(lastRefillTime)){
                        player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                        return;
                    }
                }
                player.sendMessage(Man10ShopV2.prefix + "§a§l最新の補充開始時間を現在に設定しました");
                moveToMenu(player, new RandomPriceMenu(player, shop, plugin));
            });
            menu.setOnCloseEvent(ee -> moveToMenu(player, new RandomPriceMenu(player, shop, plugin)));
            moveToMenu(player, menu);
        });

        setOnCloseEvent(e -> moveToMenu(player, new SettingsMainMenu(player, shop, shop.randomPrice.settingCategory(), plugin)));



    }


}
