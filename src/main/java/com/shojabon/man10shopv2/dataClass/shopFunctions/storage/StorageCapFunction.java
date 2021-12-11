package com.shojabon.man10shopv2.dataClass.shopFunctions.storage;

import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
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
        name = "買取制限",
        explanation = {"※買取ショップの場合のみ有効", "買取数の上限を設定する", "買取数上限を0にすると倉庫があるだけ買い取ります"},
        enabledShopType = {Man10ShopType.SELL},
        iconMaterial = Material.HOPPER,
        category = "倉庫設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = false
)
public class StorageCapFunction extends ShopFunction {

    //variables

    //init
    public StorageCapFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
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
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //number input menu
            NumericInputMenu menu = new NumericInputMenu(new SStringBuilder().green().text("購入制限設定").build(), plugin);
            if(!shop.admin) menu.setMaxValue(shop.storage.storageSize);
            menu.setOnCloseEvent(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnCancel(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnConfirm(newValue -> {
                if(newValue > shop.storage.storageSize && !shop.admin){
                    warn(player, "購入制限は倉庫以上の数にはできません");
                    return;
                }
                if(newValue < 0){
                    warn(player, "購入制限は正の数でなくてはならない");
                    return;
                }

                if(!setStorageCap(newValue)){
                    warn(player, "内部エラーが発生しました");
                }
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
            });
            menu.open(player);

        });

        return item;
    }
}
