package com.shojabon.man10shopv2.dataClass.shopFunctions.allowedToUse;

import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.menus.settings.innerSettings.WeekdayShopToggleMenu;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Calendar;
import java.util.UUID;

@ShopFunctionDefinition(
        name = "曜日有効化設定",
        explanation = {"特定の曜日にショップを有効かするかを設定する"},
        enabledShopType = {},
        iconMaterial = Material.COMPARATOR,
        category = "使用可条件設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = false
)
public class WeekDayToggleFunction extends ShopFunction {

    //variables

    //init
    public WeekDayToggleFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions

    public String weekToString(int week){
        return new String[]{"日曜日", "月曜日", "火曜日", "水曜日","木曜日", "金曜日", "土曜日"}[week];
    }

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
                    availableWeekDays.append(weekToString(i)).append(" ");
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
    public String currentSettingString() {
        String result = "";
        int i = 0;
        for(boolean res: getWeekdayShopToggle()){
            SStringBuilder builder = new SStringBuilder();
            builder.yellow().text(weekToString(i) + ": ");
            if(res){
                builder.green().text("有効");
            }else{
                builder.red().text("無効");
            }
            result = builder.build() + "\n";
            i++;
        }
        return result;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //confirmation menu
            WeekdayShopToggleMenu menu = new WeekdayShopToggleMenu(player, shop, plugin);

            menu.setAsyncOnCloseEvent(ee -> {
                if(!setWeekdayShopToggle(menu.states)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "曜日設定をしました");
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
            });
            menu.open(player);
        });

        return item;
    }
}
