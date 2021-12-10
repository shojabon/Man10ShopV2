package com.shojabon.man10shopv2.DataClass.ShopFunctions.mQuest;

import ToolMenu.AutoScaledMenu;
import ToolMenu.NumericInputMenu;
import ToolMenu.TimeSelectorMenu;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.RandomPricePriceSelector;
import com.shojabon.man10shopv2.Menus.Settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

public class MQuestTimeWindowFunction extends ShopFunction {

    //variables

    //init
    public MQuestTimeWindowFunction(Man10Shop shop) {
        super(shop);
    }


    //functions

    public long calculateLastPickTime(){
        long secondsSinceLastRefill = System.currentTimeMillis()/1000L - getLastPickedTime();
        long skippedRefills = secondsSinceLastRefill/(getRefreshMinutes()*60L);

        return getLastPickedTime() + skippedRefills*getRefreshMinutes()*60L;
    }

    //====================
    // settings
    //====================

    public int getRefreshMinutes(){
        String currentSetting = getSetting("quest.refresh.time");
        if(!BaseUtils.isInt(currentSetting)) return 0;
        return Integer.parseInt(currentSetting);
    }

    public boolean setRefreshMinutes(int time){
        if(getRefreshMinutes() == time) return true;
        return setSetting("quest.refresh.time", time);
    }

    public long getLastPickedTime(){
        String currentSetting = getSetting("quest.refresh.lastRefreshTime");
        if(!BaseUtils.isLong(currentSetting)) return 0L;
        return Long.parseLong(currentSetting);
    }

    public boolean setLastPickedTime(long time){
        if(getLastPickedTime() == time) return true;
        return setSetting("quest.refresh.lastRefreshTime", time);
    }

    @Override
    public Man10ShopType[] enabledShopTypes() {
        return new Man10ShopType[]{Man10ShopType.QUEST};
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
        return getRefreshMinutes() != 0;
    }

    @Override
    public void perMinuteExecuteTask() {
        if(System.currentTimeMillis()/1000L - getLastPickedTime() >= getRefreshMinutes()*60L){
            //select random price
            setLastPickedTime(calculateLastPickTime());
            shop.mQuestFunction.refreshQuests(1);
        }
    }

    @Override
    public SInventoryItem getAdminSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.ENCHANTING_TABLE).setDisplayName(new SStringBuilder().yellow().text("クエストリフレッシュ設定").build());
        if(isFunctionEnabled()){
            item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(getRefreshMinutes()).text("分毎にクエスト更新").build());
        }else{
            item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text("なし").build());
        }

        item.addLore("");
        item.addLore("§f設定した分間毎にクエストを更新");
        item.addLore("§fどちらかが0の場合設定は無効化");

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);

        inventoryItem.setEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //confirmation menu
            sInventory.moveToMenu(player, getInnerSettingMenu(player, plugin));

        });


        return inventoryItem;
    }

    public AutoScaledMenu getInnerSettingMenu(Player player, Man10ShopV2 plugin){

        AutoScaledMenu autoScaledMenu = new AutoScaledMenu("クエストリフレッシュ設定", plugin);

        SInventoryItem timeSetting = new SInventoryItem(new SItemStack(Material.CLOCK).setDisplayName(new SStringBuilder().green().text("時間設定").build()).build());
        timeSetting.clickable(false);
        timeSetting.setEvent(e -> {

            NumericInputMenu menu = new NumericInputMenu("時間を入力してください 0はoff", plugin);
            menu.setOnConfirm(number -> {
                if(!shop.mQuestTimeWindowFunction.setRefreshMinutes(number)){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                    return;
                }
                player.sendMessage(Man10ShopV2.prefix + "§a§l時間を設定しました");
                menu.moveToMenu(player, getInnerSettingMenu(player, plugin));
            });
            menu.setOnCancel(ee -> menu.moveToMenu(player, getInnerSettingMenu(player, plugin)));
            menu.setOnClose(ee -> menu.moveToMenu(player, getInnerSettingMenu(player, plugin)));

            autoScaledMenu.moveToMenu(player, menu);
        });



        SInventoryItem setRefillStartingTime = new SInventoryItem(new SItemStack(Material.COMPASS)
                .setDisplayName(new SStringBuilder().green().text("最終値段選択時間を設定する").build())
                .addLore("§d§l現在設定: §e§l" + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(shop.mQuestTimeWindowFunction.getLastPickedTime()*1000L)))
                .build());
        setRefillStartingTime.clickable(false);
        setRefillStartingTime.setEvent(e -> {
            TimeSelectorMenu menu = new TimeSelectorMenu(System.currentTimeMillis()/1000L, "最終値段選択時間を設定してくださ", plugin);
            menu.setOnConfirm(lastRefillTime -> {
                if(lastRefillTime == -1L){
                    shop.mQuestTimeWindowFunction.deleteSetting("quest.refresh.lastRefreshTime");
                }else{
                    if(!shop.mQuestTimeWindowFunction.setLastPickedTime(lastRefillTime)){
                        player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                        return;
                    }
                }
                player.sendMessage(Man10ShopV2.prefix + "§a§l最新の補充開始時間を現在に設定しました");
                menu.moveToMenu(player, getInnerSettingMenu(player, plugin));
            });
            menu.setOnCloseEvent(ee -> menu.moveToMenu(player, getInnerSettingMenu(player, plugin)));
            autoScaledMenu.moveToMenu(player, menu);
        });

        autoScaledMenu.setOnCloseEvent(e -> autoScaledMenu.moveToMenu(player, new SettingsMainMenu(player, shop, shop.mQuestTimeWindowFunction.settingCategory(), plugin)));

        autoScaledMenu.addItem(timeSetting);
        autoScaledMenu.addItem(setRefillStartingTime);

        return autoScaledMenu;
    }
}
