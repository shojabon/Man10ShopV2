package com.shojabon.man10shopv2.DataClass.ShopFunctions.general;

import ToolMenu.NumericInputMenu;
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

public class SetPriceFunction extends ShopFunction {

    //variables
    public int price = 0;

    //init
    public SetPriceFunction(Man10Shop shop) {
        super(shop);
    }


    //functions
    public boolean setPrice(int value){
        if(value < 0) return false;
        price = value;
        Man10ShopV2API.closeInventoryGroup(shop.getShopId());
        return Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET price = " + value + " WHERE shop_id = '" + shop.getShopId() + "'");
    }

    public int getPrice(){
        return price;
    }

    //====================
    // settings
    //====================

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
        return true;
    }

    @Override
    public boolean isAllowedToUseShopWithAmount(Player p, int amount) {
        return true;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.EMERALD).setDisplayName(new SStringBuilder().green().text("取引価格設定").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(BaseUtils.priceString(getPrice())).text("円").build());

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //number input menu
            NumericInputMenu menu = new NumericInputMenu(new SStringBuilder().green().text("取引値段設定").build(), plugin);
            menu.setOnClose(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin)));
            menu.setOnCancel(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin)));
            menu.setOnConfirm(newValue -> {
                if(setPrice(newValue)){
                    Man10ShopV2API.log(shop.shopId, "setPrice", newValue, player.getName(), player.getUniqueId()); //log
                }
                Man10ShopV2.api.updateAllSigns(shop);
                menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin));
            });
            sInventory.moveToMenu(player, menu);

        });

        return inventoryItem;
    }
}
