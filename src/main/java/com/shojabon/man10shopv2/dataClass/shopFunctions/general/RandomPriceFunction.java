package com.shojabon.man10shopv2.dataClass.shopFunctions.general;

import ToolMenu.AutoScaledMenu;
import ToolMenu.NumericInputMenu;
import ToolMenu.TimeSelectorMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.menus.settings.innerSettings.RandomPricePriceSelector;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
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

    //init
    public RandomPriceFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions

    public long calculateLastPickTime(){
        long secondsSinceLastRefill = System.currentTimeMillis()/1000L - getLastPickedTime();
        long skippedRefills = secondsSinceLastRefill/(getRandomPickMinute()*60L);

        return getLastPickedTime() + skippedRefills*getRandomPickMinute()*60L;
    }

    //====================
    // settings
    //====================

    public ArrayList<Integer> getPrices(){
        String currentSetting = getSetting("shop.randomPrice.prices");
        if(currentSetting == null) return new ArrayList<>();
        ArrayList<Integer> result = new ArrayList<>();
        for(String candidate: currentSetting.split("\\|")){
            if(!BaseUtils.isInt(candidate)) continue;
            result.add(Integer.parseInt(candidate));
        }
        return result;
    }

    public boolean setPrices(ArrayList<Integer> prices){
        if(getPrices() == prices) return true;
        StringBuilder result = new StringBuilder();
        for(int i: prices){
            result.append(i).append("|");
        }
        String finalResult = "";
        if(result.length() != 0) finalResult = result.substring(0, result.length()-1);
        return setSetting("shop.randomPrice.prices", finalResult);
    }

    public int getRandomPickMinute(){
        String currentSetting = getSetting("shop.randomPrice.time");
        if(!BaseUtils.isInt(currentSetting)) return 0;
        return Integer.parseInt(currentSetting);
    }

    public boolean setRandomPickMinute(int time){
        if(getRandomPickMinute() == time) return true;
        return setSetting("shop.randomPrice.time", time);
    }

    public long getLastPickedTime(){
        String currentSetting = getSetting("shop.randomPrice.lastRefillTime");
        if(!BaseUtils.isLong(currentSetting)) return 0L;
        return Long.parseLong(currentSetting);
    }

    public boolean setLastPickedTime(long time){
        if(getLastPickedTime() == time) return true;
        return setSetting("shop.randomPrice.lastRefillTime", time);
    }


    @Override
    public boolean isFunctionEnabled(){
        return getRandomPickMinute() != 0 && getPrices().size() != 0;
    }

    @Override
    public void perMinuteExecuteTask() {
        if(System.currentTimeMillis()/1000L - getLastPickedTime() >= getRandomPickMinute()*60L){
            //select random price
            setLastPickedTime(calculateLastPickTime());
            if(getPrices().size() == 0) return;
            ArrayList<Integer> prices = getPrices();
            Collections.shuffle(prices);
            shop.price.setPrice(prices.get(0));
            Man10ShopV2.api.updateAllSigns(shop);
        }
    }

    @Override
    public String currentSettingString() {
        return getRandomPickMinute() + "分毎に決定";
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
                if(!shop.randomPrice.setRandomPickMinute(number)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "時間を設定しました");
                menu.moveToMenu(player, getInnerSettingMenu(player, plugin));
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
                .addLore("§d§l現在設定: §e§l" + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(shop.randomPrice.getLastPickedTime()*1000L)))
                .build());
        setRefillStartingTime.clickable(false);
        setRefillStartingTime.setEvent(e -> {
            TimeSelectorMenu menu = new TimeSelectorMenu(System.currentTimeMillis()/1000L, "最終値段選択時間を設定してくださ", plugin);
            menu.setOnConfirm(lastRefillTime -> {
                if(lastRefillTime == -1L){
                    shop.randomPrice.deleteSetting("shop.randomPrice.lastRefillTime");
                }else{
                    if(!shop.randomPrice.setLastPickedTime(lastRefillTime)){
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
