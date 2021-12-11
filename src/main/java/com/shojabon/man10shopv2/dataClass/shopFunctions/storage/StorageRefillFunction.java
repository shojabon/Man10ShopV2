package com.shojabon.man10shopv2.dataClass.shopFunctions.storage;

import ToolMenu.AutoScaledMenu;
import ToolMenu.NumericInputMenu;
import ToolMenu.TimeSelectorMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
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
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
@ShopFunctionDefinition(
        name = "分間ごとの補充設定",
        explanation = {"取引を制限します", "分間毎の取引を設定した個数までとします", "どちらかが0の場合設定は無効化"}, 
        enabledShopType = {},
        iconMaterial = Material.CHEST_MINECART,
        category = "倉庫設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = false
)
public class StorageRefillFunction extends ShopFunction {

    //variables

    //init
    public StorageRefillFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions

    public boolean checkCanTrade(int amount){
        if(!isFunctionEnabled()) return false;
        if(transactionsLeft() < amount) return false;
        return true;
    }

    public int transactionsLeft(){
        if(System.currentTimeMillis()/1000L - getLastRefillTime() >= getRefillTimeMinute()*60L){
            //refill
            setLastRefillTime(calculateLastRefillTime());
            setItemLeft(getRefillAmount());
        }
        return getItemLeft();
    }

    public long calculateLastRefillTime(){
        long secondsSinceLastRefill = System.currentTimeMillis()/1000L - getLastRefillTime();
        long skippedRefills = secondsSinceLastRefill/(getRefillTimeMinute()*60L);

        return getLastRefillTime() + skippedRefills*getRefillTimeMinute()*60L;
    }

    public long getNextRefillTime(){
        if(!isFunctionEnabled()) return 0;
        if(checkCanTrade(1)) return 0;
        return getLastRefillTime() + getRefillTimeMinute()*60L;
    }

    public String getNextRefillTimeString(){
        long unixSeconds = getNextRefillTime();
        if(unixSeconds == 0) return "";
        Date date = new java.util.Date(unixSeconds*1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    //====================
    // settings
    //====================

    public int getRefillAmount(){
        String currentSetting = getSetting("storage.refill.amount");
        if(!BaseUtils.isInt(currentSetting)) return 0;
        return Integer.parseInt(currentSetting);
    }

    public boolean setRefillAmount(int amount){
        if(getRefillAmount() == amount) return true;
        return setSetting("storage.refill.amount", amount);
    }

    public int getRefillTimeMinute(){
        String currentSetting = getSetting("storage.refill.time");
        if(!BaseUtils.isInt(currentSetting)) return 0;
        return Integer.parseInt(currentSetting);
    }

    public boolean setRefillTimeMinute(int time){
        if(getRefillTimeMinute() == time) return true;
        return setSetting("storage.refill.time", time);
    }

    public long getLastRefillTime(){
        String currentSetting = getSetting("storage.refill.lastRefillTime");
        if(!BaseUtils.isLong(currentSetting)) return 0L;
        return Long.parseLong(currentSetting);
    }

    public boolean setLastRefillTime(long time){
        if(getLastRefillTime() == time) return true;
        return setSetting("storage.refill.lastRefillTime", time);
    }

    public int getItemLeft(){
        String currentSetting = getSetting("storage.refill.itemFilled");
        if(!BaseUtils.isInt(currentSetting)) return 0;
        return Integer.parseInt(currentSetting);
    }

    public boolean setItemLeft(int storageCap){
        if(getItemLeft() == storageCap) return true;
        return setSetting("storage.refill.itemFilled", storageCap);
    }
    

    @Override
    public boolean isFunctionEnabled(){
        return getRefillAmount() != 0 && getRefillTimeMinute() != 0;
    }

    @Override
    public int itemCount(Player p) {
        if(!isFunctionEnabled()) return super.itemCount(p);
        if(shop.isAdminShop()) return -getItemLeft();
        return getItemLeft();
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        return isAllowedToUseShopWithAmount(p, 1);
    }

    @Override
    public boolean isAllowedToUseShopWithAmount(Player p, int amount) {
        if(!checkCanTrade(amount)){
            if(shop.storage.getItemCount() == 0 && !shop.isAdminShop()) {
                p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップは在庫不足です");
                return false;
            }
            if(shop.shopType.getShopType() == Man10ShopType.SELL){
                p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップは買取を停止しています 次回の売却は " + getNextRefillTimeString());
            }else{
                p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップは品切れです 次の入荷は " + getNextRefillTimeString());
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean afterPerformAction(Player p, int amount) {
        if(!isFunctionEnabled()) return true;
        return setItemLeft(getItemLeft()-amount);
    }

    @Override
    public String currentSettingString() {
        return getRefillTimeMinute() + "分毎に" + getRefillAmount() + "個補充";
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
        AutoScaledMenu autoScaledMenu = new AutoScaledMenu("分間毎ごとの倉庫補充設定", plugin);

        SInventoryItem timeSetting = new SInventoryItem(new SItemStack(Material.CLOCK).setDisplayName(new SStringBuilder().green().text("時間設定").build()).build());
        timeSetting.clickable(false);
        timeSetting.setEvent(e -> {

            NumericInputMenu menu = new NumericInputMenu("時間を入力してください 0はoff", plugin);
            menu.setOnConfirm(number -> {
                if(!shop.storageRefill.setRefillTimeMinute(number)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "時間を設定しました");
                getInnerSettingMenu(player, plugin);
            });
            menu.setOnCancel(ee -> getInnerSettingMenu(player, plugin));
            menu.setOnClose(ee -> getInnerSettingMenu(player, plugin));

            autoScaledMenu.moveToMenu(player, menu);
        });


        SInventoryItem setRefillStartingTime = new SInventoryItem(new SItemStack(Material.COMPASS)
                .setDisplayName(new SStringBuilder().green().text("最終補充時間を設定する").build())
                .addLore("§d§l現在設定: §e§l" + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(shop.storageRefill.getLastRefillTime()*1000L)))
                .build());
        setRefillStartingTime.clickable(false);
        setRefillStartingTime.setEvent(e -> {
            TimeSelectorMenu menu = new TimeSelectorMenu(System.currentTimeMillis()/1000L, "最終補充時間を設定してくださ", plugin);
            menu.setOnConfirm(lastRefillTime -> {
                if(lastRefillTime == -1L){
                    shop.storageRefill.deleteSetting("storage.refill.lastRefillTime");
                }else{
                    if(!shop.storageRefill.setLastRefillTime(lastRefillTime)){
                        warn(player, "内部エラーが発生しました");
                        return;
                    }
                }
                success(player, "最新の補充開始時間を現在に設定しました");
                getInnerSettingMenu(player, plugin);
            });
            menu.setOnCloseEvent(ee -> getInnerSettingMenu(player, plugin));
            autoScaledMenu.moveToMenu(player, menu);
        });


        SInventoryItem forceRefill = new SInventoryItem(new SItemStack(Material.REDSTONE_BLOCK).setDisplayName(new SStringBuilder().green().text("強制的に在庫を補充する").build())
                .addLore("§f補充スケジュールは保持したままアイテムを補充する").build());
        forceRefill.clickable(false);
        forceRefill.setEvent(e -> {
            if(!shop.storageRefill.setItemLeft(shop.storageRefill.getRefillAmount())){
                warn(player, "内部エラーが発生しました");
                return;
            }
            success(player, "在庫を補充しました");
        });


        SInventoryItem amountSetting = new SInventoryItem(new SItemStack(Material.HOPPER).setDisplayName(new SStringBuilder().green().text("個数設定").build()).build());
        amountSetting.clickable(false);
        amountSetting.setEvent(e -> {

            NumericInputMenu menu = new NumericInputMenu("個数を入力してください 0はoff", plugin);
            menu.setOnConfirm(number -> {
                if(!shop.storageRefill.setRefillAmount(number)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "個数を設定しました");
                getInnerSettingMenu(player, plugin);
            });
            menu.setOnCancel(ee -> getInnerSettingMenu(player, plugin));
            menu.setOnClose(ee -> getInnerSettingMenu(player, plugin));

            autoScaledMenu.moveToMenu(player, menu);
        });

        autoScaledMenu.setOnCloseEvent(e -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));

        autoScaledMenu.addItem(timeSetting);
        autoScaledMenu.addItem(amountSetting);
        autoScaledMenu.addItem(setRefillStartingTime);
        autoScaledMenu.addItem(forceRefill);
        
        return autoScaledMenu;
    }
}
