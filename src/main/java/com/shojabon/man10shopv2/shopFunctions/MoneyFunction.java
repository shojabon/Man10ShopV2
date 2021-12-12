package com.shojabon.man10shopv2.shopFunctions;

import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@ShopFunctionDefinition(
        name = "お金",
        explanation = {},
        enabledShopType = {},
        iconMaterial = Material.DROPPER,
        category = "その他",
        allowedPermission = Man10ShopPermission.ACCOUNTANT,
        isAdminSetting = false
)
public class MoneyFunction extends ShopFunction {

    //variables
    public int money;
    //init
    public MoneyFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions

    public int getMoney(){
        return money;
    }
    public boolean addMoney(int value){
        money = money + value;
        boolean result = Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET money = money + " + value + " WHERE shop_id = '" + shop.getShopId() + "'");
        if(!result) return false;
        //log here
        return true;
    }

    public boolean removeMoney(int value){
        money = money - value;
        boolean result = Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET money = money - " + value + " WHERE shop_id = '" + shop.getShopId() + "'");
        if(!result) return false;
        //log here
        return true;
    }

    //====================
    // settings
    //====================

    @Override
    public int itemCount(Player p) {
        if(shop.isAdminShop()) return 0;
        if(shop.shopType.getShopType() == Man10ShopType.SELL){
            if(shop.price.getPrice() == 0) return super.itemCount(p);
            return getMoney()/shop.price.getPrice();
        }
        return super.itemCount(p);
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        if(shop.shopType.getShopType() == Man10ShopType.SELL){
            if(money < shop.price.getPrice() && !shop.isAdminShop()){
                p.sendMessage(Man10ShopV2.prefix + "§c§lショップの残高が不足しています");
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isAllowedToUseShopWithAmount(Player p, int amount) {
        return true;
    }

}
