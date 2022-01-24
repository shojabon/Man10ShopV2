package com.shojabon.man10shopv2.shopFunctions.ai;

import ToolMenu.NumericInputMenu;
import ToolMenu.SingleItemStackSelectorMenu;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@ShopFunctionDefinition(
        name = "対象アイテム設定",
        explanation = {},
        enabledShopType = {Man10ShopType.AI},
        iconMaterial = Material.DIAMOND,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class AITargetItemFunction extends ShopFunction{

    public Man10ShopSetting<ItemStack> item = new Man10ShopSetting<>("shop.ai.targetitem", null, true);

    public AITargetItemFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    @Override
    public String currentSettingString() {
        if(item.get() == null) return "現金払い";
        return new SItemStack(item.get()).getDisplayName();
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //confirmation menu
            SingleItemStackSelectorMenu menu = new SingleItemStackSelectorMenu("アイテムを設定してください", this.item.get(), plugin);
            menu.allowNullItem(true);
            menu.setOnConfirm(finalItem -> {
                if(finalItem == null){
                    if(!this.item.delete()){
                        warn(player, "内部エラーが発生しました");
                        return;
                    }
                }else{
                    if(!this.item.set(finalItem)){
                        warn(player, "内部エラーが発生しました");
                        return;
                    }
                }
                success(player, "アイテムを設定しました");
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
            });
            menu.setOnCloseEvent(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));

            menu.open(player);
        });
        return item;
    }

}
