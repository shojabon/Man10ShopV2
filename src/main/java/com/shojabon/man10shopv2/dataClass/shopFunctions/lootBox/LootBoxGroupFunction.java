package com.shojabon.man10shopv2.dataClass.shopFunctions.lootBox;

import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.LootBoxFunction;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.lootBox.LootBox;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.menus.settings.lootBoxSettings.LootBoxGroupSelectorMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
@ShopFunctionDefinition(
        name = "アイテムグループ設定",
        explanation = {"グループ/アイテムの設定"},
        enabledShopType = {Man10ShopType.LOOT_BOX},
        iconMaterial = Material.CAULDRON,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = false
)
public class LootBoxGroupFunction extends LootBoxFunction {

    //variables
    public Man10ShopSetting<LootBox> lootBox = new Man10ShopSetting<>("lootbox.data", new LootBox());


    //init
    public LootBoxGroupFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions

    //====================
    // settings
    //====================

    @Override
    public boolean isAllowedToUseShop(Player p) {
        if(!lootBox.get().canPlay()){
            p.sendMessage(Man10ShopV2.prefix + "§c§lガチャの設定が完了してません");
            return false;
        }
        return true;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //confirmation menu
            new LootBoxGroupSelectorMenu(player, shop, lootBox.get(), plugin).open(player);

        });
        return item;
    }

}
