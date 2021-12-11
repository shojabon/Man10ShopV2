package com.shojabon.man10shopv2.dataClass.shopFunctions.general;

import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.menus.settings.innerSettings.ShopTypeSelectorMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;
@ShopFunctionDefinition(
        name = "ショップタイプ設定",
        explanation = {},
        enabledShopType = {},
        iconMaterial = Material.OAK_FENCE_GATE,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = false
)
public class ShopTypeFunction extends ShopFunction {

    //variables
    public Man10ShopType shopType;

    //init
    public ShopTypeFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }

    public Man10ShopType getShopType() {
        return shopType;
    }

    public boolean setShopType(Man10ShopType type){
        shopType = type;
        if(!Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET shop_type ='" + type.name() + "' WHERE shop_id = '" + shop.getShopId() + "'")){
            return false;
        }
        Man10ShopV2API.closeInventoryGroup(shop.getShopId());
        return true;
    }

    //functions

    public String shopTypeToString(Man10ShopType type){
        if(type == Man10ShopType.BUY) return "販売ショップ";
        if(type == Man10ShopType.SELL) return "買取ショップ";
        if(type == Man10ShopType.BARTER) return "トレードショップ";
        if(type == Man10ShopType.LOOT_BOX) return "ガチャ";
        if(type == Man10ShopType.QUEST) return "クエスト";
        return "不明";
    }

    //====================
    // settings
    //====================


    @Override
    public boolean isAllowedToUseShop(Player p) {
        return true;
    }

    @Override
    public boolean isAllowedToUseShopWithAmount(Player p, int amount) {
        return true;
    }

    @Override
    public String currentSettingString() {
        return shopTypeToString(getShopType());
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //confirmation menu
            new ShopTypeSelectorMenu(player, shop, plugin).open(player);

        });


        return item;
    }
}
