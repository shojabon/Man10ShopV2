package com.shojabon.man10shopv2.Menus.Settings.mQuestSettings;

import ToolMenu.ConfirmationMenu;
import ToolMenu.LargeSInventoryMenu;
import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.quest.MQuest;
import com.shojabon.man10shopv2.DataClass.quest.MQuestGroupData;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.Settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.Random;

public class MQuestGroupSelectorMenu extends LargeSInventoryMenu {

    Man10ShopV2 plugin;
    Player player;
    Man10Shop shop;
    MQuest quest;

    Material[] defaultItemGroups = {
            Material.BEACON,
            Material.NETHERITE_BLOCK,
            Material.DIAMOND_BLOCK,
            Material.GOLD_BLOCK,
            Material.IRON_BLOCK,
            Material.COPPER_BLOCK
    };

    public MQuestGroupSelectorMenu(Player p, Man10Shop shop, MQuest quest, Man10ShopV2 plugin){
        super("§6§lアイテムグループ一覧", plugin);
        this.player = p;
        this.plugin = plugin;
        this.shop = shop;
        this.quest = quest;
    }

    public void renderMenu(){
        ArrayList<SInventoryItem> items = new ArrayList<>();

        
        for(int i = 0; i < quest.groupData.size(); i++){

            MQuestGroupData data = quest.groupData.get(i);

            SItemStack icon = new SItemStack(data.icon);
            icon.setDisplayName("§a§lグループ:" + i);

            icon.addLore("§d確率" + data.getPercentage() + "%");
            icon.addLore("§d" + data.percentageWeight + "/10000");
            icon.addLore("");
            icon.addLore("§b§lクエスト数:" + data.shopCountDictionary.size() + "種類");
            //warning
            if(quest.getLackingWeight() < 0){
                icon.addLore("§cウェイト超過: " + Math.abs(quest.getLackingWeight()));
            }else if(quest.getLackingWeight() > 0){
                icon.addLore("§cウェイト不足: " + Math.abs(quest.getLackingWeight()));
            }
            icon.addLore("");
            icon.addLore("§c§l右クリックで削除");
            icon.addLore("§b§lホイールクリックで確率調整");
            icon.addLore("§a§l左クリックでアイテム調整");

            //icon.addLore("§c§l右クリックで削除");
            SInventoryItem item = new SInventoryItem(icon.build());
            item.clickable(false);

            int finalI = i;
            item.setAsyncEvent(e -> {
                if(e.getClick() != ClickType.RIGHT) return;
                ArrayList<MQuestGroupData> newData = quest.groupData;
                ConfirmationMenu menu = new ConfirmationMenu("グループを消去しますか？", plugin);
                menu.setOnConfirm(ee -> {
                    newData.remove(finalI);
                    if(!shop.mQuestFunction.setQuest(quest)){
                        player.sendMessage(Man10ShopV2.gachaPrefix + "§c§l内部エラーが発生しました");
                        return;
                    }
                    menu.moveToMenu(player, new MQuestGroupSelectorMenu(player, shop, quest, plugin));
                });
                menu.setOnCancel(ee -> {menu.moveToMenu(player, new MQuestGroupSelectorMenu(player, shop, quest, plugin));});
                menu.setOnCloseEvent(ee -> {menu.moveToMenu(player, new MQuestGroupSelectorMenu(player, shop, quest, plugin));});
                moveToMenu(player, menu);
            });

            item.setAsyncEvent(e -> {
                if(e.getClick() != ClickType.LEFT) return;

                moveToMenu(player, new MQuestGroupShopEditorMenu(player, shop, quest, finalI, plugin));
            });

            item.setAsyncEvent(e -> {
                if(e.getClick() != ClickType.MIDDLE) return;
                ArrayList<MQuestGroupData> newData = quest.groupData;

                NumericInputMenu numberMenu = new NumericInputMenu("N x 0.01%で入力してくださ", plugin);
                numberMenu.setMaxValue(10000);
                numberMenu.setAllowZero(true);
                numberMenu.setDefaultValue(data.percentageWeight);

                numberMenu.setOnConfirm(ee -> {
                    newData.get(finalI).percentageWeight = ee;
                    if(!shop.mQuestFunction.setQuest(quest)){
                        player.sendMessage(Man10ShopV2.gachaPrefix + "§c§l内部エラーが発生しました");
                        return;
                    }
                    moveToMenu(player, new MQuestGroupSelectorMenu(player, shop, quest, plugin));
                });
                numberMenu.setOnCancel(ee -> numberMenu.moveToMenu(player, new MQuestGroupSelectorMenu(player, shop, quest, plugin)));
                numberMenu.setOnCloseEvent(ee -> numberMenu.moveToMenu(player, new MQuestGroupSelectorMenu(player, shop, quest, plugin)));

                moveToMenu(player, numberMenu);
            });

            items.add(item);
        }
        setItems(items);
        setOnCloseEvent(ee -> moveToMenu(player, new SettingsMainMenu(player, shop, shop.lootBoxFunction.settingCategory(), plugin)));
    }

    public Material getRandomMaterial(){
        for(int i = 0; i < 1024; i++){
            boolean alreadyExists = false;
            Material checking = Material.values()[new Random().nextInt(Material.values().length-1)];
            if(!checking.isItem()) continue;
            for(MQuestGroupData data: quest.groupData){
                if(data.icon == checking) {
                    alreadyExists = true;
                    break;
                }
            }
            if(alreadyExists) continue;
            return checking;
        }
        return null;
    }

    public void afterRenderMenu() {
        renderInventory(0);

        SInventoryItem addPrice = new SInventoryItem(new SItemStack(Material.DISPENSER).setDisplayName("§a§lアイテムグループを追加").build()).clickable(false);
        addPrice.setAsyncEvent(e -> {
            ArrayList<MQuestGroupData> newItemGroups = quest.groupData;
            if(newItemGroups.size() >= 45){
                player.sendMessage(Man10ShopV2.gachaPrefix + "§c§l45グループ以上は追加できません");
                return;
            }
            Material addingMaterial;
            if(newItemGroups.size() >= defaultItemGroups.length) {
                addingMaterial = getRandomMaterial();
            }else {
                addingMaterial = defaultItemGroups[newItemGroups.size()];
            }
            if(addingMaterial == null) return;
            newItemGroups.add(new MQuestGroupData(addingMaterial, 0));

            if(!shop.mQuestFunction.setQuest(quest)){
                player.sendMessage(Man10ShopV2.gachaPrefix + "§c§l内部エラーが発生しました");
                return;
            }

            moveToMenu(player, new MQuestGroupSelectorMenu(player, shop, quest, plugin));
        });


        setItem(51, addPrice);
        renderInventory();
    }
}
