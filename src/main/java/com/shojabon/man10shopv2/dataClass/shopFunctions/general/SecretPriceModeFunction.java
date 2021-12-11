package com.shojabon.man10shopv2.dataClass.shopFunctions.general;

import ToolMenu.BooleanInputMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
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
        name = "値段非表示モード",
        explanation = {"設定した分間毎に値段を設定する", "どちらかが0の場合設定は無効化"},
        enabledShopType = {Man10ShopType.BUY, Man10ShopType.SELL},
        iconMaterial = Material.POISONOUS_POTATO,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class SecretPriceModeFunction extends ShopFunction {

    //variables

    //init
    public SecretPriceModeFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions


    //====================
    // settings
    //====================

    public boolean getSecretPriceMode(){
        String currentSetting = getSetting("shop.secretPrice");
        if(!BaseUtils.isBoolean(currentSetting)) return false;
        return Boolean.parseBoolean(currentSetting);
    }

    public boolean setSecretPriceMode(boolean enabled){
        if(getSecretPriceMode() == enabled) return true;
        if(!setSetting("shop.secretPrice", enabled)) return false;
        Man10ShopV2API.closeInventoryGroup(shop.getShopId());
        return true;
    }

    @Override
    public boolean isFunctionEnabled() {
        return getSecretPriceMode();
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //confirmation menu
            BooleanInputMenu menu = new BooleanInputMenu(getSecretPriceMode(), "値段非表示設定", plugin);
            menu.setOnClose(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnCancel(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnConfirm(bool -> {
                if(!setSecretPriceMode(bool)){
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
