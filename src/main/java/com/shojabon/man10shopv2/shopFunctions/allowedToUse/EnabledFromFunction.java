package com.shojabon.man10shopv2.shopFunctions.allowedToUse;

import ToolMenu.TimeSelectorMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@ShopFunctionDefinition(
        name = "有効化開始時間設定",
        explanation = {},
        enabledShopType = {},
        iconMaterial = Material.EMERALD_BLOCK,
        category = "使用可条件設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = false
)
public class EnabledFromFunction extends ShopFunction {

    //variables
    public Man10ShopSetting<Long> enabledFrom = new Man10ShopSetting<>("shop.enabledFrom", 0L);
    //init
    public EnabledFromFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions


    //====================
    // settings
    //====================

    @Override
    public boolean isAllowedToUseShop(Player p) {
        if(System.currentTimeMillis()/1000L < enabledFrom.get()) {
            p.sendMessage(Man10ShopV2.prefix + "§c§l現在このショップは停止しています、開始は " + BaseUtils.unixTimeToString(enabledFrom.get()));
            return false;
        }
        return true;
    }

    @Override
    public String currentSettingString() {
        return BaseUtils.unixTimeToString(enabledFrom.get());
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {

            TimeSelectorMenu menu = new TimeSelectorMenu(enabledFrom.get(), "有効化開始時間を設定してください", plugin);
            menu.setOnCloseEvent(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnConfirm(time -> {
                if (time == -1L) {
                    enabledFrom.delete();
                } else {
                    if (!enabledFrom.set(time)) {
                        warn(player, "内部エラーが発生しました");
                        return;
                    }
                }
                success(player, "時間を設定しました");
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
            });

            menu.open(player);

        });
        return item;
    }
}
