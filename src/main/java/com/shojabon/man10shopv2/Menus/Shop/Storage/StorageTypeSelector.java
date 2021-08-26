package com.shojabon.man10shopv2.Menus.Shop.Storage;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.Shop.ShopMainMenu;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class StorageTypeSelector {

    SInventory inventory;
    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;

    public StorageTypeSelector(Player p, Man10Shop shop, Man10ShopV2 plugin){
        this.player = p;
        this.shop = shop;
        this.plugin = plugin;
        inventory = new SInventory(new SStringBuilder().darkGray().text("操作の種類を選択してください").build(), 3, plugin);
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

        SInventoryItem in = new SInventoryItem(new SItemStack(Material.HOPPER).setDisplayName(new SStringBuilder().gray().bold().text("倉庫にアイテムを入れる").build()).build());
        in.clickable(false);
        in.setEvent(e -> inventory.moveToMenu(player, new ItemStorageMenu(player, shop, plugin, false).getInventory()));
        inventory.setItem(11, in);

        SInventoryItem out = new SInventoryItem(new SItemStack(Material.DISPENSER).setDisplayName(new SStringBuilder().gray().bold().text("倉庫からアイテムを出す").build()).build());
        out.clickable(false);
        out.setEvent(e -> inventory.moveToMenu(player, new ItemStorageMenu(player, shop, plugin, true).getInventory()));
        inventory.setItem(15, out);

    }

    public void registerEvents(){
        inventory.setOnCloseEvent(e -> inventory.moveToMenu(player, new ShopMainMenu(player, shop, plugin).getInventory()));
    }

}
