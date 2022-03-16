package com.shojabon.man10shopv2.menus.storage;

import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.menus.InOutSelectorMenu;
import com.shojabon.man10shopv2.menus.ShopMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class ItemStorageMenu extends SInventory{
    
    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;
    int showingCount = 0;
    boolean fillItem;

    public ItemStorageMenu(boolean fillItem, Player p, Man10Shop shop, Man10ShopV2 plugin){
        super(new SStringBuilder().darkGray().text(shop.name.getName() + "倉庫 ").green().text("上限").text(String.valueOf(shop.storage.storageSize)).text("個").build(), 6, plugin);
        this.player = p;
        this.fillItem = fillItem;
        this.shop = shop;
        this.plugin = plugin;
        

    }

    public void renderMenu(){
        if(!fillItem) return;
        int nextSlot = 0;

        int stacks = shop.storage.itemCount/shop.targetItem.getTargetItem().getMaxStackSize();
        if(stacks > 6*9) stacks = 6*9;
        ItemStack targetItemSingle = shop.targetItem.getTargetItem().build().clone();
        SItemStack maxStackedItem = new SItemStack(targetItemSingle).setAmount(shop.targetItem.getTargetItem().getMaxStackSize());
        for(int i = 0; i < stacks; i++){
            setItem(i, maxStackedItem.build());
            nextSlot++;
        }

        //add remaining non full stacked item
        if(stacks != 6*9){
            int remainingItemCount = shop.storage.itemCount - stacks*shop.targetItem.getTargetItem().getMaxStackSize();
            setItem(nextSlot, new SItemStack(targetItemSingle.clone()).setAmount(remainingItemCount).build());
        }
        renderInventory();
        showingCount = countItems();
    }

    public int countItems(){
        int result = 0;
        for(int i = 0; i < 6*9; i++){
            ItemStack item = activeInventory.getItem(i);
            if(item == null) continue;
            if(!new SItemStack(item).getItemTypeMD5(true).equals(shop.targetItem.getTargetItem().getItemTypeMD5(true))) continue;
            result += item.getAmount();
        }
        return result;
    }

    public void registerEvents(){
        setOnClickEvent(e -> {
            if(e.getAction() == InventoryAction.HOTBAR_SWAP){
                e.setCancelled(true);
                return;
            }
            if(e.getCurrentItem() == null) return;
            if(e.getClickedInventory() == null) return;
            SItemStack item = new SItemStack(e.getCurrentItem());
            if(!item.getItemTypeMD5(true).equals(shop.targetItem.getTargetItem().getItemTypeMD5(true))){
                e.setCancelled(true);
                return;
            }
            //if bundle and right click
            if(item.getType() == Material.BUNDLE && e.getAction() == InventoryAction.PICKUP_HALF) {
                e.setCancelled(true);
                return;
            }
            //if item exceeds items storage size
            int selectedItemCount = new SItemStack(e.getCurrentItem()).getAmount();
            int diff = countItems() - showingCount;
            int estimatedNewStorageCount = diff + shop.storage.itemCount;
            if(estimatedNewStorageCount + selectedItemCount > shop.storage.storageSize && e.getClickedInventory().getType() != InventoryType.CHEST){
                player.sendMessage(Man10ShopV2.prefix + "§c§l倉庫のサイズ上限を越します");
                e.setCancelled(true);
                return;
            }
        });

        setAfterInventoryOpenEvents(e -> shop.currentlyEditingStorage = true);

        setAsyncOnForcedCloseEvent(e -> {
            int diff = countItems() - showingCount;
            if(diff < 0){
                //if item taken
                if(shop.storage.removeItemCount(-1*diff)){
                    Man10ShopV2API.log(shop.shopId, "storageTakeOut", diff, player.getName(), player.getUniqueId()); //log
                    player.sendMessage(Man10ShopV2.prefix + "§a§lアイテムを" + -1*diff + "個取り出しました");
                }else{
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                }
            }else if (diff > 0){
                //if item added
                if(shop.storage.removeItemCount(-1*diff)){

                    Man10ShopV2API.log(shop.shopId, "storageTakeIn", diff, player.getName(), player.getUniqueId()); //log
                    player.sendMessage(Man10ShopV2.prefix + "§a§lアイテムを" + diff + "個をしまいました");
                }else{
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                }
            }
            shop.currentlyEditingStorage = false;
            //diff range excludes 0 (no change)
        });

        //reopen selector
        setAsyncOnCloseEvent(e -> {
            InOutSelectorMenu menu = new InOutSelectorMenu(player, shop, plugin);
            menu.setOnClose(ee -> new ShopMainMenu(player, shop, plugin).open(player));
            menu.setOnInClicked(ee -> {
                //editing storage
                if(shop.currentlyEditingStorage){
                    player.sendMessage(Man10ShopV2.prefix + "§c§lこのショップは現在倉庫編集中です");
                    return;
                }
                new ItemStorageMenu(false, player, shop, plugin).open(player);
            });
            menu.setOnOutClicked(ee -> {
                //editing storage
                if(shop.currentlyEditingStorage){
                    player.sendMessage(Man10ShopV2.prefix + "§c§lこのショップは現在倉庫編集中です");
                    return;
                }
                new ItemStorageMenu(true, player, shop, plugin).open(player);
            });
            menu.setInText("倉庫にアイテムを入れる");
            menu.setOutText("倉庫からアイテムを出す");
            menu.open(player);
        });
    }

}
