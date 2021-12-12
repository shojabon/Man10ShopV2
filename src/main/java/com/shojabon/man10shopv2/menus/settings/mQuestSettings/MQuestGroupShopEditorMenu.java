package com.shojabon.man10shopv2.menus.settings.mQuestSettings;

import ToolMenu.ConfirmationMenu;
import ToolMenu.LargeSInventoryMenu;
import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.quest.MQuest;
import com.shojabon.man10shopv2.dataClass.quest.MQuestGroupData;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.menus.AdminShopSelectorMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.UUID;

public class MQuestGroupShopEditorMenu extends LargeSInventoryMenu {

    Man10ShopV2 plugin;
    Player player;
    Man10Shop shop;
    MQuest quest;
    int groupId;

    public MQuestGroupShopEditorMenu(Player p, Man10Shop shop, MQuest quest, int groupId, Man10ShopV2 plugin){
        super("§6§lアイテム設定", plugin);
        this.player = p;
        this.plugin = plugin;
        this.shop = shop;
        this.quest = quest;
        this.groupId = groupId;
    }

    public void renderMenu(){
        ArrayList<SInventoryItem> items = new ArrayList<>();
        MQuestGroupData groupData = quest.groupData.get(groupId);

        for(UUID shopId: groupData.shopCountDictionary.keySet()){
            Man10Shop targetShop = Man10ShopV2.api.getShop(shopId);
            SItemStack icon = new SItemStack(Material.BARRIER).setDisplayName("§c§lショップが存在していません");
            if(targetShop != null) icon = new SItemStack(targetShop.targetItem.getTargetItem().build().clone()).setDisplayName(shop.name.getName());
            //warning
            float percentage = (float) groupData.shopCountDictionary.get(shopId)/groupData.getTotalItemCount();
            icon.addLore("§d========グループ内確率========");
            icon.addLore("§d" + (percentage*100) + "%");
            icon.addLore("§d" + groupData.shopCountDictionary.get(shopId) + "/" + groupData.getTotalItemCount());
            icon.addLore("");
            icon.addLore("§d========最終確率========");
            icon.addLore("§d" + (groupData.getPercentage()*percentage) + "%");
            icon.addLore("");
            icon.addLore("§c§l右クリックで削除");
            icon.addLore("§a§l左クリックで確率設定");

            //icon.addLore("§c§l右クリックで削除");
            SInventoryItem item = new SInventoryItem(icon.build());
            item.clickable(false);

            item.setAsyncEvent(e -> {
                if(e.getClick() != ClickType.RIGHT) return;
                ConfirmationMenu menu = new ConfirmationMenu("グループを消去しますか？", plugin);
                menu.setOnConfirm(ee -> {
                    groupData.shopCountDictionary.remove(shopId);
                    if(!shop.mQuestFunction.quest.set(quest)){
                        player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                        return;
                    }
                    new MQuestGroupShopEditorMenu(player, shop, quest, groupId, plugin).open(player);
                });
                menu.setOnCancel(ee -> {new MQuestGroupShopEditorMenu(player, shop, quest, groupId, plugin).open(player);});
                menu.setOnCloseEvent(ee -> {new MQuestGroupShopEditorMenu(player, shop, quest, groupId, plugin).open(player);});
                menu.open(player);
            });

            item.setAsyncEvent(e -> {
                if(e.getClick() != ClickType.LEFT) return;

                NumericInputMenu numberMenu = new NumericInputMenu("アイテム個数を入力してください", plugin);
                numberMenu.setAllowZero(false);
                numberMenu.setDefaultValue(groupData.shopCountDictionary.get(shopId));

                numberMenu.setOnConfirm(ee -> {
                    groupData.shopCountDictionary.put(shopId, ee);
                    if(!shop.mQuestFunction.quest.set(quest)){
                        player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                        return;
                    }
                    new MQuestGroupShopEditorMenu(player, shop, quest, groupId, plugin).open(player);
                });
                numberMenu.setOnCancel(ee -> new MQuestGroupShopEditorMenu(player, shop, quest, groupId, plugin).open(player));
                numberMenu.setOnCloseEvent(ee -> new MQuestGroupShopEditorMenu(player, shop, quest, groupId, plugin).open(player));

                numberMenu.open(player);
            });

            items.add(item);
        }
        setItems(items);
        setOnCloseEvent(ee -> new MQuestGroupSelectorMenu(player, shop, quest, plugin).open(player));
    }

    public void afterRenderMenu() {
        renderInventory(0);
        MQuestGroupData groupData = quest.groupData.get(groupId);

        SInventoryItem addPrice = new SInventoryItem(new SItemStack(Material.DROPPER).setDisplayName("§a§lアイテム種を追加").build()).clickable(false);
        addPrice.setAsyncEvent(e -> {

            if(groupData.shopCountDictionary.size() >= 45){
                player.sendMessage(Man10ShopV2.prefix + "§c§l45ショップ種以上は追加できません");
                return;
            }

            AdminShopSelectorMenu itemSelector = new AdminShopSelectorMenu(player, "その他", plugin);
            itemSelector.setOnClick(selectedShop -> {
                if(groupData.shopCountDictionary.containsKey(selectedShop.shopId)){
                    player.sendMessage(Man10ShopV2.prefix + "§c§lこのショップはすでに登録されています");
                    new MQuestGroupShopEditorMenu(player, shop, quest, groupId, plugin).open(player);
                    return;
                }
                groupData.shopCountDictionary.put(selectedShop.shopId, 1);

                //update database here
                if(!shop.mQuestFunction.quest.set(quest)){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                    return;
                }
                new MQuestGroupShopEditorMenu(player, shop, quest, groupId, plugin).open(player);
            });

            itemSelector.setOnCloseEvent(ee -> {
                new MQuestGroupShopEditorMenu(player, shop, quest, groupId, plugin).open(player);
            });


            itemSelector.open(player);
        });


        setItem(51, addPrice);
        renderInventory();
    }
}
