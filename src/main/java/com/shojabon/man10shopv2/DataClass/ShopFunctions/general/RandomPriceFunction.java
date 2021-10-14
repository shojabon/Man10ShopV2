package com.shojabon.man10shopv2.DataClass.ShopFunctions.general;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.RandomPriceMenu;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.RandomPricePriceSelector;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.StorageRefillMenu;
import com.shojabon.man10shopv2.Menus.Settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

public class RandomPriceFunction extends ShopFunction {

    //variables

    //init
    public RandomPriceFunction(Man10Shop shop) {
        super(shop);
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
    public Man10ShopType[] enabledShopTypes() {
        return new Man10ShopType[]{Man10ShopType.BUY, Man10ShopType.SELL};
    }

    @Override
    public String settingCategory() {
        return "一般設定";
    }

    @Override
    public boolean hasPermissionToEdit(UUID uuid) {
        return shop.permission.hasPermissionAtLeast(uuid, Man10ShopPermission.MODERATOR);
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
    public SInventoryItem getSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.ENCHANTING_TABLE).setDisplayName(new SStringBuilder().yellow().text("分間毎ごとの値段ランダム決定設定").build());
        if(isFunctionEnabled()){
            item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(getRandomPickMinute()).text("分毎に値段を決定").build());
        }else{
            item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text("なし").build());
        }

        item.addLore("");
        item.addLore("§f設定した分間毎に値段を設定する");
        item.addLore("§fどちらかが0の場合設定は無効化");

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);

        inventoryItem.setEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //confirmation menu
            RandomPriceMenu menu = new RandomPriceMenu(player, shop, plugin);
            sInventory.moveToMenu(player, menu);

        });


        return inventoryItem;
    }
}
