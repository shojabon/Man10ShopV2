package com.shojabon.man10shopv2.shopFunctions.general;

import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@ShopFunctionDefinition(
        name = "取引価格設定",
        explanation = {},
        enabledShopType = {Man10ShopType.BUY, Man10ShopType.SELL, Man10ShopType.COMMAND},
        iconMaterial = Material.EMERALD,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = false
)
public class SetPriceFunction extends ShopFunction {

    //variables
    public int price = 0;

    //init
    public SetPriceFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
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
    public boolean performAction(Player p, int amount) {
        if(shop.shopType.getShopType() == Man10ShopType.BUY || shop.shopType.getShopType() == Man10ShopType.COMMAND){
            int totalPrice = getPrice()*amount;
            if(!Man10ShopV2.vault.withdraw(p.getUniqueId(), totalPrice)){
                warn(p, "内部エラーが発生しました");
                return false;
            }
            shop.money.addMoney(totalPrice);
        }
        if(shop.shopType.getShopType() == Man10ShopType.SELL){
            int totalPrice = getPrice()*amount;
            if(!shop.money.removeMoney(totalPrice)){
                warn(p, "内部エラーが発生しました");
                return false;
            }
            Man10ShopV2.vault.deposit(p.getUniqueId(), totalPrice);
        }
        return true;
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        return isAllowedToUseShopWithAmount(p, 1);
    }

    @Override
    public boolean isAllowedToUseShopWithAmount(Player p, int amount) {
        if(shop.shopType.getShopType() == Man10ShopType.BUY){
            int totalPrice = getPrice()*amount;
            if(Man10ShopV2.vault.getBalance(p.getUniqueId()) < totalPrice){
                warn(p, "残高が不足しています");
                return false;
            }
        }
        if(shop.shopType.getShopType() == Man10ShopType.SELL){
            int totalPrice = getPrice()*amount;
            if(totalPrice > shop.money.getMoney() && !shop.isAdminShop()){
                warn(p, "このショップの現金が不足しています");
                return false;
            }
        }
        return true;
    }

    @Override
    public String currentSettingString() {
        return getPrice() + "円";
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //number input menu
            NumericInputMenu menu = new NumericInputMenu(new SStringBuilder().green().text("取引値段設定").build(), plugin);
            menu.setOnClose(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnCancel(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnConfirm(newValue -> {
                if(!setPrice(newValue)){
                    warn(player, "内部エラーが発生しました");
                }
                Man10ShopV2.api.updateAllSigns(shop);
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
            });
            menu.open(player);

        });

        return item;
    }
}
