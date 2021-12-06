package com.shojabon.man10shopv2.DataClass.ShopFunctions.lootBox;

import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
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

public class LootBoxSpinTimeFunction extends ShopFunction {

    //variables

    //init
    public LootBoxSpinTimeFunction(Man10Shop shop) {
        super(shop);
    }


    //functions


    //====================
    // settings
    //====================

    public int getSpinTime(){
        String currentSetting = getSetting("lootbox.spinTime");
        if(!BaseUtils.isInt(currentSetting)) return 5;
        return Integer.parseInt(currentSetting);
    }

    public boolean setSpinTime(int seconds){
        if(getSpinTime() == seconds) return true;
        return setSetting("lootbox.spinTime", seconds);
    }

    @Override
    public Man10ShopType[] enabledShopTypes() {
        return new Man10ShopType[]{};
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
    public SInventoryItem getAdminSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.WATER_BUCKET).setDisplayName(new SStringBuilder().green().text("回転秒数").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(getSpinTime()).build());
        item.addLore("");

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //number input menu
            NumericInputMenu menu = new NumericInputMenu(new SStringBuilder().green().text("回転秒数").build(), plugin);
            if(!shop.admin) menu.setMaxValue(shop.storage.storageSize);
            menu.setOnClose(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin)));
            menu.setOnCancel(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin)));
            menu.setOnConfirm(newValue -> {

                if(setSpinTime(newValue)){
                    Man10ShopV2API.log(shop.shopId, "setSpinTime", newValue, player.getName(), player.getUniqueId()); //log
                }
                menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin));
            });
            sInventory.moveToMenu(player, menu);

        });

        return inventoryItem;
    }
}
