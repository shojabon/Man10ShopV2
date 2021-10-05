package com.shojabon.man10shopv2.DataClass.ShopFunctions;

import ToolMenu.ConfirmationMenu;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.Settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DeleteShopFunction extends ShopFunction {

    //variables

    //init
    public DeleteShopFunction(Man10Shop shop) {
        super(shop);
    }


    //functions


    //====================
    // settings
    //====================


    @Override
    public boolean hasPermissionToEdit(UUID uuid) {
        return shop.permission.hasPermissionAtLeast(uuid, Man10ShopPermission.OWNER);
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
        SItemStack item = new SItemStack(Material.LAVA_BUCKET).setDisplayName(new SStringBuilder().yellow().obfuscated().text("OO")
                .darkRed().bold().text("ショップを削除")
                .yellow().obfuscated().text("OO")
                .build());

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setAsyncEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //confirmation menu
            ConfirmationMenu menu = new ConfirmationMenu("確認", plugin);
            menu.setOnCancel(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));
            menu.setOnConfirm(ee -> {
                //delete shop
                shop.deleteShop();
                plugin.getServer().getScheduler().runTask(plugin, () -> Man10ShopV2.api.destroyAllSigns(shop));
                Man10ShopV2API.log(shop.shopId, "deleteShop", null, player.getName(), player.getUniqueId()); //log
                menu.close(player);
            });

            sInventory.moveToMenu(player, menu);
        });

        return inventoryItem;
    }
}
