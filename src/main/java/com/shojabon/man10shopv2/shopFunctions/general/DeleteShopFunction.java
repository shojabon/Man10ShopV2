package com.shojabon.man10shopv2.shopFunctions.general;

import ToolMenu.ConfirmationMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@ShopFunctionDefinition(
        name = "§e§l§k00§4§lショップを削除§e§l§k00",
        explanation = {},
        enabledShopType = {},
        iconMaterial = Material.LAVA_BUCKET,
        category = "削除設定",
        allowedPermission = Man10ShopPermission.OWNER,
        isAdminSetting = false
)
public class DeleteShopFunction extends ShopFunction {

    //variables

    //init
    public DeleteShopFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions


    //====================
    // settings
    //====================

    @Override
    public boolean isAllowedToUseShop(Player p) {
        return true;
    }

    @Override
    public boolean isAllowedToUseShopWithAmount(Player p, int amount) {
        return true;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setAsyncEvent(e -> {
            //confirmation menu
            ConfirmationMenu menu = new ConfirmationMenu("確認", plugin);
            menu.setOnCloseEvent(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnConfirm(ee -> {
                //delete shop
                shop.deleteShop();
                plugin.getServer().getScheduler().runTask(plugin, () -> Man10ShopV2.api.destroyAllSigns(shop));
                menu.close(player);
            });
            menu.open(player);
        });

        return item;
    }
}
