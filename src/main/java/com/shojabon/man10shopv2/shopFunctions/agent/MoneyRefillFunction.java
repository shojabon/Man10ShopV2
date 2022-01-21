package com.shojabon.man10shopv2.shopFunctions.agent;

import ToolMenu.AutoScaledMenu;
import ToolMenu.NumericInputMenu;
import ToolMenu.TimeSelectorMenu;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

@ShopFunctionDefinition(
        name = "分間ごとの現金補充設定",
        explanation = {},
        enabledShopType = {Man10ShopType.BUY, Man10ShopType.SELL},
        iconMaterial = Material.EMERALD_ORE,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = false
)
public class MoneyRefillFunction extends ShopFunction {

    //variables
    public Man10ShopSetting<Integer> refillAmount = new Man10ShopSetting<>("money.refill.amount", 0);
    public Man10ShopSetting<Integer> refillMinutes = new Man10ShopSetting<>("money.refill.time", 0);
    public Man10ShopSetting<Long> lastRefillTime = new Man10ShopSetting<>("money.refill.lastRefillTime", 0L);


    //init
    public MoneyRefillFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions

    public long calculateLastRefillTime(){
        long secondsSinceLastRefill = System.currentTimeMillis()/1000L - lastRefillTime.get();
        long skippedRefills = secondsSinceLastRefill/(refillMinutes.get()*60L);

        return lastRefillTime.get() + skippedRefills*refillMinutes.get()*60L;
    }

    public void calculateMoneyToRefill(){
        if(System.currentTimeMillis()/1000L - lastRefillTime.get() >= refillMinutes.get()*60L){
            //refill
            lastRefillTime.set(calculateLastRefillTime());
            shop.money.setMoney(refillAmount.get());
        }
    }

    @Override
    public void perMinuteExecuteTask() {
        calculateMoneyToRefill();
    }

    @Override
    public String currentSettingString() {
        return refillMinutes.get() + "分毎に" + BaseUtils.priceString(refillAmount.get()) + "円に補充";
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        if(!player.hasPermission("man10shopv2.admin.agent")) return null;
        item.setEvent(e -> {
            //confirmation menu
            getInnerSettingMenu(player, plugin).open(player);
        });
        return item;
    }
    
    public AutoScaledMenu getInnerSettingMenu(Player player, Man10ShopV2 plugin){
        if(!player.hasPermission("man10shopv2.admin.agent")) return null;
        AutoScaledMenu autoScaledMenu = new AutoScaledMenu("分間毎ごとの現金補充設定", plugin);

        SInventoryItem timeSetting = new SInventoryItem(new SItemStack(Material.CLOCK).setDisplayName(new SStringBuilder().green().text("時間設定").build()).build());
        timeSetting.clickable(false);
        timeSetting.setEvent(e -> {

            NumericInputMenu menu = new NumericInputMenu("時間を入力してください 0はoff", plugin);
            menu.setOnConfirm(number -> {
                if(!refillMinutes.set(number)){
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


        SInventoryItem setRefillStartingTime = new SInventoryItem(new SItemStack(Material.COMPASS)
                .setDisplayName(new SStringBuilder().green().text("最終補充時間を設定する").build())
                .addLore("§d§l現在設定: §e§l" + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(lastRefillTime.get()*1000L)))
                .build());
        setRefillStartingTime.clickable(false);
        setRefillStartingTime.setEvent(e -> {
            TimeSelectorMenu menu = new TimeSelectorMenu(System.currentTimeMillis()/1000L, "最終補充時間を設定してくださ", plugin);
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


        SInventoryItem forceRefill = new SInventoryItem(new SItemStack(Material.REDSTONE_BLOCK).setDisplayName(new SStringBuilder().green().text("強制的に在庫を補充する").build())
                .addLore("§f補充スケジュールは保持したままアイテムを補充する").build());
        forceRefill.clickable(false);
        forceRefill.setEvent(e -> {
            if(!shop.money.setMoney(refillAmount.get())){
                warn(player, "内部エラーが発生しました");
                return;
            }
            success(player, "在庫を補充しました");
        });


        SInventoryItem amountSetting = new SInventoryItem(new SItemStack(Material.HOPPER).setDisplayName(new SStringBuilder().green().text("金額設定").build()).build());
        amountSetting.clickable(false);
        amountSetting.setEvent(e -> {

            NumericInputMenu menu = new NumericInputMenu("金額を入力してください 0はoff", plugin);
            menu.setOnConfirm(number -> {
                if(!refillAmount.set(number)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "金額を設定しました");
                getInnerSettingMenu(player, plugin).open(player);
            });
            menu.setOnCancel(ee -> getInnerSettingMenu(player, plugin).open(player));
            menu.setOnClose(ee -> getInnerSettingMenu(player, plugin).open(player));

            menu.open(player);
        });

        autoScaledMenu.setOnCloseEvent(e -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));

        autoScaledMenu.addItem(timeSetting);
        autoScaledMenu.addItem(amountSetting);
        autoScaledMenu.addItem(setRefillStartingTime);
        autoScaledMenu.addItem(forceRefill);
        
        return autoScaledMenu;
    }
}
