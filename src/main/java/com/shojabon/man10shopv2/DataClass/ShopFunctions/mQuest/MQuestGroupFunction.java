package com.shojabon.man10shopv2.DataClass.ShopFunctions.mQuest;

import com.shojabon.man10shopv2.DataClass.LootBoxFunction;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.DataClass.quest.MQuest;
import com.shojabon.man10shopv2.DataClass.quest.MQuestGroupData;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.Settings.mQuestSettings.MQuestGroupSelectorMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class MQuestGroupFunction extends ShopFunction {

    //variables

    MQuest quest = null;


    //init
    public MQuestGroupFunction(Man10Shop shop) {
        super(shop);
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
    public Man10ShopType[] enabledShopTypes() {
        return new Man10ShopType[]{Man10ShopType.QUEST};
    }

    @Override
    public String settingCategory() {
        return "一般設定";
    }

    @Override
    public boolean hasPermissionToEdit(UUID uuid) {
        return shop.permission.hasPermissionAtLeast(uuid, Man10ShopPermission.MODERATOR);
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
    public SInventoryItem getAdminSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.CAULDRON).setDisplayName("§e§lクエストグループの設定");
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(getQuest().groupData.size()).text(" グループ").build());

        item.addLore("");
        item.addLore("§f最上位グループの設定");
        item.addLore("§fグループ数が0の場合は使用不可");

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);

        inventoryItem.setEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //confirmation menu
            sInventory.moveToMenu(player, new MQuestGroupSelectorMenu(player, shop, shop.mQuestFunction.getQuest(), plugin));

        });



        return inventoryItem;
    }

}
