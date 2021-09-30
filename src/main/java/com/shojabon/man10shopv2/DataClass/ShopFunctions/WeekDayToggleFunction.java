package com.shojabon.man10shopv2.DataClass.ShopFunctions;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Utils.BaseUtils;
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
}
