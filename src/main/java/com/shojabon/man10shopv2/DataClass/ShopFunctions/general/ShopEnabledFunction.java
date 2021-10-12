package com.shojabon.man10shopv2.DataClass.ShopFunctions.general;

import ToolMenu.BooleanInputMenu;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.Settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ShopEnabledFunction extends ShopFunction {

    //variables

    //init
    public ShopEnabledFunction(Man10Shop shop) {
        super(shop);
    }


    //functions


    //====================
    // settings
    //====================

    public boolean getShopEnabled(){
        String currentSetting = getSetting("shop.enabled");
        if(!BaseUtils.isBoolean(currentSetting)) return true;
        return Boolean.parseBoolean(currentSetting);
    }

    public boolean setShopEnabled(boolean enabled){
        if(getShopEnabled() == enabled) return true;
        if(!setSetting("shop.enabled", enabled)) return false;
        Man10ShopV2API.closeInventoryGroup(shop.getShopId());
        return true;
    }

    @Override
    public String settingCategory() {
        return "一般設定";
    }

    @Override
    public boolean hasPermissionToEdit(UUID uuid) {
        return shop.permission.hasPermissionAtLeast(uuid, Man10ShopPermission.MODERATOR);
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        //shop disabled
        if(!getShopEnabled()){
            p.sendMessage(Man10ShopV2.prefix + "§c§l現在このショップは停止しています");
            return false;
        }
        return true;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.LEVER).setDisplayName(new SStringBuilder().gray().text("ショップ取引有効").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(BaseUtils.booleanToJapaneseText(getShopEnabled())).build());
        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);

        inventoryItem.setEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //confirmation menu
            BooleanInputMenu menu = new BooleanInputMenu(getShopEnabled(), "ショップ有効化設定", plugin);
            menu.setOnClose(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin)));
            menu.setOnCancel(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin)));
            menu.setOnConfirm(bool -> {
                if(setShopEnabled(bool)){
                    Man10ShopV2API.log(shop.shopId, "enableShop", bool, player.getName(), player.getUniqueId()); //log
                }
                plugin.api.updateAllSigns(shop);
                menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin));
            });

            sInventory.moveToMenu(player, menu);

        });


        return inventoryItem;
    }
}
