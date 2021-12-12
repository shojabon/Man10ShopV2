package com.shojabon.man10shopv2.shopFunctions.mQuest;

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
        name = "クエストリフレッシュ設定",
        explanation = {"設定した分間毎にクエストを更新", "0の場合はなし"},
        enabledShopType = {Man10ShopType.QUEST},
        iconMaterial = Material.FLOWER_POT,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class MQuestQuestCountFunction extends ShopFunction{

    //variables

    //init
    public MQuestQuestCountFunction(Man10Shop shop, Man10ShopV2 plugin) {
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

    public Man10ShopSetting<Integer> questCount = new Man10ShopSetting<>("quest.refresh.amount", 1);


    @Override
    public boolean isFunctionEnabled() {
        return questCount.get() != 0;
    }

    @Override
    public String currentSettingString() {
        return questCount.get() + "個";
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //number input menu
            NumericInputMenu menu = new NumericInputMenu("§aクエスト数", plugin);
            menu.setOnCloseEvent(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnCancel(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnConfirm(newValue -> {
                if(!questCount.set(newValue)){
                    warn(player, "内部エラーが発生しました");
                }
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
            });
            menu.open(player);

        });
        return item;
    }

}
