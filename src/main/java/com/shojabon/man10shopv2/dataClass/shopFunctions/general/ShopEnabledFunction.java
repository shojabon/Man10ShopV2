package com.shojabon.man10shopv2.dataClass.shopFunctions.general;

import ToolMenu.BooleanInputMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;
@ShopFunctionDefinition(
        name = "ショップ有効化設定",
        explanation = {},
        enabledShopType = {},
        iconMaterial = Material.LEVER,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = false
)
public class ShopEnabledFunction extends ShopFunction {

    //variables

    //init
    public ShopEnabledFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions


    //====================
    // settings
    //====================

    public boolean getShopEnabled(){
        String currentSetting = getSetting("shop.enabled");
        if(!BaseUtils.isBoolean(currentSetting)) return true;
        return Boolean.parseBoolean(currentSetting);
    }

    public boolean setShopEnabled(boolean enabled){
        if(getShopEnabled() == enabled) return true;
        if(!setSetting("shop.enabled", enabled)) return false;
        Man10ShopV2API.closeInventoryGroup(shop.getShopId());
        return true;
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        //shop disabled
        if(!getShopEnabled()){
            warn(p, "現在このショップは停止しています");
            return false;
        }
        return true;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //confirmation menu
            BooleanInputMenu menu = new BooleanInputMenu(getShopEnabled(), "ショップ有効化設定", plugin);
            menu.setOnClose(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnCancel(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnConfirm(bool -> {
                if(!setShopEnabled(bool)){
                    warn(player, "内部エラーが発生しました");
                }
                Man10ShopV2.api.updateAllSigns(shop);
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
            });
            menu.open(player);
        });
        return item;
    }

}
