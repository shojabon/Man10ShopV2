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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

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
    public Man10ShopSetting<Integer> time = new Man10ShopSetting<>("shop.perminute.cooldown.time", 0);
    public Man10ShopSetting<Integer> amount = new Man10ShopSetting<>("shop.perminute.cooldown.amount", 0);

    public Man10ShopSetting<Map<UUID, Long>> timeMap = new Man10ShopSetting<>("shop.perminute.cooldown.timeMap", new HashMap<>(), true);
    public Man10ShopSetting<Map<UUID, Integer>> countMap = new Man10ShopSetting<>("shop.perminute.cooldown.countMap", new HashMap<>(), true);

    //init
    public PerMinuteCoolDownFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }

    //functions

    public boolean checkPerMinuteCoolDown(Player p, int addingAmount){
        //if(!isFunctionEnabled())return false;
        if(addingAmount > amount.get()) return true;
        if(!countMap.get().containsKey(p.getUniqueId())) return false;

        boolean result = countMap.get().get(p.getUniqueId()) + addingAmount > amount.get();
        if(result){
            if(timeMap.get().get(p.getUniqueId()) + time.get()*60 <= System.currentTimeMillis()/1000L){
                timeMap.get().remove(p.getUniqueId());
                countMap.get().remove(p.getUniqueId());
                countMap.set(countMap.get());
                timeMap.set(timeMap.get());
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
        if(!countMap.get().containsKey(p.getUniqueId())) return amount.get();
        if(shop.isAdminShop()) return -(amount.get() - Math.toIntExact(countMap.get().get(p.getUniqueId())));
        return amount.get() - Math.toIntExact(countMap.get().get(p.getUniqueId()));

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
        if(!countMap.get().containsKey(p.getUniqueId())) countMap.get().put(p.getUniqueId(), 0);
        if(!timeMap.get().containsKey(p.getUniqueId())) timeMap.get().put(p.getUniqueId(), System.currentTimeMillis()/1000L);
        countMap.get().put(p.getUniqueId(), countMap.get().get(p.getUniqueId()) + amount);
        countMap.set(countMap.get());
        timeMap.set(timeMap.get());
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
                if(!time.set(number)){
                    warn(player , "内部エラーが発生しました");
                    return;
                }
                success(player , "時間を設定しました");
                getInnerSettingMenu(player, plugin).open(player);
            });
            menu.setOnCancel(eee -> getInnerSettingMenu(player, plugin).open(player));
            menu.setOnClose(eee -> getInnerSettingMenu(player, plugin).open(player));

            menu.open(player);
        });

        SInventoryItem amountSetting = new SInventoryItem(new SItemStack(Material.HOPPER).setDisplayName(new SStringBuilder().green().text("個数設定").build()).build());
        amountSetting.clickable(false);
        amountSetting.setEvent(ee -> {

            NumericInputMenu menu = new NumericInputMenu("個数を入力してください 0はoff", plugin);
            menu.setOnConfirm(number -> {
                if(!amount.set(number)){
                    warn(player , "内部エラーが発生しました");
                    return;
                }
                success(player , "個数を設定しました");
                getInnerSettingMenu(player, plugin).open(player);
            });
            menu.setOnCancel(eee -> getInnerSettingMenu(player, plugin).open(player));
            menu.setOnClose(eee -> getInnerSettingMenu(player, plugin).open(player));

            menu.open(player);
        });
        autoScaledMenu.setOnCloseEvent(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));


        autoScaledMenu.addItem(timeSetting);
        autoScaledMenu.addItem(amountSetting);
        return autoScaledMenu;
    }
}
