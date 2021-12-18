package com.shojabon.man10shopv2.menus.action;

import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopOrder;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.mcutils.Utils.BannerDictionary;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public class CommandActionMenu extends SInventory {
    Man10Shop shop;
    JavaPlugin plugin;
    Player player;
    BannerDictionary dictionary = new BannerDictionary();

    public CommandActionMenu(Player p, Man10Shop shop, JavaPlugin plugin){
        super(shop.name.getName(), 5, plugin);
        this.player = p;
        this.shop = shop;
        this.plugin = plugin;
        SStringBuilder builder = new SStringBuilder().darkGray().text(shop.name.getName());

        int maxTradeItemCount = shop.getPlayerAvailableTransactionCount(p);
        if(maxTradeItemCount != 0){
            builder.text(" 残り " + maxTradeItemCount + "回");
        }
        setTitle(builder.build());

    }

    public void renderMenu(){
        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        fillItem(background);

        renderConfirmButton();

        renderInventory();

        setOnClickEvent(e -> {
            if(e.getClickedInventory() == null) return;
            if(e.getClickedInventory().getType() == InventoryType.PLAYER)e.setCancelled(true);
        });

        setAfterInventoryOpenEvents(e -> inventoryGroup.put(player.getUniqueId(), shop.shopId));
    }

    public void renderConfirmButton(){
        Material buttonMaterial = Material.LIME_STAINED_GLASS_PANE;
        SItemStack item = new SItemStack(buttonMaterial).setDisplayName("§a§l確認");

        for(String explanation: shop.commandShopExplanationFunction.explanation.get().split("\\|")){
            item.addLore(explanation);
        }

        item.addLore("");
        if(!shop.secretPrice.isFunctionEnabled()){
            item.addLore(shop.price.getPrice() + "円で買う");
        }

        SInventoryItem confirm = new SInventoryItem(item.build());
        confirm.clickable(false);

        confirm.setAsyncEvent(e -> {
            Man10ShopV2.api.addTransaction(new Man10ShopOrder(player, shop.getShopId(), 1));
            //orderRequested = true;
            //close(player);
        });

        setItem(new int[]{12,13,14,21,22,23,30,31,32}, confirm);
    }

}
