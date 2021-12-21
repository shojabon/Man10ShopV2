package com.shojabon.man10shopv2.shopFunctions.general;

import ToolMenu.AutoScaledMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@ShopFunctionDefinition(
        name = "ショップタイプ設定",
        explanation = {},
        enabledShopType = {},
        iconMaterial = Material.OAK_FENCE_GATE,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = false
)
public class ShopTypeFunction extends ShopFunction {

    //variables
    public Man10ShopType shopType;

    //init
    public ShopTypeFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
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
    public String currentSettingString() {
        return getShopType().displayName;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //confirmation menu
            getInnerSettingMenu(player, plugin).open(player);

        });


        return item;
    }

    public AutoScaledMenu getInnerSettingMenu(Player player, Man10ShopV2 plugin){
        AutoScaledMenu menu = new AutoScaledMenu("ショップタイプ選択", plugin);
        for(Man10ShopType type: Man10ShopType.values()){
            if(type.admin && !shop.admin) continue;
            SInventoryItem mode = new SInventoryItem(new SItemStack(type.settingItem).setDisplayName("§a§l" + type.displayName).build());
            mode.clickable(false);
            mode.setAsyncEvent(e -> {
                if(!shop.shopType.setShopType(type)){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                    return;
                }

                Man10ShopV2API.log(shop.shopId, "setShopType", type.name(), player.getName(), player.getUniqueId()); //log
                player.sendMessage(Man10ShopV2.prefix + "§a§lショップタイプが設定されました");
                player.getServer().getScheduler().runTask(plugin, ()-> {
                    SInventory.closeInventoryGroup(shop.shopId, plugin);
                    Man10ShopV2.api.updateAllSigns(shop);
                });
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
            });
            menu.addItem(mode);
        }

        return menu;
    }
}
