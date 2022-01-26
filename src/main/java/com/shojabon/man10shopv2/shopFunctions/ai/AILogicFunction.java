package com.shojabon.man10shopv2.shopFunctions.ai;

import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@ShopFunctionDefinition(
        name = "AIロジック",
        explanation = {},
        enabledShopType = {Man10ShopType.AI},
        iconMaterial = Material.REPEATER,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class AILogicFunction extends ShopFunction{

    boolean tracking = false;
    int currentSellCount = 0;
    long trackingStart = -1L;


    public AILogicFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }

    @Override
    public void perMinuteExecuteTask() {
        if(System.currentTimeMillis()/1000L - trackingStart <= 4*60*60) return;
        if(currentSellCount == 0) currentSellCount = 1;
        updatePrice();
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        if(shop.aiPriceUnitFunction.price.get() <= 0) return false;
        if(shop.aiSetTargetItemCountFunction.count.get() == 0) return false;
        return true;
    }

    @Override
    public void init() {
        shop.shopEnabled.enabled.addOnStateChangeEvent(e -> {
            if(e){
                startTracking();
            }else{
                stopTracking();
            }
        });
        if(shop.shopEnabled.enabled.get()){
            startTracking();
        }
    }

    public void startTracking(){
        if(tracking) return;
        tracking = true;
        currentSellCount = 0;
        trackingStart = System.currentTimeMillis()/1000;
    }

    public void stopTracking(){
        if(!tracking) return;
        updatePrice();
        tracking = false;
    }

    public void updatePrice(){
        if(shop.aiPriceUnitFunction.price.get() <= 0) return;
        if(shop.aiSetTargetItemCountFunction.count.get() == 0) return;
        if(currentSellCount == 0) return;
        long secondsPasted = (System.currentTimeMillis()/1000-trackingStart);
        if(secondsPasted == 0) return;
        double supposedToSell = (secondsPasted/(24*60*60d)) * shop.aiSetTargetItemCountFunction.count.get();
        if(supposedToSell == 0) {
            if(shop.aiSetTargetItemCountFunction.count.get() == 1){
                supposedToSell = 1/24d;
            }else{
                supposedToSell += 1;
            }
        }
        int currentPriceUnit = shop.aiPriceUnitFunction.price.get();
        long newPrice = (long) (currentSellCount/supposedToSell)*currentPriceUnit;
        newPrice = (long) (currentPriceUnit + (newPrice - currentPriceUnit) * ((shop.aiLearningRateFunction.rate.get()/100d)));
        if(newPrice < shop.aiMinimumPriceUnitFunction.price.get() && shop.aiMinimumPriceUnitFunction.price.get() != 0){
            newPrice = shop.aiMinimumPriceUnitFunction.price.get();
        }
        if(newPrice > 999999999) newPrice = 999999999;
        shop.aiPriceUnitFunction.price.set(Math.toIntExact(newPrice));
        SInventory.closeInventoryGroup(shop.shopId, plugin);
    }

    @Override
    public boolean afterPerformAction(Player p, int amount) {
        currentSellCount += amount;
        if(currentSellCount >= shop.aiSetTargetItemCountFunction.count.get()){
            stopTracking();
            startTracking();
        }
        return true;
    }

    @Override
    public void onUnload() {
        updatePrice();
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        return null;
    }

}
