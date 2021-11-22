package com.shojabon.man10shopv2.DataClass.ShopFunctions.general;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.MySQL.MySQLAPI;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SLongTextInput;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class CategoryFunction extends ShopFunction {

    //variables
    //init
    public CategoryFunction(Man10Shop shop) {
        super(shop);
    }


    //functions

    //====================
    // settings
    //====================

    public String getCategory(){
        String currentSetting = getSetting("shop.category");
        if(currentSetting == null) return "その他";
        return currentSetting;
    }

    public boolean setCategory(String category){
        if(Objects.equals(getCategory(), category)) return true;
        return setSetting("shop.category", category);
    }

    @Override
    public String settingCategory() {
        return "一般設定";
    }

    @Override
    public boolean hasPermissionToEdit(UUID uuid) {
        return shop.permission.hasPermissionAtLeast(uuid, Man10ShopPermission.MODERATOR);
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.LEAD).setDisplayName(new SStringBuilder().gold().text("ショップのカテゴリを設定する").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(getCategory()).build());

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setAsyncEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //text input
            SLongTextInput textInput = new SLongTextInput("§d§lカテゴリ名を入力してください 空白の場合はその他になります", plugin);
            textInput.setOnConfirm(categoryName -> {
                if(categoryName.length() > 64){
                    player.sendMessage(Man10ShopV2.prefix + "§c§lショップ名は64文字以内でなくてはなりません");
                    return;
                }
                if(categoryName.length() == 0) categoryName = "その他";
                if(!setCategory(categoryName)){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                    return;
                }
                player.sendMessage(Man10ShopV2.prefix + "§a§lカテゴリを変更しました");
            });

            textInput.setOnCancel(ee -> player.sendMessage(Man10ShopV2.prefix + "§c§lキャンセルしました"));


            textInput.open(player);
            sInventory.close(player);
        });

        return inventoryItem;
    }
}
