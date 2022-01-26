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

public class AIActionMenu extends SInventory {
    Man10Shop shop;
    JavaPlugin plugin;
    Player player;
    boolean orderRequested = false;
    BannerDictionary dictionary = new BannerDictionary();

    public AIActionMenu(Player p, Man10Shop shop, JavaPlugin plugin){
        super(shop.name.getName(), 6, plugin);
        this.player = p;
        this.shop = shop;
        this.plugin = plugin;
        SStringBuilder builder = new SStringBuilder().darkGray().text(shop.targetItem.getTargetItem().getDisplayName() + "を買う");
    }
    public void renderMenu(){
        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        fillItem(background);

        SInventoryItem targetItem = new SInventoryItem(shop.targetItem.getTargetItem().build()).clickable(false);
        setItem(13, targetItem);

        renderConfirmButton();
        renderRequiredItem();
        renderPriceUnit();

        renderInventory();

        setOnClickEvent(e -> {
            if(e.getClickedInventory() == null) return;
            if(e.getClickedInventory().getType() == InventoryType.PLAYER)e.setCancelled(true);
        });

        setAfterInventoryOpenEvents(e -> inventoryGroup.put(player.getUniqueId(), shop.shopId));
    }

    public void renderRequiredItem(){
        if(shop.aiTargetItemFunction.item.get() == null) return;
        SInventoryItem targetItem = new SInventoryItem(shop.aiTargetItemFunction.item.get()).clickable(false);
        setItem(31, targetItem);
    }

    public void renderPriceUnit(){
        int[] startingIndex = {40, 40, 39, 39, 38, 38, 37, 37, 36};
        String priceString = shop.aiPriceUnitFunction.price.get().toString();
        int starting = startingIndex[priceString.length()-1];
        for(int i = starting; i < starting + priceString.length(); i++){
            int charInt = priceString.charAt(i - starting);
            setItem(i, dictionary.getItem(Character.getNumericValue(charInt)));
        }
    }

    public void renderConfirmButton(){
        Material buttonMaterial = Material.LIME_STAINED_GLASS_PANE;
        if(shop.shopType.getShopType() == Man10ShopType.SELL){
            buttonMaterial = Material.RED_STAINED_GLASS_PANE;
        }
        SItemStack item = new SItemStack(buttonMaterial).setDisplayName("§a§l確認");
        SStringBuilder lore = new SStringBuilder().yellow().text(1).text("個を");
        if(shop.aiTargetItemFunction.item.get() == null){
            lore.text(shop.aiPriceUnitFunction.price.get()).text("円で").text("買う");
        }else{
            lore.text(new SItemStack(shop.aiTargetItemFunction.item.get()).getDisplayName()).text("を").text(shop.aiPriceUnitFunction.price.get()).text("個で").text("買う");
        }
        item.addLore(lore.build());
        SInventoryItem confirm = new SInventoryItem(item.build());
        confirm.clickable(false);

        confirm.setAsyncEvent(e -> {
            if(orderRequested) return;
            Man10ShopV2.api.addTransaction(new Man10ShopOrder(player, shop.getShopId(), 1));
            setTitle("test");
            //orderRequested = true;
            //close(player);
        });

        setItem(new int[]{48,49,50}, confirm);
    }
}
