package com.shojabon.man10shopv2.shopFunctions.general;

import ToolMenu.AutoScaledMenu;
import ToolMenu.NumericInputMenu;
import ToolMenu.TimeSelectorMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.menus.settings.innerSettings.RandomPricePriceSelector;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;
@ShopFunctionDefinition(
        name = "ランダム価格設定",
        explanation = {"設定した分間毎に値段を設定する", "どちらかが0の場合設定は無効化"},
        enabledShopType = {Man10ShopType.BUY, Man10ShopType.SELL},
        iconMaterial = Material.ENCHANTING_TABLE,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class RandomPriceFunction extends ShopFunction {

    //variables
    public Man10ShopSetting<List<Integer>> prices = new Man10ShopSetting<>("shop.randomPrice.prices", new ArrayList<>());
    public Man10ShopSetting<Integer> randomPickMinute = new Man10ShopSetting<>("shop.randomPrice.time", 0);
    public Man10ShopSetting<Long> lastRefillTime = new Man10ShopSetting<>("shop.randomPrice.lastRefillTime", 0L);
    //init
    public RandomPriceFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions

    public long calculateLastPickTime(){
        long secondsSinceLastRefill = System.currentTimeMillis()/1000L - lastRefillTime.get();
        long skippedRefills = secondsSinceLastRefill/(randomPickMinute.get()*60L);

        return lastRefillTime.get() + skippedRefills*randomPickMinute.get()*60L;
    }

    //====================
    // settings
    //====================
    


    @Override
    public boolean isFunctionEnabled(){
        return randomPickMinute.get() != 0 && prices.get().size() != 0;
    }

    @Override
    public void perMinuteExecuteTask() {
        if(System.currentTimeMillis()/1000L - lastRefillTime.get() >= randomPickMinute.get()*60L){
            //select random price
            lastRefillTime.set(calculateLastPickTime());
            if(prices.get().size() == 0) return;
            ArrayList<Integer> pricesLocal = new ArrayList<>(prices.get());
            Collections.shuffle(pricesLocal);
            shop.price.setPrice(pricesLocal.get(0));
            Man10ShopV2.api.updateAllSigns(shop);
        }
    }

    @Override
    public String currentSettingString() {
        return randomPickMinute.get() + "分毎に決定";
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //confirmation menu
            getInnerSettingMenu(player, plugin).open(player);
        });

        return item;
    }


    public AutoScaledMenu getInnerSettingMenu(Player player, Man10ShopV2 plugin){

        AutoScaledMenu autoScaledMenu = new AutoScaledMenu("ランダム値段設定", plugin);

        SInventoryItem timeSetting = new SInventoryItem(new SItemStack(Material.CLOCK).setDisplayName(new SStringBuilder().green().text("時間設定").build()).build());
        timeSetting.clickable(false);
        timeSetting.setEvent(e -> {

            NumericInputMenu menu = new NumericInputMenu("時間を入力してください 0はoff", plugin);
            menu.setOnConfirm(number -> {
                if(!randomPickMinute.set(number)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "時間を設定しました");
                getInnerSettingMenu(player, plugin).open(player);
            });
            menu.setOnCancel(ee -> getInnerSettingMenu(player, plugin).open(player));
            menu.setOnClose(ee -> getInnerSettingMenu(player, plugin).open(player));

            menu.open(player);
        });


        SInventoryItem priceGroup = new SInventoryItem(new SItemStack(Material.EMERALD_BLOCK).setDisplayName(new SStringBuilder().green().text("値段群を設定").build()).build());
        priceGroup.clickable(false);
        priceGroup.setEvent(e -> {

            RandomPricePriceSelector menu = new RandomPricePriceSelector(player, shop, plugin);
            menu.open(player);
        });


        SInventoryItem setRefillStartingTime = new SInventoryItem(new SItemStack(Material.COMPASS)
                .setDisplayName(new SStringBuilder().green().text("最終値段選択時間を設定する").build())
                .addLore("§d§l現在設定: §e§l" + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(shop.randomPrice.lastRefillTime.get()*1000L)))
                .build());
        setRefillStartingTime.clickable(false);
        setRefillStartingTime.setEvent(e -> {
            TimeSelectorMenu menu = new TimeSelectorMenu(System.currentTimeMillis()/1000L, "最終値段選択時間を設定してくださ", plugin);
            menu.setOnConfirm(lastRefillTimeLocal -> {
                if(lastRefillTimeLocal == -1L){
                    lastRefillTime.delete();
                }else{
                    if(!lastRefillTime.set(lastRefillTimeLocal)){
                        warn(player, "内部エラーが発生しました");
                        return;
                    }
                }
                success(player, "最新の補充開始時間を現在に設定しました");
                getInnerSettingMenu(player, plugin).open(player);
            });
            menu.setOnCloseEvent(ee -> getInnerSettingMenu(player, plugin).open(player));
            menu.open(player);
        });

        autoScaledMenu.setOnCloseEvent(e -> {new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);});

        autoScaledMenu.addItem(timeSetting);
        autoScaledMenu.addItem(setRefillStartingTime);
        autoScaledMenu.addItem(priceGroup);

        return autoScaledMenu;
    }
}
