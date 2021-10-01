package com.shojabon.man10shopv2.Menus.Settings.InnerSettings;

import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.Settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PerMinuteCoolDownSelectorMenu extends SInventory{

    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;

    public PerMinuteCoolDownSelectorMenu(Player p, Man10Shop shop, Man10ShopV2 plugin){
        super("分間毎ごとのクールダウン設定",3, plugin);
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
                if(!shop.perMinuteCoolDown.setPerMinuteCoolDownTime(number)){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                    return;
                }
                player.sendMessage(Man10ShopV2.prefix + "§a§l時間を設定しました");
                shop.perMinuteCoolDown.loadPerMinuteMap();
                moveToMenu(player, new PerMinuteCoolDownSelectorMenu(player, shop, plugin));
            });
            menu.setOnCancel(ee -> moveToMenu(player, new PerMinuteCoolDownSelectorMenu(player, shop, plugin)));
            menu.setOnClose(ee -> moveToMenu(player, new PerMinuteCoolDownSelectorMenu(player, shop, plugin)));

            moveToMenu(player, menu);
        });
        setItem(12, timeSetting);

        SInventoryItem amountSetting = new SInventoryItem(new SItemStack(Material.HOPPER).setDisplayName(new SStringBuilder().green().text("個数設定").build()).build());
        amountSetting.clickable(false);
        setItem(14, amountSetting);
        amountSetting.setEvent(e -> {

            NumericInputMenu menu = new NumericInputMenu("個数を入力してください 0はoff", plugin);
            menu.setOnConfirm(number -> {
                if(!shop.perMinuteCoolDown.setPerMinuteCoolDownAmount(number)){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                    return;
                }
                player.sendMessage(Man10ShopV2.prefix + "§a§l個数を設定しました");
                moveToMenu(player, new PerMinuteCoolDownSelectorMenu(player, shop, plugin));
            });
            menu.setOnCancel(ee -> moveToMenu(player, new PerMinuteCoolDownSelectorMenu(player, shop, plugin)));
            menu.setOnClose(ee -> moveToMenu(player, new PerMinuteCoolDownSelectorMenu(player, shop, plugin)));

            moveToMenu(player, menu);
        });
        setOnCloseEvent(e -> moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));

    }


}
