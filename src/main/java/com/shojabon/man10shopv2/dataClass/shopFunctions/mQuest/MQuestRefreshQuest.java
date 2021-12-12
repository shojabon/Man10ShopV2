package com.shojabon.man10shopv2.dataClass.shopFunctions.mQuest;

import ToolMenu.BooleanInputMenu;
import ToolMenu.ConfirmationMenu;
import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@ShopFunctionDefinition(
        name = "クエストをリフレッシュ",
        explanation = {"強制的にクエストを更新する"},
        enabledShopType = {Man10ShopType.QUEST},
        iconMaterial = Material.REDSTONE_BLOCK,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class MQuestRefreshQuest extends ShopFunction{

    //variables

    //init
    public MQuestRefreshQuest(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }

    @Override
    public void init() {
        shop.mQuestFunction.refreshQuests(shop.mQuestCountFunction.questCount.get());
    }

    //functions



    //====================
    // settings
    //====================

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //number input menu
            ConfirmationMenu menu = new ConfirmationMenu("§a確認", plugin);
            menu.setOnCloseEvent(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnCancel(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnConfirm(newValue -> {
                shop.mQuestFunction.refreshQuests(shop.mQuestCountFunction.questCount.get());
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
            });
            menu.open(player);
        });
        return item;
    }

}
