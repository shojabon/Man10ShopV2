package com.shojabon.man10shopv2.DataClass.ShopFunctions;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.Settings.SettingsMainMenu;
import com.shojabon.man10shopv2.Utils.BaseUtils;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SInventory.ToolMenu.BooleanInputMenu;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SingleTransactionModeFunction extends ShopFunction {

    //variables

    //init
    public SingleTransactionModeFunction(Man10Shop shop) {
        super(shop);
    }


    //functions


    //====================
    // settings
    //====================

    public boolean isSingleTransactionMode(){
        String currentSetting = getSetting("shop.transaction.single");
        if(!BaseUtils.isBoolean(currentSetting)) return false;
        return Boolean.parseBoolean(currentSetting);
    }

    public boolean setSingleSellMode(boolean enabled){
        if(isSingleTransactionMode() == enabled) return true;
        if(!setSetting("shop.transaction.single", enabled)) return false;
        Man10ShopV2API.closeInventoryGroup(shop.getShopId());
        return true;
    }


    @Override
    public boolean isAllowedToUseShop(Player p) {
        return true;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.BOWL).setDisplayName(new SStringBuilder().gray().text("単品取引モード").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(BaseUtils.booleanToJapaneseText(isSingleTransactionMode())).build());
        item.addLore("");
        item.addLore("§fまとめて取引ができなくなります");
        item.addLore("§f1個ずつのみの取引になります");
        item.addLore("§fイベントなど盛り上げたいときに使います");


        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(e -> {
            //confirmation menu
            BooleanInputMenu menu = new BooleanInputMenu(shop.singleTransactionMode.isSingleTransactionMode(), "単品取引モード", plugin);
            menu.setOnClose(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));
            menu.setOnCancel(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));
            menu.setOnConfirm(bool -> {
                if(shop.singleTransactionMode.setSingleSellMode(bool)){
                    Man10ShopV2API.log(shop.shopId, "setSingleSellMode", bool, player.getName(), player.getUniqueId()); //log
                }
                menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin));
            });

            sInventory.moveToMenu(player, menu);

        });


        return inventoryItem;
    }
}
