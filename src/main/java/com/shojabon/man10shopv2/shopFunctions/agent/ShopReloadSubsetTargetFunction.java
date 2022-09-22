package com.shojabon.man10shopv2.shopFunctions.agent;

import ToolMenu.BooleanInputMenu;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@ShopFunctionDefinition(
        name = "部分リロードターゲット設定",
        explanation = {"部分リロードの対象とする"},
        enabledShopType = {},
        iconMaterial = Material.STICKY_PISTON,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = false
)
public class ShopReloadSubsetTargetFunction extends ShopFunction {

    //variables
    public Man10ShopSetting<Boolean> enabled = new Man10ShopSetting<>("shop.reload.subset.enabled", false);
    //init
    public ShopReloadSubsetTargetFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }

    //====================
    // settings
    //====================

    @Override
    public String currentSettingString() {
        return BaseUtils.booleanToJapaneseText(enabled.get());
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        if(!player.hasPermission("man10shopv2.admin.agent")) return null;
        item.setEvent(e -> {
            //confirmation menu
            BooleanInputMenu menu = new BooleanInputMenu(enabled.get(), "リロードターゲット有効化設定", plugin);
            menu.setOnClose(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnCancel(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnConfirm(bool -> {
                if(!enabled.set(bool)){
                    warn(player, "内部エラーが発生しました");
                }
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
            });
            menu.open(player);
        });
        return item;
    }

}
