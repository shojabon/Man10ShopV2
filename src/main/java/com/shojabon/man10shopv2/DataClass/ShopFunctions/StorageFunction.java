package com.shojabon.man10shopv2.DataClass.ShopFunctions;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Utils.BaseUtils;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;

public class StorageFunction extends ShopFunction {
    //variables
    public int storageSize;
    public int itemCount;

    //init
    public StorageFunction(Man10Shop shop) {
        super(shop);
        storageSize = calculateCurrentStorageSize(0);
    }

    //storage space functions

    public int calculateCurrentStorageSize(int addedUnits){
        int boughtCurrentStorageCount = getBoughtStorageUnits();
        int defaultUnit = Man10ShopV2.config.getInt("itemStorage.unitDefinition");
        if(defaultUnit == 0) defaultUnit = 1;
        return (addedUnits+boughtCurrentStorageCount)*defaultUnit;
    }

    public int calculateNextUnitPrice(int addUnits){
        int totalPrice = 0;

        MemorySection section = ((MemorySection)Man10ShopV2.config.get("itemStorage.prices"));
        if(section == null) return -1;

        int nextUnit = getBoughtStorageUnits();

        if(nextUnit + 1 > Man10ShopV2.config.getInt("itemStorage.maxStorageUnits")) return -1;

        for(int i = 0; i < addUnits; i++){
            nextUnit += 1;
            if(nextUnit > Man10ShopV2.config.getInt("itemStorage.maxStorageUnits")) continue;

            String currentRangeKey = null;
            for(String key : section.getKeys(false)){
                if(!BaseUtils.isInt(key)) continue;
                if(Integer.parseInt(key) <= nextUnit){
                    currentRangeKey = key;
                }else{
                    break;
                }
            }
            if(currentRangeKey == null){
                totalPrice += 0;
                continue;
            }
            totalPrice += section.getInt(currentRangeKey);
        }

        return totalPrice;
    }

    public boolean buyStorageSpace(Player p, int units){
        int boughtCurrentStorageCount = getBoughtStorageUnits();
        if(boughtCurrentStorageCount+units > Man10ShopV2.config.getInt("itemStorage.maxStorageUnits")){
            p.sendMessage(Man10ShopV2.prefix + "§c§l倉庫ユニットの上限を超えました");
            return false;
        }
        if(Man10ShopV2.vault.getBalance(p.getUniqueId()) < calculateNextUnitPrice(units)){
            p.sendMessage(Man10ShopV2.prefix + "§c§lお金が足りません");
            return false;
        }
        Man10ShopV2.vault.withdraw(p.getUniqueId(), calculateNextUnitPrice(units));
        Man10ShopV2API.log(shop.getShopId(), "buyStorageSpace", units, p.getName(), p.getUniqueId()); //log
        p.sendMessage(Man10ShopV2.prefix + "§a§l倉庫スペースを購入しました");
        storageSize = calculateCurrentStorageSize(units);
        return buyStorageSpace(units);
    }

    public boolean buyStorageSpace(int units){
        return setBoughtStorageUnits(getBoughtStorageUnits()+units);

    }

    public int getStorageSize(){
        return storageSize;
    }

    //====================
    // settings
    //====================

    public int getBoughtStorageUnits(){
        String currentSetting = getSetting("storage.bought");
        if(!BaseUtils.isInt(currentSetting)) return Man10ShopV2.config.getInt("itemStorage.defaultUnits");
        return Integer.parseInt(currentSetting);
    }

    public boolean setBoughtStorageUnits(int units){
        if(getBoughtStorageUnits() == units) return true;
        return setSetting("storage.bought", units);
    }

    //item count functions

    public boolean removeItemCount(int count){
        itemCount = itemCount - count;
        boolean result = Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET item_count = item_count - " + count + " WHERE shop_id = '" + shop.getShopId() + "'");
        if(!result) return false;
        return true;
    }

    public boolean addItemCount(int count){
        itemCount = itemCount + count;
        boolean result = Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET item_count = item_count + " + count + " WHERE shop_id = '" + shop.getShopId() + "'");
        if(!result) return false;
        //log here
        return true;
    }

    public int getItemCount(){
        return itemCount;
    }

    //allowed


    @Override
    public boolean isAllowedToUseShop(Player p) {

        //sell shop check
        if(shop.getShopType() == Man10ShopType.SELL){
            if(itemCount >= calculateCurrentStorageSize(0) && !shop.isAdminShop()){
                p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップは在庫がいっぱいです");
                return false;
            }
        }

        //buy shop check
        if(shop.getShopType() == Man10ShopType.BUY){
            if(itemCount <= 0 && !shop.isAdminShop()){
                p.sendMessage(Man10ShopV2.prefix + "§c§l在庫がありません");
                return false;
            }
        }
        return true;
    }
}
