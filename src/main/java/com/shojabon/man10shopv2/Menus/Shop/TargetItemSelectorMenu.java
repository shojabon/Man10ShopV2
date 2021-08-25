package com.shojabon.man10shopv2.Menus.Shop;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.Shop.ShopMainMenu;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TargetItemSelectorMenu {

    SInventory inventory;
    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;

    public TargetItemSelectorMenu(Player p, Man10Shop shop, Man10ShopV2 plugin){
        this.player = p;
        this.shop = shop;
        this.plugin = plugin;
        inventory = new SInventory(new SStringBuilder().aqua().text("変更したいアイテムをクリックしてください").build(), 3, plugin);
        renderMenu();
        registerEvents();


    }

    public SInventory getInventory() {
        return inventory;
    }

    public void renderMenu(){
        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        inventory.fillItem(background);


        SInventoryItem target = new SInventoryItem(shop.targetItem.build());
        target.clickable(false);
        inventory.setItem(13, target);
        inventory.renderInventory();
    }

    public void registerEvents(){
        inventory.setOnClickEvent(e -> {
            e.setCancelled(true);
            if(e.getCurrentItem() == null) return;
            ItemStack newTargetItem = new SItemStack(e.getCurrentItem()).getTypeItem();
            if(newTargetItem == null) return;

            boolean changeResult = shop.setTargetItem(player, newTargetItem);
            if(!changeResult) return;
            player.sendMessage(Man10ShopV2.prefix + "§a§lアイテムが変更されました");
            renderMenu();

        });

        inventory.setOnCloseEvent(e -> inventory.moveToMenu(player, new ShopMainMenu(player, shop, plugin).getInventory()));
    }

}
