package com.shojabon.man10shopv2.dataClass.shopFunctions.agent;

import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
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
        name = "倉庫サイズ設定",
        explanation = {},
        enabledShopType = {Man10ShopType.BUY, Man10ShopType.SELL},
        iconMaterial = Material.ENDER_CHEST,
        category = "倉庫設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = false
)
public class SetStorageSizeFunction extends ShopFunction {

    //variables

    //init
    public SetStorageSizeFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }

    //====================
    // settings
    //====================

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        if(!player.hasPermission("man10shopv2.admin.agent")) return null;
        item.setEvent(e -> {
            //number input menu
            NumericInputMenu menu = new NumericInputMenu("§aユニット数を選んでください", plugin);
            menu.setOnClose(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnCancel(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnConfirm(newValue -> {
                if(!shop.storage.boughtStorageUnits.set(newValue)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
                player.sendMessage(Man10ShopV2.prefix + "§a§l倉庫サイズを" + newValue + "に設定しました");
            });
            menu.open(player);

        });

        return item;
    }
}
