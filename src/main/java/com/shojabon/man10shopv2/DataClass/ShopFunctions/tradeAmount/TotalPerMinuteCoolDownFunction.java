package com.shojabon.man10shopv2.DataClass.ShopFunctions.tradeAmount;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.Man10ShopLogObject;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.PerMinuteCoolDownSelectorMenu;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.TotalPerMinuteCoolDownSelectorMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.MySQL.MySQLCachedResultSet;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;

public class TotalPerMinuteCoolDownFunction extends ShopFunction {

    //variables

    LinkedList<Man10ShopLogObject> perMinuteCoolDownMap = new LinkedList<>();

    //init
    public TotalPerMinuteCoolDownFunction(Man10Shop shop) {
        super(shop);
        loadTotalPerMinuteMap();
    }

    //functions

    public void loadTotalPerMinuteMap(){
        perMinuteCoolDownMap.clear();
        if(!isFunctionEnabled()){
            return;
        }

        ArrayList<MySQLCachedResultSet> result = Man10ShopV2.mysql.query("SELECT SUM(amount) AS amount,UNIX_TIMESTAMP(date_time) AS time FROM man10shop_trade_log WHERE shop_id = \"" + shop.getShopId() + "\" and UNIX_TIMESTAMP(date_time) >= UNIX_TIMESTAMP(CURRENT_TIMESTAMP()) - " + getTotalPerMinuteCoolDownTime()*60L + " GROUP BY YEAR(date_time), MONTH(date_time), DATE(date_time), HOUR(date_time), MINUTE(date_time) ORDER BY date_time DESC");
        for(MySQLCachedResultSet rs: result){
            addTotalPerMinuteCoolDownLog(new Man10ShopLogObject(rs.getLong("time"), rs.getInt("amount")));
        }
    }

    public void addTotalPerMinuteCoolDownLog(Man10ShopLogObject obj){
        perMinuteCoolDownMap.addFirst(obj);
    }

    public int totalPerMinuteCoolDownTotalAmountInTime(){
        if(!isFunctionEnabled()){
            return 0;
        }

        int totalAmountInTime = 0;

        long currentTime = System.currentTimeMillis() / 1000L;
        //count amount
        for(int i = 0; i < perMinuteCoolDownMap.size(); i++){
            Man10ShopLogObject log = perMinuteCoolDownMap.get(i);
            if(currentTime - log.time >= getTotalPerMinuteCoolDownTime()* 60L) continue;
            totalAmountInTime += log.amount;
        }

        //delete unneeded logs
        for(int i = 0; i < perMinuteCoolDownMap.size(); i++){
            Man10ShopLogObject log = perMinuteCoolDownMap.getLast();
            if(currentTime - log.time >= getTotalPerMinuteCoolDownTime()* 60L) {
                perMinuteCoolDownMap.removeLast();
            }else{
                break;
            }
        }
        return totalAmountInTime;
    }

    public boolean checkTotalPerMinuteCoolDown(int addingAmount){
        if(!isFunctionEnabled()){
            return false;
        }
        if(addingAmount > getTotalPerMinuteCoolDownAmount()) return true;

        return totalPerMinuteCoolDownTotalAmountInTime() + addingAmount > getTotalPerMinuteCoolDownAmount();
    }

    //====================
    // settings
    //====================

    public int getTotalPerMinuteCoolDownTime(){
        String currentSetting = getSetting("shop.total.perminute.cooldown.time");
        if(!BaseUtils.isInt(currentSetting)) return 0;
        return Integer.parseInt(currentSetting);
    }

    public boolean setTotalPerMinuteCoolDownTime(int time){
        if(getTotalPerMinuteCoolDownTime() == time) return true;
        return setSetting("shop.total.perminute.cooldown.time", time);
    }

    public int getTotalPerMinuteCoolDownAmount(){
        String currentSetting = getSetting("shop.total.perminute.cooldown.amount");
        if(!BaseUtils.isInt(currentSetting)) return 0;
        return Integer.parseInt(currentSetting);
    }

    public boolean setTotalPerMinuteCoolDownAmount(int amount){
        if(getTotalPerMinuteCoolDownAmount() == amount) return true;
        return setSetting("shop.total.perminute.cooldown.amount", amount);
    }

    @Override
    public String settingCategory() {
        return "取引量制限設定";
    }

    @Override
    public boolean hasPermissionToEdit(UUID uuid) {
        return shop.permission.hasPermissionAtLeast(uuid, Man10ShopPermission.MODERATOR);
    }

    @Override
    public boolean isFunctionEnabled() {
        return getTotalPerMinuteCoolDownTime() != 0 && getTotalPerMinuteCoolDownAmount() != 0;
    }

    @Override
    public int itemCount(Player p) {
        if(!isFunctionEnabled()) return super.itemCount(p);
        if(shop.isAdminShop()) return -(getTotalPerMinuteCoolDownAmount() - totalPerMinuteCoolDownTotalAmountInTime());
        return getTotalPerMinuteCoolDownAmount() - totalPerMinuteCoolDownTotalAmountInTime();
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        if(checkTotalPerMinuteCoolDown(1)){
            p.sendMessage(Man10ShopV2.prefix + "§c§l時間内ショップ取引上限に達しました");
            return false;
        }
        return true;
    }

    @Override
    public boolean isAllowedToUseShopWithAmount(Player p, int amount) {
        if(checkTotalPerMinuteCoolDown(amount)){
            p.sendMessage(Man10ShopV2.prefix + "§c§l時間内ショップ取引上限に達しました");
            return false;
        }
        return true;
    }

    @Override
    public boolean afterPerformAction(Player p, int amount) {
        if(!isFunctionEnabled()) return true;
        addTotalPerMinuteCoolDownLog(new Man10ShopLogObject(System.currentTimeMillis() / 1000L, amount));
        return true;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.DROPPER).setDisplayName(new SStringBuilder().yellow().text("分間毎ごとの総クールダウン設定").build());
        if(getTotalPerMinuteCoolDownAmount() != 0 && getTotalPerMinuteCoolDownTime() != 0){
            item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(getTotalPerMinuteCoolDownTime()).text("分毎に").text(getTotalPerMinuteCoolDownAmount()).text("個").build());
        }else{
            item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text("なし").build());
        }

        item.addLore("");
        item.addLore("§f取引を制限します");
        item.addLore("§f分間毎の取引を設定した個数までとします");
        item.addLore("§fプレイヤーごとではなくショップごとのクールダウン");
        item.addLore("§fどちらかが0の場合設定は無効化");

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);

        inventoryItem.setEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //confirmation menu
            sInventory.moveToMenu(player, new TotalPerMinuteCoolDownSelectorMenu(player, shop, plugin));

        });


        return inventoryItem;
    }


}
