package com.shojabon.man10shopv2.menus;

import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class TargetItemSelectorMenu extends SInventory{
    
    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;

    public TargetItemSelectorMenu(Player p, Man10Shop shop, Man10ShopV2 plugin){
        super(new SStringBuilder().aqua().text("アイテムをクリックしてください").build(), 3, plugin);
        this.player = p;
        this.shop = shop;
        this.plugin = plugin;


    }

    public void renderMenu(){
        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        fillItem(background);


        SInventoryItem target = new SInventoryItem(shop.targetItem.getTargetItem().build());
        target.clickable(false);
        setItem(13, target);
        renderInventory();
    }

    public void registerEvents(){
        setOnClickEvent(e -> {
            e.setCancelled(true);
            if(e.getCurrentItem() == null) return;
            if(e.getClickedInventory().getType() == InventoryType.CHEST) return;
            ItemStack newTargetItem = new SItemStack(e.getCurrentItem()).getTypeItem(true);
            if(newTargetItem == null) return;
            if(shop.currentlyEditingStorage){
                player.sendMessage(Man10ShopV2.prefix + "§c§l現在倉庫編集中です");
                return;
            }

            SInventory.threadPool.execute(()->{

                if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
                    player.sendMessage(Man10ShopV2.prefix + "§c§lあなたにはこの項目を設定する権限がありません");
                    return;
                }
                if(!new SItemStack(e.getCurrentItem()).getItemTypeMD5(true).equals(shop.targetItem.getTargetItem().getItemTypeMD5(true)) && shop.storage.itemCount != 0 && !shop.admin){
                    player.sendMessage(Man10ShopV2.prefix + "§c§lショップ在庫があるときは取引アイテムを変更することはできません");
                    return;
                }

                if(!shop.targetItem.setTargetItem(newTargetItem)) {
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                    return;
                }

                Man10ShopV2API.log(shop.shopId, "itemTypeChange", new SItemStack(newTargetItem).getItemTypeMD5(true), player.getName(), player.getUniqueId()); //log
                player.sendMessage(Man10ShopV2.prefix + "§a§lアイテムが変更されました");
                renderMenu();
            });

        });

        setOnCloseEvent(e -> new ShopMainMenu(player, shop, plugin).open(player));
    }

}
