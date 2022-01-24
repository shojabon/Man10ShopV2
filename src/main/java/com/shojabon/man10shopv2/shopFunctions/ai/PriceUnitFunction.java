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
        name = "金額ユニット設定",
        explanation = {},
        enabledShopType = {Man10ShopType.AI},
        iconMaterial = Material.EMERALD,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class PriceUnitFunction extends ShopFunction{

    public Man10ShopSetting<Integer> price = new Man10ShopSetting<>("shop.ai.price", 0, true);

    public PriceUnitFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    @Override
    public String currentSettingString() {
        return "金額ユニット" + price.get();
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //confirmation menu
            NumericInputMenu menu = new NumericInputMenu("金額ユニットを設定してください", plugin);
            menu.setOnConfirm(number -> {
                if(!price.set(number)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "金額ユニットをを設定しました");
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
            });
            menu.setOnCancel(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnClose(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));

            menu.open(player);
        });
        return item;
    }

}
