package com.shojabon.man10shopv2.dataClass.shopFunctions.tradeAmount;

import ToolMenu.AutoScaledMenu;
import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopLogObject;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;
@ShopFunctionDefinition(
        name = "分間毎ごとのクールダウン設定",
        explanation = {"取引を制限します", "分間毎の取引を設定した個数までとします", "プレイヤーごとではなくショップごとのクールダウン", "どちらかが0の場合設定は無効化"},
        enabledShopType = {},
        iconMaterial = Material.DROPPER,
        category = "取引量制限設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = false
)
public class TotalPerMinuteCoolDownFunction extends ShopFunction {

    //variables

    LinkedList<Man10ShopLogObject> perMinuteCoolDownMap = new LinkedList<>();
    public Man10ShopSetting<Integer> time = new Man10ShopSetting<>("shop.total.perminute.cooldown.time", 0);
    public Man10ShopSetting<Integer> amount = new Man10ShopSetting<>("shop.total.perminute.cooldown.amount", 0);

    //init
    public TotalPerMinuteCoolDownFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }

    @Override
    public void init() {
        loadTotalPerMinuteMap();
    }

    //functions

    public void loadTotalPerMinuteMap(){
        perMinuteCoolDownMap.clear();
        if(!isFunctionEnabled()){
            return;
        }

        ArrayList<MySQLCachedResultSet> result = Man10ShopV2.mysql.query("SELECT SUM(amount) AS amount,UNIX_TIMESTAMP(date_time) AS time FROM man10shop_trade_log WHERE shop_id = \"" + shop.getShopId() + "\" and UNIX_TIMESTAMP(date_time) >= UNIX_TIMESTAMP(CURRENT_TIMESTAMP()) - " + time.get()*60L + " GROUP BY YEAR(date_time), MONTH(date_time), DATE(date_time), HOUR(date_time), MINUTE(date_time) ORDER BY date_time DESC");
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
            if(currentTime - log.time >= time.get()* 60L) continue;
            totalAmountInTime += log.amount;
        }

        //delete unneeded logs
        for(int i = 0; i < perMinuteCoolDownMap.size(); i++){
            Man10ShopLogObject log = perMinuteCoolDownMap.getLast();
            if(currentTime - log.time >= time.get()* 60L) {
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
        if(addingAmount > amount.get()) return true;

        return totalPerMinuteCoolDownTotalAmountInTime() + addingAmount > amount.get();
    }

    //====================
    // settings
    //====================

    @Override
    public boolean isFunctionEnabled() {
        return time.get() != 0 && amount.get() != 0;
    }

    @Override
    public int itemCount(Player p) {
        if(!isFunctionEnabled()) return super.itemCount(p);
        if(shop.isAdminShop()) return -(amount.get() - totalPerMinuteCoolDownTotalAmountInTime());
        return amount.get() - totalPerMinuteCoolDownTotalAmountInTime();
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
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //confirmation menu
            getInnerSettingMenu(player, plugin).open(player);
        });
        return item;
    }

    public AutoScaledMenu getInnerSettingMenu(Player player, Man10ShopV2 plugin){
        AutoScaledMenu autoScaledMenu = new AutoScaledMenu("分間毎ごとの総クールダウン設定", plugin);

        SInventoryItem timeSetting = new SInventoryItem(new SItemStack(Material.CLOCK).setDisplayName(new SStringBuilder().green().text("時間設定").build()).build());
        timeSetting.clickable(false);
        timeSetting.setEvent(e -> {

            NumericInputMenu menu = new NumericInputMenu("時間を入力してください 0はoff", plugin);
            menu.setOnConfirm(number -> {
                if(!time.set(number)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "時間を設定しました");
                shop.totalPerMinuteCoolDown.loadTotalPerMinuteMap();
                getInnerSettingMenu(player, plugin).open(player);
            });
            menu.setOnCancel(ee -> getInnerSettingMenu(player, plugin).open(player));
            menu.setOnClose(ee -> getInnerSettingMenu(player, plugin).open(player));

            menu.open(player);;
        });

        SInventoryItem amountSetting = new SInventoryItem(new SItemStack(Material.HOPPER).setDisplayName(new SStringBuilder().green().text("個数設定").build()).build());
        amountSetting.clickable(false);
        amountSetting.setEvent(e -> {

            NumericInputMenu menu = new NumericInputMenu("個数を入力してください 0はoff", plugin);
            menu.setOnConfirm(number -> {
                if(!amount.set(number)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "個数を設定しました");
                getInnerSettingMenu(player, plugin).open(player);
            });
            menu.setOnCancel(ee -> getInnerSettingMenu(player, plugin).open(player));
            menu.setOnClose(ee -> getInnerSettingMenu(player, plugin).open(player));

            menu.open(player);;
        });
        autoScaledMenu.setOnCloseEvent(e -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));

        autoScaledMenu.addItem(timeSetting);
        autoScaledMenu.addItem(amountSetting);
        return autoScaledMenu;
    }


}
