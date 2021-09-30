package com.shojabon.man10shopv2.DataClass.ShopFunctions;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Utils.BaseUtils;
import org.bukkit.entity.Player;

public class ShopEnabledFunction extends ShopFunction {

    //variables

    //init
    public ShopEnabledFunction(Man10Shop shop) {
        super(shop);
    }


    //functions


    //====================
    // settings
    //====================

    public boolean getShopEnabled(){
        String currentSetting = getSetting("shop.enabled");
        if(!BaseUtils.isBoolean(currentSetting)) return true;
        return Boolean.parseBoolean(currentSetting);
    }

    public boolean setShopEnabled(boolean enabled){
        if(getShopEnabled() == enabled) return true;
        if(!setSetting("shop.enabled", enabled)) return false;
        Man10ShopV2API.closeInventoryGroup(shop.getShopId());
        return true;
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        //shop disabled
        if(!getShopEnabled()){
            p.sendMessage(Man10ShopV2.prefix + "§c§l現在このショップは停止しています");
            return false;
        }
        return true;
    }
}
