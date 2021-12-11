package com.shojabon.man10shopv2.dataClass.shopFunctions.tradeAmount;

import ToolMenu.AutoScaledMenu;
import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopLogObject;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.MySQL.MySQLCachedResultSet;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
@ShopFunctionDefinition(
        name = "分間毎ごとのクールダウン設定",
        explanation = {"取引を制限します", "分間毎の取引を設定した個数までとします", "どちらかが0の場合設定は無効化"},
        enabledShopType = {},
        iconMaterial = Material.DISPENSER,
        category = "取引量制限設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = false
)
public class PerMinuteCoolDownFunction extends ShopFunction {

    //variables

    public HashMap<UUID, LinkedList<Man10ShopLogObject>> perMinuteCoolDownMap = new HashMap<>();

    //init
    public PerMinuteCoolDownFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
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
    public boolean afterPerformAction(Player p, int amount) {
        if(!isFunctionEnabled()) return true;
        addPerMinuteCoolDownLog(p.getUniqueId(), new Man10ShopLogObject(System.currentTimeMillis() / 1000L, amount));
        return true;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            getInnerSettingMenu(player, plugin).open(player);
        });
        return item;
    }

    public AutoScaledMenu getInnerSettingMenu(Player player, Man10ShopV2 plugin){
        AutoScaledMenu autoScaledMenu = new AutoScaledMenu("分間毎ごとのクールダウン設定", plugin);

        SInventoryItem timeSetting = new SInventoryItem(new SItemStack(Material.CLOCK).setDisplayName(new SStringBuilder().green().text("時間設定").build()).build());
        timeSetting.clickable(false);
        timeSetting.setEvent(ee -> {

            NumericInputMenu menu = new NumericInputMenu("時間を入力してください 0はoff", plugin);
            menu.setOnConfirm(number -> {
                if(!setPerMinuteCoolDownTime(number)){
                    warn(player , "内部エラーが発生しました");
                    return;
                }
                success(player , "時間を設定しました");
                loadPerMinuteMap();
                getInnerSettingMenu(player, plugin).open(player);
            });
            menu.setOnCancel(eee -> getInnerSettingMenu(player, plugin).open(player));
            menu.setOnClose(eee -> getInnerSettingMenu(player, plugin).open(player));

            autoScaledMenu.moveToMenu(player, menu);
        });

        SInventoryItem amountSetting = new SInventoryItem(new SItemStack(Material.HOPPER).setDisplayName(new SStringBuilder().green().text("個数設定").build()).build());
        amountSetting.clickable(false);
        amountSetting.setEvent(ee -> {

            NumericInputMenu menu = new NumericInputMenu("個数を入力してください 0はoff", plugin);
            menu.setOnConfirm(number -> {
                if(!setPerMinuteCoolDownAmount(number)){
                    warn(player , "内部エラーが発生しました");
                    return;
                }
                success(player , "個数を設定しました");
                getInnerSettingMenu(player, plugin).open(player);
            });
            menu.setOnCancel(eee -> getInnerSettingMenu(player, plugin).open(player));
            menu.setOnClose(eee -> getInnerSettingMenu(player, plugin).open(player));

            autoScaledMenu.moveToMenu(player, menu);
        });
        autoScaledMenu.setOnCloseEvent(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));


        autoScaledMenu.addItem(timeSetting);
        autoScaledMenu.addItem(amountSetting);
        return autoScaledMenu;
    }
}
