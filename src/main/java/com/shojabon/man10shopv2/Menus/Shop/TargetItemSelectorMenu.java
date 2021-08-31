package com.shojabon.man10shopv2.Menus.Shop;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.Shop.ShopMainMenu;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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


        SInventoryItem target = new SInventoryItem(shop.targetItem.build());
        target.clickable(false);
        setItem(13, target);
        renderInventory();
    }

    public void registerEvents(){
        setOnClickEvent(e -> {
            e.setCancelled(true);
            if(e.getCurrentItem() == null) return;
            ItemStack newTargetItem = new SItemStack(e.getCurrentItem()).getTypeItem();
            if(newTargetItem == null) return;

            SInventory.threadPool.execute(()->{
                boolean changeResult = shop.setTargetItem(player, newTargetItem);
                if(!changeResult) return;

                Man10ShopV2API.log(shop.shopId, "itemTypeChange", new SItemStack(newTargetItem).getItemTypeMD5(), player.getName(), player.getUniqueId()); //log
                player.sendMessage(Man10ShopV2.prefix + "§a§lアイテムが変更されました");
                renderMenu();
            });

        });

        setOnCloseEvent(e -> moveToMenu(player, new ShopMainMenu(player, shop, plugin)));
    }

}
