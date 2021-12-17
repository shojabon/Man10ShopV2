package com.shojabon.man10shopv2.shopFunctions.tradeAmount;

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
import com.shojabon.mcutils.Utils.MySQL.MySQLCachedResultSet;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;

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

    public Man10ShopSetting<Integer> time = new Man10ShopSetting<>("shop.total.perminute.cooldown.time", 0);
    public Man10ShopSetting<Integer> amount = new Man10ShopSetting<>("shop.total.perminute.cooldown.amount", 0);

    public Man10ShopSetting<Long> oldestTransactionTime = new Man10ShopSetting<>("shop.total.perminute.cooldown.oldestTransactionTime", 0L, true);
    public Man10ShopSetting<Integer> currentTransactionCount = new Man10ShopSetting<>("shop.total.perminute.cooldown.current", 0, true);

    //init
    public TotalPerMinuteCoolDownFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    public boolean checkTotalPerMinuteCoolDown(int addingAmount){
//        if(!isFunctionEnabled()){
//            return false;
//        }
        if(addingAmount > amount.get()) return true;

        boolean result = currentTransactionCount.get() + addingAmount > amount.get();
        if(result){
            if(oldestTransactionTime.get() + time.get()*60 <= System.currentTimeMillis()/1000L){
                oldestTransactionTime.delete();
                currentTransactionCount.delete();
                return false;
            }
        }

        return result;
    }

    //====================
    // settings
    //====================


    @Override
    public String currentSettingString() {
        return time.get() + "分" + amount.get() + "まで";
    }

    @Override
    public int itemCount(Player p) {
        //if(!isFunctionEnabled()) return super.itemCount(p);
        if(shop.isAdminShop()) return -(amount.get() - currentTransactionCount.get());
        return amount.get() - currentTransactionCount.get();
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
        //if(!isFunctionEnabled()) return true;
        if(currentTransactionCount.get() == 0) oldestTransactionTime.set(System.currentTimeMillis()/1000L);
        currentTransactionCount.set(currentTransactionCount.get() + amount);
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
