package com.shojabon.man10shopv2.shopFunctions;

import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import org.bukkit.entity.Player;

public class FunctionTemplate extends ShopFunction {

    //variables

    //init
    public FunctionTemplate(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions


    //====================
    // settings
    //====================


    @Override
    public boolean isAllowedToUseShop(Player p) {
        return true;
    }

    @Override
    public boolean isAllowedToUseShopWithAmount(Player p, int amount) {
        return true;
    }

}
