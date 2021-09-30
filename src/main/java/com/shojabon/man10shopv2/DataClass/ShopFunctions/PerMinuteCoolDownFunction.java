package com.shojabon.man10shopv2.DataClass.ShopFunctions;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.Man10ShopLogObject;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Utils.BaseUtils;
import com.shojabon.man10shopv2.Utils.MySQL.MySQLCachedResultSet;
import org.bukkit.entity.Player;

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

    public void loadPerMinuteMap(){
        perMinuteCoolDownMap.clear();
        if(getPerMinuteCoolDownTime() == 0 || getPerMinuteCoolDownAmount() == 0){
            return;
        }

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

    public int perMinuteCoolDownTotalAmountInTime(Player p){
        if(getPerMinuteCoolDownTime() == 0 || getPerMinuteCoolDownAmount() == 0){
            return 0;
        }

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
        if(getPerMinuteCoolDownTime() == 0 || getPerMinuteCoolDownAmount() == 0){
            return false;
        }

        if(!perMinuteCoolDownMap.containsKey(p.getUniqueId())){
            if(addingAmount > getPerMinuteCoolDownAmount()) return true;//if not trade within time and amount is bigger than limit
            return false;
        }

        return perMinuteCoolDownTotalAmountInTime(p) + addingAmount > getPerMinuteCoolDownAmount();
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
    public boolean isAllowedToUseShop(Player p) {
        if(checkPerMinuteCoolDown(p, 1)){
            p.sendMessage(Man10ShopV2.prefix + "§c§l時間内の最大取引数に達しました");
            return false;
        }
        return true;
    }
}