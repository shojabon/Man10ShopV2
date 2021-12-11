package com.shojabon.man10shopv2.dataClass.shopFunctions.general;

import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
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

@ShopFunctionDefinition(
        name = "ショップ名設定",
        explanation = {},
        enabledShopType = {},
        iconMaterial = Material.NAME_TAG,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = false
)

public class NameFunction extends ShopFunction {

    //variables
    public String name;
    //init
    public NameFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
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
    public String currentSettingString() {
        return getName();
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setAsyncEvent(e -> {
            //text input
            SLongTextInput textInput = new SLongTextInput("§d§lカテゴリ名を入力してください 空白の場合はその他になります", plugin);
            textInput.setOnConfirm(categoryName -> {
                if(categoryName.length() > 64){
                    warn(player, "ショップ名は64文字以内でなくてはなりません");
                    return;
                }
                if(categoryName.length() == 0) categoryName = "その他";
                if(!setName(categoryName)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "カテゴリを変更しました");
            });

            textInput.setOnCancel(ee -> warn(player, "キャンセルしました"));


            textInput.open(player);
            SInventory.closeNoEvent(player, plugin);
        });

        return item;
    }
}
