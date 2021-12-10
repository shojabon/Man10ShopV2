package com.shojabon.man10shopv2.DataClass.ShopFunctions.agent;

import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.Settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SetItemCountFunction extends ShopFunction {

    //variables

    //init
    public SetItemCountFunction(Man10Shop shop) {
        super(shop);
    }

    //====================
    // settings
    //====================

    @Override
    public Man10ShopType[] enabledShopTypes() {
        return new Man10ShopType[]{Man10ShopType.BUY, Man10ShopType.SELL};
    }

    @Override
    public String settingCategory() {
        return "倉庫設定";
    }

    @Override
    public boolean hasPermissionToEdit(UUID uuid) {
        return shop.permission.hasPermissionAtLeast(uuid, Man10ShopPermission.MODERATOR);
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        if(!player.hasPermission("man10shopv2.admin.agent")) return null;
        SItemStack item = new SItemStack(Material.COMPASS).setDisplayName(new SStringBuilder().green().text("アイテム数設定").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(BaseUtils.priceString(shop.storage.getItemCount())).text("個").build());

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                warn(player, "権限が不足しています");
                return;
            }
            //number input menu
            NumericInputMenu menu = new NumericInputMenu(new SStringBuilder().green().text("アイテム数を設定してください").build(), plugin);
            menu.setOnClose(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin)));
            menu.setOnCancel(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin)));
            menu.setOnConfirm(newValue -> {
                if(!shop.storage.setItemCount(newValue)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin));
                success(player, "アイテム数を" + newValue + "に設定しました");
            });
            sInventory.moveToMenu(player, menu);

        });

        return inventoryItem;
    }
}
