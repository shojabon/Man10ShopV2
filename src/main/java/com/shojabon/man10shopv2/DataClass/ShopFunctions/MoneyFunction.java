package com.shojabon.man10shopv2.DataClass.ShopFunctions;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MoneyFunction extends ShopFunction {

    //variables
    public int money;
    //init
    public MoneyFunction(Man10Shop shop) {
        super(shop);
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
    public boolean hasPermissionToEdit(UUID uuid) {
        if(!shop.permission.hasPermissionAtLeast(uuid, Man10ShopPermission.ACCOUNTANT)) return false;
        if(shop.permission.hasPermission(uuid, Man10ShopPermission.STORAGE_ACCESS)) return false;
        return true;
    }

    @Override
    public int itemCount(Player p) {
        if(shop.isAdminShop()) return 0;
        if(shop.shopType.getShopType() == Man10ShopType.SELL){
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
