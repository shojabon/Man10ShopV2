package com.shojabon.man10shopv2.shopFunctions.general;

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
        name = "ショップのカテゴリを設定する",
        explanation = {},
        enabledShopType = {},
        iconMaterial = Material.LEAD,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = false
)
public class CategoryFunction extends ShopFunction {

    //variables
    public Man10ShopSetting<String> category = new Man10ShopSetting<>("shop.category", "その他");
    //init
    public CategoryFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions

    //====================
    // settings
    //====================

    @Override
    public String currentSettingString() {
        return category.get();
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
                if(!category.set(categoryName)){
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
