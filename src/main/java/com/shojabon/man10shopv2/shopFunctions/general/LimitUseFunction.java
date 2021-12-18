package com.shojabon.man10shopv2.shopFunctions.general;

import ToolMenu.AutoScaledMenu;
import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@ShopFunctionDefinition(
        name = "使用回数制限",
        explanation = {},
        enabledShopType = {},
        iconMaterial = Material.MELON_SEEDS,
        category = "使用可条件設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = false
)
public class LimitUseFunction extends ShopFunction{

    public Man10ShopSetting<Integer> count = new Man10ShopSetting<>("shop.limit.use.count", 0, true);
    public Man10ShopSetting<Boolean> enabled = new Man10ShopSetting<>("shop.limit.use.enabled", false);

    public LimitUseFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }



    @Override
    public boolean isAllowedToUseShop(Player p) {
        if(count.get() == 0){
            warn(p, "ショップの使用回数制限に達しました");
            return false;
        }
        return true;
    }

    @Override
    public int itemCount(Player p) {
        if(enabled.get()) {
            if(shop.admin) return -count.get();
            return count.get();
        }
        return super.itemCount(p);
    }

    @Override
    public String currentSettingString() {
        if(!isFunctionEnabled()) return "なし";
        return "残り" + count.get() + "回";
    }

    @Override
    public boolean afterPerformAction(Player p, int amount) {
        count.set(count.get()-1);
        return true;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //confirmation menu
            NumericInputMenu menu = new NumericInputMenu("残り使用回数を設定してください 0はoff", plugin);
            menu.setOnConfirm(number -> {
                if(!enabled.set(number != 0) || !count.set(number)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "回数を設定しました");
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
            });
            menu.setOnCancel(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnClose(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));

            menu.open(player);
        });
        return item;
    }

}
