package com.shojabon.man10shopv2.shopFunctions.allowedToUse;

import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SLongTextInput;
import org.bukkit.Material;
import org.bukkit.entity.Player;

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
    public Man10ShopSetting<String> permission = new Man10ShopSetting<>("shop.permission.allowed", null);
    //init
    public AllowedPermissionFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions


    //====================
    // settings
    //====================

    
    @Override
    public boolean isAllowedToUseShop(Player p) {
        //if player has permission
        if(permission.get() != null && !p.hasPermission("man10shopv2.use." + permission.get())){
            p.sendMessage(Man10ShopV2.prefix + "§c§lあなたはこのショップを使う権限がありません");
            return false;
        }
        return true;
    }

    @Override
    public String currentSettingString() {
        return "man10shopv2.use." + permission.get();
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
                if(!permission.set(permissionName)){
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
