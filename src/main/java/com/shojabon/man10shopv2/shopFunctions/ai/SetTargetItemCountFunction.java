package com.shojabon.man10shopv2.shopFunctions.ai;

import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@ShopFunctionDefinition(
        name = "販売目標設定(24時間)",
        explanation = {},
        enabledShopType = {Man10ShopType.AI},
        iconMaterial = Material.TARGET,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class SetTargetItemCountFunction extends ShopFunction{

    public Man10ShopSetting<Integer> count = new Man10ShopSetting<>("shop.ai.target.count", 0, true);

    public SetTargetItemCountFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    @Override
    public String currentSettingString() {
        if(!isFunctionEnabled()) return "なし";
        return "24時間で" + count.get() + "個販売目標";
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //confirmation menu
            NumericInputMenu menu = new NumericInputMenu("24時間での販売個数目標を設定してください", plugin);
            menu.setOnConfirm(number -> {
                if(!count.set(number)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "個数を設定しました");
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
            });
            menu.setOnCancel(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnClose(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));

            menu.open(player);
        });
        return item;
    }

}
