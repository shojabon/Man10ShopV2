package com.shojabon.man10shopv2.DataClass.ShopFunctions;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Utils.BaseUtils;
import org.bukkit.entity.Player;

public class SingleTransactionModeFunction extends ShopFunction {

    //variables

    //init
    public SingleTransactionModeFunction(Man10Shop shop) {
        super(shop);
    }


    //functions


    //====================
    // settings
    //====================

    public boolean isSingleTransactionMode(){
        String currentSetting = getSetting("shop.transaction.single");
        if(!BaseUtils.isBoolean(currentSetting)) return false;
        return Boolean.parseBoolean(currentSetting);
    }

    public boolean setSingleSellMode(boolean enabled){
        if(isSingleTransactionMode() == enabled) return true;
        if(!setSetting("shop.transaction.single", enabled)) return false;
        Man10ShopV2API.closeInventoryGroup(shop.getShopId());
        return true;
    }


    @Override
    public boolean isAllowedToUseShop(Player p) {
        return true;
    }
}
