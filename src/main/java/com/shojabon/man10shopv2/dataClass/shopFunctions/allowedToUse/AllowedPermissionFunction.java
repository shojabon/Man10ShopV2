package com.shojabon.man10shopv2.dataClass.shopFunctions.allowedToUse;

import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SLongTextInput;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

@ShopFunctionDefinition(
        name = "ショップを使用可能な権限を設定",
        explanation = {"man10shopv2.use.XXXX", "空白の場合はなし"},
        enabledShopType = {},
        iconMaterial = Material.NAME_TAG,
        category = "使用可条件設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class AllowedPermissionFunction extends ShopFunction {

    //variables

    //init
    public AllowedPermissionFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
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

    @Override
    public String currentSettingString() {
        return "man10shopv2.use." + getAllowedPermission();
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setAsyncEvent(e -> {
            //text input
            SLongTextInput textInput = new SLongTextInput("§d§l権限を入力してください man10shopv2.use.XXXX 空白の場合はなし", plugin);
            textInput.setOnConfirm(permissionName -> {
                if(permissionName.length() > 64){
                    warn(player, "権限は64文字以内でなくてはなりません");
                    return;
                }
                if(!setAllowedPermission(permissionName)){
                    warn(player,"内部エラーが発生しました");
                    return;
                }
                player.sendMessage(Man10ShopV2.prefix + "§a§l権限を変更しました");
            });

            textInput.setOnCancel(ee -> warn(player,"キャンセルしました"));


            textInput.open(player);
            SInventory.closeNoEvent(player, plugin);
        });

        return item;
    }
}
