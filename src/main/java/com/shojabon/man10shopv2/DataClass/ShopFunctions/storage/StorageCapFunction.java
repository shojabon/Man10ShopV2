package com.shojabon.man10shopv2.DataClass.ShopFunctions.storage;

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

public class StorageCapFunction extends ShopFunction {

    //variables

    //init
    public StorageCapFunction(Man10Shop shop) {
        super(shop);
    }


    //functions


    //====================
    // settings
    //====================

    public int getStorageCap(){
        String currentSetting = getSetting("storage.sell.cap");
        if(!BaseUtils.isInt(currentSetting)) return 0;
        return Integer.parseInt(currentSetting);
    }

    public boolean setStorageCap(int storageCap){
        if(getStorageCap() == storageCap) return true;
        return setSetting("storage.sell.cap", storageCap);
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
    public boolean isFunctionEnabled() {
        return getStorageCap() != 0;
    }

    @Override
    public int itemCount(Player p) {
        if(!isFunctionEnabled()) return super.itemCount(p);
        if(shop.shopType.getShopType() == Man10ShopType.SELL){
            return getStorageCap();
        }
        return super.itemCount(p);
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        if(shop.shopType.getShopType() == Man10ShopType.SELL){
            //no money (sell)
            if(getStorageCap() != 0 && shop.storage.itemCount >= getStorageCap()){
                p.sendMessage(Man10ShopV2.prefix + "§c§l現在このショップは買取をしていません");
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isAllowedToUseShopWithAmount(Player p, int amount) {
        //if item storage hits storage cap
        if(shop.shopType.getShopType() == Man10ShopType.SELL){
            if(shop.storage.itemCount + amount > getStorageCap() && getStorageCap() != 0){
                p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップは現在買取を行っていません");
                return false;
            }
        }
        return true;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.HOPPER).setDisplayName(new SStringBuilder().green().text("買取制限").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(getStorageCap()).build());
        item.addLore("");
        item.addLore("§f※買取ショップの場合のみ有効");
        item.addLore("§f買取数の上限を設定する");
        item.addLore("§f買取数上限を0にすると倉庫があるだけ買い取ります");

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //number input menu
            NumericInputMenu menu = new NumericInputMenu(new SStringBuilder().green().text("購入制限設定").build(), plugin);
            if(!shop.admin) menu.setMaxValue(shop.storage.storageSize);
            menu.setOnClose(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin)));
            menu.setOnCancel(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin)));
            menu.setOnConfirm(newValue -> {
                if(newValue > shop.storage.storageSize && !shop.admin){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l購入制限は倉庫以上の数にはできません");
                    return;
                }
                if(newValue < 0){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l購入制限は正の数でなくてはならない");
                    return;
                }

                if(setStorageCap(newValue)){
                    Man10ShopV2API.log(shop.shopId, "setStorageCap", newValue, player.getName(), player.getUniqueId()); //log
                }
                menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin));
            });
            sInventory.moveToMenu(player, menu);

        });

        return inventoryItem;
    }
}
