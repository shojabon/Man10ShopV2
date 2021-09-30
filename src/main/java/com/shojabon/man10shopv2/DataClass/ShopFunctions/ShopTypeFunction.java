package com.shojabon.man10shopv2.DataClass.ShopFunctions;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import org.bukkit.entity.Player;

public class ShopTypeFunction extends ShopFunction {

    //variables
    public Man10ShopType shopType;

    //init
    public ShopTypeFunction(Man10Shop shop) {
        super(shop);
    }

    public Man10ShopType getShopType() {
        return shopType;
    }

    public boolean setShopType(Man10ShopType type){
        shopType = type;
        if(!Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET shop_type ='" + type.name() + "' WHERE shop_id = '" + shop.getShopId() + "'")){
            return false;
        }
        Man10ShopV2API.closeInventoryGroup(shop.getShopId());
        return true;
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
