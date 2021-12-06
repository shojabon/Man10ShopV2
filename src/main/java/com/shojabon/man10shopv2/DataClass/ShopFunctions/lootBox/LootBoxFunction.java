package com.shojabon.man10shopv2.DataClass.ShopFunctions.lootBox;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.DataClass.lootBox.LootBox;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.Settings.lootBoxSettings.LootBoxGroupSelectorMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class LootBoxFunction extends ShopFunction {

    //variables


    //init
    public LootBoxFunction(Man10Shop shop) {
        super(shop);
    }


    //functions

    //====================
    // settings
    //====================

    public LootBox getLootBox(){
        YamlConfiguration currentSetting = getSettingYaml("lootbox.data");
        if(currentSetting == null) return new LootBox();

        //load groups
        LootBox result = new LootBox();
        result.loadLootBox(currentSetting);

        return result;
    }

    public boolean setLootBox(LootBox box){
        //save group
        if(getLootBox() == box) return true;
        return setSetting("lootbox.data", box.exportLootBox());
    }

    @Override
    public Man10ShopType[] enabledShopTypes() {
        return new Man10ShopType[]{Man10ShopType.LOOT_BOX};
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
        if(!getLootBox().canPlay()){
            p.sendMessage(Man10ShopV2.prefix + "§c§lガチャの設定が完了してません");
            return false;
        }
        return true;
    }

    @Override
    public SInventoryItem getAdminSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.CAULDRON).setDisplayName("§e§lアイテムグループ設定");
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(getLootBox().groupData.size()).text(" グループ").build());

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
            sInventory.moveToMenu(player, new LootBoxGroupSelectorMenu(player, shop, shop.lootBoxFunction.getLootBox(), plugin));

        });


        return inventoryItem;
    }

}
