package com.shojabon.man10shopv2.DataClass.ShopFunctions;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
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
    public String settingCategory() {
        return "使用可条件設定";
    }

    @Override
    public boolean hasPermissionToEdit(UUID uuid) {
        return shop.permission.hasPermissionAtLeast(uuid, Man10ShopPermission.MODERATOR);
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
    public SInventoryItem getAdminSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.IRON_DOOR).setDisplayName(new SStringBuilder().gold().text("ショップを使用可能な権限を設定する").build());
        SStringBuilder currentSetting = new SStringBuilder().lightPurple().text("現在の設定: ").yellow();
        if(getAllowedPermission() == null){
            currentSetting.text("なし");
        }else{
            currentSetting.text("man10shopv2.use." + getAllowedPermission());
        }
        item.addLore(currentSetting.build());

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setAsyncEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //text input
            SLongTextInput textInput = new SLongTextInput("§d§l権限を入力してください man10shopv2.use.XXXX 空白の場合はなし", plugin);
            textInput.setOnConfirm(permissionName -> {
                if(permissionName.length() > 64){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l権限は64文字以内でなくてはなりません");
                    return;
                }
                if(!setAllowedPermission(permissionName)){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                    return;
                }
                Man10ShopV2API.log(shop.getShopId(), "setShopAllowedPermission", permissionName, player.getName(), player.getUniqueId()); //log
                player.sendMessage(Man10ShopV2.prefix + "§a§l権限を変更しました");
            });

            textInput.setOnCancel(ee -> player.sendMessage(Man10ShopV2.prefix + "§c§lキャンセルしました"));


            textInput.open(player);
            sInventory.close(player);
        });

        return inventoryItem;
    }
}
