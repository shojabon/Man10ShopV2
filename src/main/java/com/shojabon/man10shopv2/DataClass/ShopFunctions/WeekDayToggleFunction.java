package com.shojabon.man10shopv2.DataClass.ShopFunctions;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.WeekdayShopToggleMenu;
import com.shojabon.man10shopv2.Menus.Settings.SettingsMainMenu;
import com.shojabon.man10shopv2.Utils.BaseUtils;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Calendar;

public class WeekDayToggleFunction extends ShopFunction {

    //variables

    //init
    public WeekDayToggleFunction(Man10Shop shop) {
        super(shop);
    }


    //functions


    //====================
    // settings
    //====================


    public boolean[] getWeekdayShopToggle(){
        String current = getSetting("shop.weekday.toggle");
        boolean[] results = new boolean[7];
        Arrays.fill(results, true);
        if(current == null) return results;
        for(int i = 0; i < current.length(); i++){
            results[i] = current.charAt(i) == '1';
        }
        return results;
    }

    public boolean setWeekdayShopToggle(boolean[] results){
        StringBuilder result = new StringBuilder();
        for(boolean res: results){
            if(res) {
                result.append("1");
            }else{
                result.append("0");
            }
        }
        if(Arrays.equals(getWeekdayShopToggle(), results)) return true;
        return setSetting("shop.weekday.toggle", result.toString());
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        //weekday toggle
        if(!getWeekdayShopToggle()[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1]){
            p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップを本日ご利用することはできません");
            StringBuilder availableWeekDays = new StringBuilder();
            int i = 0;
            for(boolean enabled: getWeekdayShopToggle()){
                if(enabled){
                    availableWeekDays.append(BaseUtils.weekToString(i)).append(" ");
                }
                i++;
            }
            if(availableWeekDays.toString().length() != 0){
                p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップは" + availableWeekDays.substring(0, availableWeekDays.length()-1) + "に利用することができます");
            }
            return false;
        }
        return true;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.COMPARATOR).setDisplayName(new SStringBuilder().red().text("曜日有効化設定").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定").build());
        int i = 0;
        for(boolean res: getWeekdayShopToggle()){
            SStringBuilder builder = new SStringBuilder();
            builder.yellow().text(BaseUtils.weekToString(i) + ": ");
            if(res){
                builder.green().text("有効");
            }else{
                builder.red().text("無効");
            }
            item.addLore(builder.build());
            i++;
        }

        item.addLore("");
        item.addLore("§f特定の曜日にショップを有効かするかを設定する");


        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);

        inventoryItem.setEvent(e -> {
            if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //confirmation menu
            WeekdayShopToggleMenu menu = new WeekdayShopToggleMenu(player, shop, plugin);

            menu.setAsyncOnCloseEvent(ee -> {
                if(setWeekdayShopToggle(menu.states)){
                    Man10ShopV2API.log(shop.getShopId(), "setWeekdayShopToggle", getWeekdayShopToggle(), player.getName(), player.getUniqueId()); //log
                }
                player.sendMessage(Man10ShopV2.prefix + "§a§l曜日設定をしました");
                sInventory.moveToMenu(player, new SettingsMainMenu(player, shop, plugin));
            });

            sInventory.moveToMenu(player, menu);

        });

        return inventoryItem;
    }
}
