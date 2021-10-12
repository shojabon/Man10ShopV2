package com.shojabon.man10shopv2.DataClass.ShopFunctions.general;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.mcutils.Utils.MySQL.MySQLAPI;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SLongTextInput;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NameFunction extends ShopFunction {

    //variables
    public String name;
    //init
    public NameFunction(Man10Shop shop) {
        super(shop);
    }


    //functions
    public boolean setName(String name){
        if(name.length() > 64 || name.length() == 0) return false;
        this.name = name;
        return Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET name = '" + MySQLAPI.escapeString(name) + "' WHERE shop_id = '" + shop.getShopId() + "'");
    }

    public String getName(){
        return name;
    }

    //====================
    // settings
    //====================

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
        SItemStack item = new SItemStack(Material.NAME_TAG).setDisplayName(new SStringBuilder().gold().text("ショップの名前を変更する").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(getName()).build());

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setAsyncEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //text input
            SLongTextInput textInput = new SLongTextInput("§d§lショップ名を入力してください", plugin);
            textInput.setOnConfirm(shopName -> {
                if(shopName.length() > 64 || shopName.length() == 0){
                    player.sendMessage(Man10ShopV2.prefix + "§c§lショップ名は64文字以内でなくてはなりません");
                    return;
                }
                if(!setName(shopName)){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                    return;
                }
                player.sendMessage(Man10ShopV2.prefix + "§a§lショップ名を変更しました");
            });

            textInput.setOnCancel(ee -> player.sendMessage(Man10ShopV2.prefix + "§c§lキャンセルしました"));


            textInput.open(player);
            sInventory.close(player);
        });

        return inventoryItem;
    }
}
