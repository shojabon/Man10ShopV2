package com.shojabon.man10shopv2.DataClass.ShopFunctions.general;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.ShopTypeSelectorMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ShopTypeFunction extends ShopFunction {

    //variables
    public Man10ShopType shopType;

    //init
    public ShopTypeFunction(Man10Shop shop) {
        super(shop);
    }

    public Man10ShopType getShopType() {
        return shopType;
    }

    public boolean setShopType(Man10ShopType type){
        shopType = type;
        if(!Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET shop_type ='" + type.name() + "' WHERE shop_id = '" + shop.getShopId() + "'")){
            return false;
        }
        Man10ShopV2API.closeInventoryGroup(shop.getShopId());
        return true;
    }

    //functions

    public String buySellToString(Man10ShopType type){
        if(type == Man10ShopType.BUY) return "販売ショップ";
        return "買取ショップ";
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
        SItemStack item = new SItemStack(Material.OAK_FENCE_GATE).setDisplayName(new SStringBuilder().yellow().text("ショップタイプ設定").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(buySellToString(getShopType())).build());
        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);

        inventoryItem.setEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //confirmation menu
            sInventory.moveToMenu(player, new ShopTypeSelectorMenu(player, shop, plugin));

        });


        return inventoryItem;
    }
}
