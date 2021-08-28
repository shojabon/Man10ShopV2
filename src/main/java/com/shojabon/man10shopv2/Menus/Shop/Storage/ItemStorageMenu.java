package com.shojabon.man10shopv2.Menus.Shop.Storage;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.Shop.InOutSelectorMenu;
import com.shojabon.man10shopv2.Menus.Shop.ShopMainMenu;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class ItemStorageMenu {

    SInventory inventory;
    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;
    int showingCount = 0;
    boolean fillItem;

    public ItemStorageMenu(Player p, Man10Shop shop, Man10ShopV2 plugin, boolean fillItem){
        this.player = p;
        this.fillItem = fillItem;
        this.shop = shop;
        this.plugin = plugin;
        inventory = new SInventory(new SStringBuilder().darkGray().text(shop.name + "倉庫 ").green().text("上限").text(String.valueOf(shop.storageSize)).text("個").build(), 6, plugin);

        registerEvents();
        if(fillItem) renderMenu();

    }

    public SInventory getInventory() {
        return inventory;
    }

    public void renderMenu(){
        int nextSlot = 0;

        int stacks = shop.itemCount/shop.targetItem.getMaxStackSize();
        if(stacks > 6*9) stacks = 6*9;
        ItemStack targetItemSingle = shop.targetItem.build().clone();
        SItemStack maxStackedItem = new SItemStack(targetItemSingle).setAmount(shop.targetItem.getMaxStackSize());
        for(int i = 0; i < stacks; i++){
            inventory.setItem(i, maxStackedItem.build());
            nextSlot++;
        }

        //add remaining non full stacked item
        if(stacks != 6*9){
            int remainingItemCount = shop.itemCount - stacks*shop.targetItem.getMaxStackSize();
            inventory.setItem(nextSlot, new SItemStack(targetItemSingle.clone()).setAmount(remainingItemCount).build());
        }
        inventory.renderInventory();
        showingCount = countItems();
    }

    public int countItems(){
        int result = 0;
        for(int i = 0; i < 6*9; i++){
            ItemStack item = inventory.activeInventory.getItem(i);
            if(item == null) continue;
            if(!new SItemStack(item).getItemTypeMD5().equals(shop.targetItem.getItemTypeMD5())) continue;
            result += item.getAmount();
        }
        return result;
    }

    public void registerEvents(){
        inventory.setOnClickEvent(e -> {
            if(e.getCurrentItem() == null) return;
            if(e.getClickedInventory() == null) return;
            if(!new SItemStack(e.getCurrentItem()).getItemTypeMD5().equals(shop.targetItem.getItemTypeMD5())) {
                e.setCancelled(true);
                return;
            }
            //if item exceeds items storage size
            int selectedItemCount = new SItemStack(e.getCurrentItem()).getAmount();
            int diff = countItems() - showingCount;
            int estimatedNewStorageCount = diff + shop.itemCount;
            if(estimatedNewStorageCount + selectedItemCount > shop.storageSize && e.getClickedInventory().getType() != InventoryType.CHEST){
                player.sendMessage(Man10ShopV2.prefix + "§c§l倉庫のサイズ上限を越します");
                e.setCancelled(true);
                return;
            }
        });

        inventory.setOnForcedCloseEvent(e -> {
            int diff = countItems() - showingCount;
            if(diff < 0){
                //if item taken
                if(shop.removeItemCount(-1*diff)){
                    player.sendMessage(Man10ShopV2.prefix + "§a§lアイテムを" + -1*diff + "個取り出しました");
                }else{
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                }
            }else if (diff > 0){
                //if item added
                if(shop.removeItemCount(-1*diff)){
                    player.sendMessage(Man10ShopV2.prefix + "§a§lアイテムを" + diff + "個をしまいました");
                }else{
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                }
            }
            //diff range excludes 0 (no change)
        });
        inventory.setOnCloseEvent(e -> {
            InOutSelectorMenu menu = new InOutSelectorMenu(player, shop, plugin);
            menu.setOnClose(ee -> menu.getInventory().moveToMenu(player, new ShopMainMenu(player, shop, plugin).getInventory()));
            menu.setOnInClicked(ee -> menu.getInventory().moveToMenu(player, new ItemStorageMenu(player, shop, plugin, false).getInventory()));
            menu.setOnOutClicked(ee -> menu.getInventory().moveToMenu(player, new ItemStorageMenu(player, shop, plugin, true).getInventory()));
            menu.setInText("倉庫にアイテムを入れる");
            menu.setOutText("倉庫からアイテムを出す");
            inventory.moveToMenu(player, menu.getInventory());
        });
    }

}
