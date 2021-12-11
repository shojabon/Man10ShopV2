package com.shojabon.man10shopv2.dataClass.shopFunctions.allowedToUse;

import ToolMenu.TimeSelectorMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
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

    //init
    public EnabledFromFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions


    //====================
    // settings
    //====================

    public long getEnabledTime(){
        String currentSetting = getSetting("shop.enabledFrom");
        if(!BaseUtils.isLong(currentSetting)) return 0;
        return Long.parseLong(currentSetting);
    }

    public boolean setEnabledTime(long enabled){
        if(getEnabledTime() == enabled) return true;
        if(!setSetting("shop.enabledFrom", enabled)) return false;
        Man10ShopV2API.closeInventoryGroup(shop.getShopId());
        return true;
    }

    @Override
    public boolean isFunctionEnabled() {
        return getEnabledTime() != 0;
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        if(System.currentTimeMillis()/1000L < getEnabledTime()) {
            p.sendMessage(Man10ShopV2.prefix + "§c§l現在このショップは停止しています、開始は " + BaseUtils.unixTimeToString(getEnabledTime()));
            return false;
        }
        return true;
    }

    @Override
    public String currentSettingString() {
        return BaseUtils.unixTimeToString(getEnabledTime());
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {

            TimeSelectorMenu menu = new TimeSelectorMenu(getEnabledTime(), "有効化開始時間を設定してください", plugin);
            menu.setOnCloseEvent(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnConfirm(time -> {
                if (time == -1L) {
                    deleteSetting("shop.enabledFrom");
                } else {
                    if (!setEnabledTime(time)) {
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
