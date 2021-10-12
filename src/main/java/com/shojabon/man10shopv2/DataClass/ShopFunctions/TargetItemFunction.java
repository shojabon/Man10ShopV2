package com.shojabon.man10shopv2.DataClass.ShopFunctions;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.mcutils.Utils.SItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class TargetItemFunction extends ShopFunction {

    //variables
    public SItemStack targetItem;
    //init
    public TargetItemFunction(Man10Shop shop) {
        super(shop);
    }


    //functions
    public boolean setTargetItem(ItemStack item){
        SItemStack sItem = new SItemStack(item);
        targetItem = sItem;
        boolean result = Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET target_item = '" + sItem.getItemTypeBase64(true) + "', target_item_hash ='" + sItem.getItemTypeMD5(true) + "' WHERE shop_id = '" + shop.getShopId() + "'");
        if(!result) return false;
        Man10ShopV2API.closeInventoryGroup(shop.getShopId());
        return true;
    }

    public SItemStack getTargetItem(){
        return targetItem;
    }

    //====================
    // settings
    //====================

    @Override
    public boolean hasPermissionToEdit(UUID uuid) {
        return shop.permission.hasPermissionAtLeast(uuid, Man10ShopPermission.MODERATOR);
    }

    @Override
    public boolean performAction(Player p, int amount) {
        if(shop.shopType.getShopType() == Man10ShopType.BUY){
            SItemStack item = new SItemStack(getTargetItem().build().clone());
            if(!shop.storage.removeItemCount(amount*item.getAmount())){
                p.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return false;
            }
            for(int i = 0; i < amount; i++){
                p.getInventory().addItem(item.build());
            }
        }
        if(shop.shopType.getShopType() == Man10ShopType.SELL){
            //remove items from shop storage
            SItemStack item = new SItemStack(getTargetItem().build().clone());
            if(!shop.storage.addItemCount(amount)){
                p.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return false;
            }
            //perform transaction
            for( int i =0; i < amount; i++){
                p.getInventory().removeItemAnySlot(item.build());
            }
        }
        return true;
    }

    @Override
    public boolean isAllowedToUseShopWithAmount(Player p, int amount) {
        if(shop.shopType.getShopType() == Man10ShopType.SELL){
            SItemStack item = new SItemStack(getTargetItem().build().clone());
            if(!p.getInventory().containsAtLeast(getTargetItem().build(), amount*item.getAmount())){
                p.sendMessage(Man10ShopV2.prefix + "§c§l買い取るためのアイテムを持っていません");
                return false;
            }
        }
        return true;
    }

}
