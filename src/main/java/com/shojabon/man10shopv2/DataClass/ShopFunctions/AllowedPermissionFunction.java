package com.shojabon.man10shopv2.DataClass.ShopFunctions;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Man10ShopV2;
import org.bukkit.entity.Player;

public class AllowedPermissionFunction extends ShopFunction {

    //variables

    //init
    public AllowedPermissionFunction(Man10Shop shop) {
        super(shop);
    }


    //functions


    //====================
    // settings
    //====================

    public String getAllowedPermission(){
        return getSetting("shop.permission.allowed");
    }

    public boolean setAllowedPermission(String permission){
        if(getAllowedPermission() != null) {
            if(getAllowedPermission().equalsIgnoreCase(permission)) return true;
        }
        if(permission.equalsIgnoreCase("")) return deleteSetting("shop.permission.allowed");
        return setSetting("shop.permission.allowed", permission);
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        //if player has permission
        if(getAllowedPermission() != null && !p.hasPermission("man10shopv2.use." + getAllowedPermission())){
            p.sendMessage(Man10ShopV2.prefix + "§c§lあなたはこのショップを使う権限がありません");
            return false;
        }
        return true;
    }
}
