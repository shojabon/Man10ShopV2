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
        name = "学習レート設定",
        explanation = {},
        enabledShopType = {Man10ShopType.AI},
        iconMaterial = Material.EXPERIENCE_BOTTLE,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class LearningRateFunction extends ShopFunction{

    public Man10ShopSetting<Integer> rate = new Man10ShopSetting<>("shop.ai.learningrate", 0, true);

    public LearningRateFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    @Override
    public String currentSettingString() {
        return "学習レート" + rate.get();
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //confirmation menu
            NumericInputMenu menu = new NumericInputMenu("金額ユニットを設定してください", plugin);
            menu.setOnConfirm(number -> {
                if(!rate.set(number)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "金額ユニットを設定しましたを設定しました");
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
            });
            menu.setOnCancel(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnClose(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));

            menu.open(player);
        });
        return item;
    }

}
