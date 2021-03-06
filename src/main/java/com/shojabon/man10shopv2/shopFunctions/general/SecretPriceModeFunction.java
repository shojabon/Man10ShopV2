package com.shojabon.man10shopv2.shopFunctions.general;

import ToolMenu.BooleanInputMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@ShopFunctionDefinition(
        name = "値段非表示モード",
        explanation = {"設定した分間毎に値段を設定する", "どちらかが0の場合設定は無効化"},
        enabledShopType = {Man10ShopType.BUY, Man10ShopType.SELL, Man10ShopType.COMMAND},
        iconMaterial = Material.POISONOUS_POTATO,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class SecretPriceModeFunction extends ShopFunction {

    //variables
    public Man10ShopSetting<Boolean> enabled = new Man10ShopSetting<>("shop.secretPrice", false);
    //init
    public SecretPriceModeFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions


    //====================
    // settings
    //====================

    @Override
    public String currentSettingString() {
        return BaseUtils.booleanToJapaneseText(enabled.get());
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //confirmation menu
            BooleanInputMenu menu = new BooleanInputMenu(enabled.get(), "値段非表示設定", plugin);
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
