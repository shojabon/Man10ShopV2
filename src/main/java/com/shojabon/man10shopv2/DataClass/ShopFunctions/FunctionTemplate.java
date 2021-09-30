package com.shojabon.man10shopv2.DataClass.ShopFunctions;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Utils.BaseUtils;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class FunctionTemplate extends ShopFunction {

    //variables

    //init
    public FunctionTemplate(Man10Shop shop) {
        super(shop);
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
