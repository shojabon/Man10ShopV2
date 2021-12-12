package com.shojabon.man10shopv2.dataClass.shopFunctions.mQuest;

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
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
@ShopFunctionDefinition(
        name = "クエストリフレッシュ設定",
        explanation = {"設定した分間毎にクエストを更新", "どちらかが0の場合設定は無効化"},
        enabledShopType = {Man10ShopType.QUEST},
        iconMaterial = Material.ENCHANTING_TABLE,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class MQuestTimeWindowFunction extends ShopFunction {

    //variables
    
    public Man10ShopSetting<Integer> minutes = new Man10ShopSetting<>("quest.refresh.time", 0);
    public Man10ShopSetting<Long> lastPickedTime = new Man10ShopSetting<>("quest.refresh.lastRefreshTime", 0L);
    
    //init
    public MQuestTimeWindowFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions

    public long calculateLastPickTime(){
        if(!isFunctionEnabled()) return -1;
        long secondsSinceLastRefill = System.currentTimeMillis()/1000L - lastPickedTime.get();
        long skippedRefills = secondsSinceLastRefill/(minutes.get()*60L);

        return lastPickedTime.get() + skippedRefills*minutes.get()*60L;
    }

    //====================
    // settings
    //====================
    

    @Override
    public boolean isFunctionEnabled(){
        return minutes.get() != 0;
    }

    @Override
    public void perMinuteExecuteTask() {
        if(System.currentTimeMillis()/1000L - lastPickedTime.get() >= minutes.get()*60L){
            //select random price
            lastPickedTime.set(calculateLastPickTime());
            shop.mQuestFunction.refreshQuests(shop.mQuestCountFunction.questCount.get());
        }
    }

    @Override
    public String currentSettingString() {
        return minutes.get() + "分毎にクエスト更新";
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

        AutoScaledMenu autoScaledMenu = new AutoScaledMenu("クエストリフレッシュ設定", plugin);

        SInventoryItem timeSetting = new SInventoryItem(new SItemStack(Material.CLOCK).setDisplayName(new SStringBuilder().green().text("時間設定").build()).build());
        timeSetting.clickable(false);
        timeSetting.setEvent(e -> {

            NumericInputMenu menu = new NumericInputMenu("時間を入力してください 0はoff", plugin);
            menu.setOnConfirm(number -> {
                if(!minutes.set(number)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "時間を設定しました");
               getInnerSettingMenu(player, plugin).open(player);
            });
            menu.setOnCancel(ee ->getInnerSettingMenu(player, plugin).open(player));
            menu.setOnClose(ee ->getInnerSettingMenu(player, plugin).open(player));

            menu.open(player);
        });



        SInventoryItem setRefillStartingTime = new SInventoryItem(new SItemStack(Material.COMPASS)
                .setDisplayName(new SStringBuilder().green().text("最終値段選択時間を設定する").build())
                .addLore("§d§l現在設定: §e§l" + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(shop.mQuestTimeWindowFunction.lastPickedTime.get()*1000L)))
                .build());
        setRefillStartingTime.clickable(false);
        setRefillStartingTime.setEvent(e -> {
            TimeSelectorMenu menu = new TimeSelectorMenu(System.currentTimeMillis()/1000L, "最終値段選択時間を設定してくださ", plugin);
            menu.setOnConfirm(lastRefillTime -> {
                if(lastRefillTime == -1L){
                    lastPickedTime.delete();
                }else{
                    if(!lastPickedTime.set(lastRefillTime)){
                        warn(player, "内部エラーが発生しました");
                        return;
                    }
                }
                success(player, "最新の補充開始時間を現在に設定しました");
               getInnerSettingMenu(player, plugin).open(player);
            });
            menu.setOnCloseEvent(ee ->getInnerSettingMenu(player, plugin).open(player));
            menu.open(player);
        });

        autoScaledMenu.setOnCloseEvent(e -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));

        autoScaledMenu.addItem(timeSetting);
        autoScaledMenu.addItem(setRefillStartingTime);

        return autoScaledMenu;
    }
}
