package com.shojabon.man10shopv2.DataClass.ShopFunctions;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Utils.BaseUtils;
import org.bukkit.entity.Player;

public class StorageCapFunction extends ShopFunction {

    //variables

    //init
    public StorageCapFunction(Man10Shop shop) {
        super(shop);
    }


    //functions


    //====================
    // settings
    //====================

    public int getStorageCap(){
        String currentSetting = getSetting("storage.sell.cap");
        if(!BaseUtils.isInt(currentSetting)) return 0;
        return Integer.parseInt(currentSetting);
    }

    public boolean setStorageCap(int storageCap){
        if(getStorageCap() == storageCap) return true;
        return setSetting("storage.sell.cap", storageCap);
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        if(shop.getShopType() == Man10ShopType.SELL){
            //no money (sell)
            if(getStorageCap() != 0 && shop.storage.itemCount >= getStorageCap()){
                p.sendMessage(Man10ShopV2.prefix + "§c§l現在このショップは買取をしていません");
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isAllowedToUseShopWithAmount(Player p, int amount) {
        //if item storage hits storage cap
        if(shop.getShopType() == Man10ShopType.SELL){
            if(shop.storage.itemCount + amount > getStorageCap() && getStorageCap() != 0){
                p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップは現在買取を行っていません");
                return false;
            }
        }
        return true;
    }
}
