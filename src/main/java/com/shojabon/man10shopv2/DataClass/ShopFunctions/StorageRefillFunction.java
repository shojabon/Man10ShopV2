package com.shojabon.man10shopv2.DataClass.ShopFunctions;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.PerMinuteCoolDownSelectorMenu;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.StorageRefillMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StorageRefillFunction extends ShopFunction {

    //variables

    //init
    public StorageRefillFunction(Man10Shop shop) {
        super(shop);
    }


    //functions

    public boolean isFunctionEnabled(){
        return getRefillAmount() != 0 && getRefillTimeMinute() != 0;
    }

    public boolean checkCanTrade(int amount){
        if(!isFunctionEnabled()) return false;
        if(transactionsLeft() < amount) return false;
        return true;
    }

    public int transactionsLeft(){
        if(System.currentTimeMillis()/1000L - getLastRefillTime() >= getRefillTimeMinute()*60L){
            //refill
            setLastRefillTime(System.currentTimeMillis()/1000L);
            setItemLeft(getRefillAmount());
        }
        return getItemLeft();
    }

    public long getNextRefillTime(){
        if(!isFunctionEnabled()) return 0;
        if(checkCanTrade(1)) return 0;
        return getLastRefillTime() + getRefillTimeMinute()*60L;
    }

    public String getNextRefillTimeString(){
        long unixSeconds = getNextRefillTime();
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
    public boolean isAllowedToUseShop(Player p) {
        if(!isFunctionEnabled()) return true;
        if(!checkCanTrade(1)){
            p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップは品切れです 次の入荷は " + getNextRefillTimeString());
            return false;
        }
        return true;
    }

    @Override
    public boolean isAllowedToUseShopWithAmount(Player p, int amount) {
        if(!isFunctionEnabled()) return true;
        if(!checkCanTrade(amount)){
            p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップは品切れです 次の入荷は " + getNextRefillTimeString());
            return false;
        }
        return true;
    }

    @Override
    public void performAction(Player p, int amount) {
        if(!isFunctionEnabled()) return;
        setItemLeft(getItemLeft()-amount);
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.CHEST_MINECART).setDisplayName(new SStringBuilder().yellow().text("分間ごとの補充設定").build());
        if(isFunctionEnabled()){
            item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(getRefillTimeMinute()).text("分毎に").text(getRefillAmount()).text("個に補充").build());
        }else{
            item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text("なし").build());
        }

        item.addLore("");
        item.addLore("§f取引を制限します");
        item.addLore("§f分間毎の取引を設定した個数までとします");
        item.addLore("§fどちらかが0の場合設定は無効化");

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);

        inventoryItem.setEvent(e -> {
            if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //confirmation menu
            sInventory.moveToMenu(player, new StorageRefillMenu(player, shop, plugin));

        });


        return inventoryItem;
    }
}
