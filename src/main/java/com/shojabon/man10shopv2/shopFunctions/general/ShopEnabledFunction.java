package com.shojabon.man10shopv2.shopFunctions.general;

import ToolMenu.BooleanInputMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

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
    public Man10ShopSetting<Boolean> enabled = new Man10ShopSetting<>("shop.enabled", true);
    //init
    public ShopEnabledFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions


    //====================
    // settings
    //====================

    @Override
    public boolean isAllowedToUseShop(Player p) {
        //shop disabled
        if(!enabled.get()){
            warn(p, "現在このショップは停止しています");
            return false;
        }
        return true;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //confirmation menu
            BooleanInputMenu menu = new BooleanInputMenu(enabled.get(), "ショップ有効化設定", plugin);
            menu.setOnClose(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnCancel(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnConfirm(bool -> {
                if(!enabled.set(bool)){
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
