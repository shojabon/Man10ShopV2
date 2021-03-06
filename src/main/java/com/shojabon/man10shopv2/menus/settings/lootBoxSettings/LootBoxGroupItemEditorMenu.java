package com.shojabon.man10shopv2.menus.settings.lootBoxSettings;

import ToolMenu.ConfirmationMenu;
import ToolMenu.LargeSInventoryMenu;
import ToolMenu.NumericInputMenu;
import ToolMenu.SingleItemStackSelectorMenu;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.lootBox.LootBox;
import com.shojabon.man10shopv2.dataClass.lootBox.LootBoxGroupData;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class LootBoxGroupItemEditorMenu extends LargeSInventoryMenu {

    Man10ShopV2 plugin;
    Player player;
    Man10Shop shop;
    LootBox lootBox;
    int groupId;

    public LootBoxGroupItemEditorMenu(Player p, Man10Shop shop, LootBox box, int groupId, Man10ShopV2 plugin){
        super("§6§lアイテム設定", plugin);
        this.player = p;
        this.plugin = plugin;
        this.shop = shop;
        this.lootBox = box;
        this.groupId = groupId;
    }

    public void renderMenu(){
        ArrayList<SInventoryItem> items = new ArrayList<>();
        LootBoxGroupData groupData = lootBox.groupData.get(groupId);

        for(String itemHash: groupData.itemCountDictionary.keySet()){

            SItemStack icon = new SItemStack(lootBox.itemDictionary.get(itemHash).clone());
            //warning
            float percentage = (float) groupData.itemCountDictionary.get(itemHash)/groupData.getTotalItemCount();
            icon.addLore("§d========グループ内確率========");
            icon.addLore("§d" + (percentage*100) + "%");
            icon.addLore("§d" + groupData.itemCountDictionary.get(itemHash) + "/" + groupData.getTotalItemCount());
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
                    groupData.itemCountDictionary.remove(itemHash);
                    if(!shop.lootBoxFunction.lootBox.set(lootBox)){
                        player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                        return;
                    }
                    new LootBoxGroupItemEditorMenu(player, shop, lootBox, groupId, plugin).open(player);
                });
                menu.setOnCancel(ee -> {new LootBoxGroupItemEditorMenu(player, shop, lootBox, groupId, plugin).open(player);});
                menu.setOnCloseEvent(ee -> {new LootBoxGroupItemEditorMenu(player, shop, lootBox, groupId, plugin).open(player);});
                menu.open(player);
            });

            item.setAsyncEvent(e -> {
                if(e.getClick() != ClickType.LEFT) return;

                NumericInputMenu numberMenu = new NumericInputMenu("アイテム個数を入力してください", plugin);
                numberMenu.setAllowZero(false);
                numberMenu.setDefaultValue(groupData.itemCountDictionary.get(itemHash));

                numberMenu.setOnConfirm(ee -> {
                    groupData.itemCountDictionary.put(itemHash, ee);
                    if(!shop.lootBoxFunction.lootBox.set(lootBox)){
                        player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                        return;
                    }
                    new LootBoxGroupItemEditorMenu(player, shop, lootBox, groupId, plugin).open(player);
                });
                numberMenu.setOnCancel(ee -> new LootBoxGroupItemEditorMenu(player, shop, lootBox, groupId, plugin).open(player));
                numberMenu.setOnCloseEvent(ee -> new LootBoxGroupItemEditorMenu(player, shop, lootBox, groupId, plugin).open(player));

                numberMenu.open(player);
            });

            items.add(item);
        }
        setItems(items);
        setOnCloseEvent(ee -> new LootBoxGroupSelectorMenu(player, shop, lootBox, plugin).open(player));
    }

    public void afterRenderMenu() {
        renderInventory(0);
        LootBoxGroupData groupData = lootBox.groupData.get(groupId);

        SInventoryItem addPrice = new SInventoryItem(new SItemStack(Material.DROPPER).setDisplayName("§a§lアイテム種を追加").build()).clickable(false);
        addPrice.setAsyncEvent(e -> {

            if(groupData.itemCountDictionary.size() >= 45){
                player.sendMessage(Man10ShopV2.prefix + "§c§l45アイテム種以上は追加できません");
                return;
            }

            SingleItemStackSelectorMenu itemSelector = new SingleItemStackSelectorMenu("追加するアイテム種を選択してください", new ItemStack(Material.DIAMOND), plugin);
            itemSelector.selectTypeItem(false);
            itemSelector.selectMaterial(false);
            itemSelector.setOnConfirm(selectedItem -> {
                SItemStack item = new SItemStack(selectedItem);
                if(groupData.itemCountDictionary.containsKey(item.getMD5())){
                    player.sendMessage(Man10ShopV2.prefix + "§c§lこのアイテム種はすでに登録されています");
                    new LootBoxGroupItemEditorMenu(player, shop, lootBox, groupId, plugin).open(player);
                    return;
                }
                lootBox.itemDictionary.put(item.getMD5(), selectedItem);
                groupData.itemCountDictionary.put(item.getMD5(), 1);

                //update database here
                if(!shop.lootBoxFunction.lootBox.set(lootBox)){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                    return;
                }
                new LootBoxGroupItemEditorMenu(player, shop, lootBox, groupId, plugin).open(player);
            });

            itemSelector.setOnCloseEvent(ee -> {
                new LootBoxGroupItemEditorMenu(player, shop, lootBox, groupId, plugin).open(player);
            });

            itemSelector.open(player);
        });


        setItem(51, addPrice);
        renderInventory();
    }
}
