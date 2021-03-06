package com.shojabon.man10shopv2.shopFunctions.mQuest;

import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.dataClass.quest.MQuest;
import com.shojabon.man10shopv2.dataClass.quest.MQuestGroupData;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.menus.settings.mQuestSettings.MQuestGroupSelectorMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

@ShopFunctionDefinition(
        name = "クエストグループの設定",
        explanation = {"最上位グループの設定", "グループ数が0の場合は使用不可"},
        enabledShopType = {Man10ShopType.QUEST},
        iconMaterial = Material.CAULDRON,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class MQuestGroupFunction extends ShopFunction {

    //variables
    public Man10ShopSetting<MQuest> quest = new Man10ShopSetting<>("mquest.data", new MQuest());


    //init
    public MQuestGroupFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions

    public void refreshQuests(int questCount){
        SInventory.closeInventoryGroup(shop.getShopId(), (JavaPlugin) shop.plugin);
        quest.get().currentQuests = quest.get().getQuests(questCount);
        disableAllLinkedShops();
        for(UUID uuid: quest.get().currentQuests){
            Man10ShopV2.api.getShop(uuid).shopEnabled.enabled.set(true);
        }
        SInventory.closeInventoryGroup(shop.getShopId(), (JavaPlugin) shop.plugin);
    }

    public void disableAllLinkedShops(){
        MQuest m = quest.get();
        for(MQuestGroupData group: m.groupData){
            for(UUID targetShop: group.shopCountDictionary.keySet()){
                Man10ShopV2.api.getShop(targetShop).shopEnabled.enabled.set(false);
            }
        }
    }

    //====================
    // settings
    //====================

    @Override
    public boolean isAllowedToUseShop(Player p) {
        if(!quest.get().isAvailable()){
            p.sendMessage(Man10ShopV2.prefix + "§c§lクエストの設定が終わっていません");
            return false;
        }
        return true;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //confirmation menu
            new MQuestGroupSelectorMenu(player, shop, shop.mQuestFunction.quest.get(), plugin).open(player);

        });
        return item;
    }

}
