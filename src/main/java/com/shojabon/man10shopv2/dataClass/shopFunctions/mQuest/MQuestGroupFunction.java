package com.shojabon.man10shopv2.dataClass.shopFunctions.mQuest;

import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.dataClass.quest.MQuest;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.menus.settings.mQuestSettings.MQuestGroupSelectorMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
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

    MQuest quest = null;


    //init
    public MQuestGroupFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions

    public void refreshQuests(int questCount){
        getQuest().currentQuests = getQuest().getQuests(questCount);
        SInventory.closeInventoryGroup(shop.getShopId(), (JavaPlugin) shop.plugin);
    }

    //====================
    // settings
    //====================

    public MQuest getQuest(){
        if(quest != null) return quest;
        YamlConfiguration currentSetting = getSettingYaml("mquest.data");
        if(currentSetting == null) return new MQuest();

        //load groups
        MQuest result = new MQuest();
        result.loadLootBox(currentSetting);
        quest = result;
        return quest;
    }

    public boolean setQuest(MQuest box){
        //save group
        if(setSetting("mquest.data", box.exportQuest())){
            quest = box;
            return true;
        }
        return false;
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        if(!getQuest().isAvailable()){
            p.sendMessage(Man10ShopV2.prefix + "§c§lクエストの設定が終わっていません");
            return false;
        }
        return true;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //confirmation menu
            new MQuestGroupSelectorMenu(player, shop, shop.mQuestFunction.getQuest(), plugin).open(player);

        });
        return item;
    }

}
