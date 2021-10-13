package com.shojabon.man10shopv2.DataClass.ShopFunctions.tradeAmount;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.Man10ShopLogObject;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.PerMinuteCoolDownSelectorMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.MySQL.MySQLCachedResultSet;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

public class PerMinuteCoolDownFunction extends ShopFunction {

    //variables

    public HashMap<UUID, LinkedList<Man10ShopLogObject>> perMinuteCoolDownMap = new HashMap<>();

    //init
    public PerMinuteCoolDownFunction(Man10Shop shop) {
        super(shop);
        loadPerMinuteMap();
    }

    //functions

    public void loadPerMinuteMap(){
        perMinuteCoolDownMap.clear();
        if(!isFunctionEnabled()) return;

        ArrayList<MySQLCachedResultSet> result = Man10ShopV2.mysql.query("SELECT SUM(amount) AS amount,uuid,UNIX_TIMESTAMP(date_time) AS time FROM man10shop_trade_log WHERE shop_id = \"" + shop.getShopId() + "\" and UNIX_TIMESTAMP(date_time) >= UNIX_TIMESTAMP(CURRENT_TIMESTAMP()) - " + getPerMinuteCoolDownTime()*60L + " GROUP BY UUID, YEAR(date_time), MONTH(date_time), DATE(date_time), HOUR(date_time), MINUTE(date_time) ORDER BY date_time DESC");
        for(MySQLCachedResultSet rs: result){
            addPerMinuteCoolDownLog(UUID.fromString(rs.getString("uuid")), new Man10ShopLogObject(rs.getLong("time"), rs.getInt("amount")));
        }
    }

    public void addPerMinuteCoolDownLog(UUID uuid, Man10ShopLogObject obj){
        if(!perMinuteCoolDownMap.containsKey(uuid)){
            perMinuteCoolDownMap.put(uuid, new LinkedList<>());
        }
        perMinuteCoolDownMap.get(uuid).addFirst(obj);
    }

    public int perMinuteCoolDownAmountInTime(Player p){
        if(!isFunctionEnabled())return 0;

        if(!perMinuteCoolDownMap.containsKey(p.getUniqueId())){
            return 0;
        }
        int totalAmountInTime = 0;

        LinkedList<Man10ShopLogObject> logs = perMinuteCoolDownMap.get(p.getUniqueId());
        long currentTime = System.currentTimeMillis() / 1000L;
        //count amount
        for(int i = 0; i < logs.size(); i++){
            Man10ShopLogObject log = logs.get(i);
            if(currentTime - log.time >= getPerMinuteCoolDownTime()* 60L) continue;
            totalAmountInTime += log.amount;
        }

        //delete unneeded logs
        for(int i = 0; i < logs.size(); i++){
            Man10ShopLogObject log = logs.getLast();
            if(currentTime - log.time >= getPerMinuteCoolDownTime()* 60L) {
                logs.removeLast();
            }else{
                break;
            }
        }
        return totalAmountInTime;
    }

    public boolean checkPerMinuteCoolDown(Player p, int addingAmount){
        if(!isFunctionEnabled())return false;

        if(!perMinuteCoolDownMap.containsKey(p.getUniqueId())){
            if(addingAmount > getPerMinuteCoolDownAmount()) return true;//if not trade within time and amount is bigger than limit
            return false;
        }

        return perMinuteCoolDownAmountInTime(p) + addingAmount > getPerMinuteCoolDownAmount();
    }

    //====================
    // settings
    //====================

    public int getPerMinuteCoolDownTime(){
        String currentSetting = getSetting("shop.perminute.cooldown.time");
        if(!BaseUtils.isInt(currentSetting)) return 0;
        return Integer.parseInt(currentSetting);
    }

    public boolean setPerMinuteCoolDownTime(int time){
        if(getPerMinuteCoolDownTime() == time) return true;
        return setSetting("shop.perminute.cooldown.time", time);
    }

    public int getPerMinuteCoolDownAmount(){
        String currentSetting = getSetting("shop.perminute.cooldown.amount");
        if(!BaseUtils.isInt(currentSetting)) return 0;
        return Integer.parseInt(currentSetting);
    }

    public boolean setPerMinuteCoolDownAmount(int amount){
        if(getPerMinuteCoolDownAmount() == amount) return true;
        return setSetting("shop.perminute.cooldown.amount", amount);
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
        return getPerMinuteCoolDownTime() != 0 && getPerMinuteCoolDownAmount() != 0;
    }

    @Override
    public int itemCount(Player p) {
        if(!isFunctionEnabled()) return super.itemCount(p);
        if(shop.isAdminShop()) return -(getPerMinuteCoolDownAmount() - perMinuteCoolDownAmountInTime(p));
        return getPerMinuteCoolDownAmount() - perMinuteCoolDownAmountInTime(p);

    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        if(checkPerMinuteCoolDown(p, 1)){
            p.sendMessage(Man10ShopV2.prefix + "§c§l時間内の最大取引数に達しました");
            return false;
        }
        return true;
    }

    @Override
    public boolean isAllowedToUseShopWithAmount(Player p, int amount) {
        if(checkPerMinuteCoolDown(p, amount)){
            p.sendMessage(Man10ShopV2.prefix + "§c§l時間内の最大取引数に達しました");
            return false;
        }
        return true;
    }

    @Override
    public boolean performAction(Player p, int amount) {
        if(!isFunctionEnabled()) return true;
        addPerMinuteCoolDownLog(p.getUniqueId(), new Man10ShopLogObject(System.currentTimeMillis() / 1000L, amount));
        return true;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.DISPENSER).setDisplayName(new SStringBuilder().yellow().text("分間毎ごとのクールダウン設定").build());
        if(getPerMinuteCoolDownAmount() != 0 && getPerMinuteCoolDownTime() != 0){
            item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(getPerMinuteCoolDownTime()).text("分毎に").text(getPerMinuteCoolDownAmount()).text("個").build());
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
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //confirmation menu
            sInventory.moveToMenu(player, new PerMinuteCoolDownSelectorMenu(player, shop, plugin));

        });


        return inventoryItem;
    }
}
