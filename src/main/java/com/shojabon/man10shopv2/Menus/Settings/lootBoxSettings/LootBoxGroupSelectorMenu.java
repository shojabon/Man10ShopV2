package com.shojabon.man10shopv2.Menus.Settings.lootBoxSettings;

import ToolMenu.ConfirmationMenu;
import ToolMenu.LargeSInventoryMenu;
import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.lootBox.LootBox;
import com.shojabon.man10shopv2.DataClass.lootBox.LootBoxGroupData;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.Settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.*;

public class LootBoxGroupSelectorMenu extends LargeSInventoryMenu {

    Man10ShopV2 plugin;
    Player player;
    Man10Shop shop;
    LootBox lootBox;

    Material[] defaultItemGroups = {
            Material.BEACON,
            Material.NETHERITE_BLOCK,
            Material.DIAMOND_BLOCK,
            Material.GOLD_BLOCK,
            Material.IRON_BLOCK,
            Material.COPPER_BLOCK
    };

    public LootBoxGroupSelectorMenu(Player p, Man10Shop shop, LootBox lootBox, Man10ShopV2 plugin){
        super("§6§lアイテムグループ一覧", plugin);
        this.player = p;
        this.plugin = plugin;
        this.shop = shop;
        this.lootBox = lootBox;
    }

    public void renderMenu(){
        ArrayList<SInventoryItem> items = new ArrayList<>();

        
        for(int i = 0; i < lootBox.groupData.size(); i++){

            LootBoxGroupData data = lootBox.groupData.get(i);

            SItemStack icon = new SItemStack(data.icon);
            icon.setDisplayName("§a§lグループ:" + i);

            icon.addLore("§d確率" + data.getPercentage() + "%");
            icon.addLore("§d" + data.percentageWeight + "/10000");
            //warning
            if(lootBox.getLackingWeight() < 0){
                icon.addLore("§cウェイト超過: " + Math.abs(lootBox.getLackingWeight()));
            }else if(lootBox.getLackingWeight() > 0){
                icon.addLore("§cウェイト不足: " + Math.abs(lootBox.getLackingWeight()));
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
                ArrayList<LootBoxGroupData> newData = lootBox.groupData;
                ConfirmationMenu menu = new ConfirmationMenu("グループを消去しますか？", plugin);
                menu.setOnConfirm(ee -> {
                    newData.remove(finalI);
                    if(!shop.lootBoxItemGroupFunction.setLootBox(lootBox)){
                        player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                        return;
                    }
                    menu.moveToMenu(player, new LootBoxGroupSelectorMenu(player, shop, lootBox, plugin));
                });
                menu.setOnCancel(ee -> {menu.moveToMenu(player, new LootBoxGroupSelectorMenu(player, shop, lootBox, plugin));});
                menu.setOnCloseEvent(ee -> {menu.moveToMenu(player, new LootBoxGroupSelectorMenu(player, shop, lootBox, plugin));});
                moveToMenu(player, menu);
            });

            item.setAsyncEvent(e -> {
                if(e.getClick() != ClickType.LEFT) return;

                moveToMenu(player, new LootBoxGroupItemEditorMenu(player, shop, lootBox, finalI, plugin));
            });

            item.setAsyncEvent(e -> {
                if(e.getClick() != ClickType.MIDDLE) return;
                ArrayList<LootBoxGroupData> newData = lootBox.groupData;

                NumericInputMenu numberMenu = new NumericInputMenu("N x 0.01%で入力してくださ", plugin);
                numberMenu.setMaxValue(10000);
                numberMenu.setAllowZero(true);
                numberMenu.setDefaultValue(data.percentageWeight);

                numberMenu.setOnConfirm(ee -> {
                    newData.get(finalI).percentageWeight = ee;
                    if(!shop.lootBoxItemGroupFunction.setLootBox(lootBox)){
                        player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                        return;
                    }
                    moveToMenu(player, new LootBoxGroupSelectorMenu(player, shop, lootBox, plugin));
                });
                numberMenu.setOnCancel(ee -> numberMenu.moveToMenu(player, new LootBoxGroupSelectorMenu(player, shop, lootBox, plugin)));
                numberMenu.setOnCloseEvent(ee -> numberMenu.moveToMenu(player, new LootBoxGroupSelectorMenu(player, shop, lootBox, plugin)));

                moveToMenu(player, numberMenu);
            });

            items.add(item);
        }
        setItems(items);
        setOnCloseEvent(ee -> moveToMenu(player, new SettingsMainMenu(player, shop, shop.lootBoxItemGroupFunction.settingCategory(), plugin)));
    }

    public Material getRandomMaterial(){
        for(int i = 0; i < 1024; i++){
            boolean alreadyExists = false;
            Material checking = Material.values()[new Random().nextInt(Material.values().length-1)];
            if(!checking.isItem()) continue;
            for(LootBoxGroupData data: lootBox.groupData){
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
            ArrayList<LootBoxGroupData> newItemGroups = lootBox.groupData;
            if(newItemGroups.size() >= 45){
                player.sendMessage(Man10ShopV2.prefix + "§c§l45グループ以上は追加できません");
                return;
            }
            Material addingMaterial;
            if(newItemGroups.size() >= defaultItemGroups.length) {
                addingMaterial = getRandomMaterial();
            }else {
                addingMaterial = defaultItemGroups[newItemGroups.size()];
            }
            if(addingMaterial == null) return;
            newItemGroups.add(new LootBoxGroupData(addingMaterial, 0));

            if(!shop.lootBoxItemGroupFunction.setLootBox(lootBox)){
                player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return;
            }

            moveToMenu(player, new LootBoxGroupSelectorMenu(player, shop, lootBox, plugin));
        });


        setItem(51, addPrice);
        renderInventory();
    }
}
