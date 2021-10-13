package com.shojabon.man10shopv2.Menus.action;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.Man10ShopOrder;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.mcutils.Utils.BannerDictionary;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class BarterActionMenu extends SInventory{

    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;
    BannerDictionary banner = new BannerDictionary();
    int[] slots = new int[]{10, 11, 12, 19, 20 ,21 ,28, 29, 30, 37, 38, 39, 34};
    ItemStack[] current;
    boolean orderRequested = false;


    public BarterActionMenu(Player p, Man10Shop shop, Man10ShopV2 plugin){
        super("トレード設定",6, plugin);
        this.shop = shop;
        this.player = p;
        this.plugin = plugin;
        this.current = (ItemStack[]) ArrayUtils.addAll(shop.setBarter.getRequiredItems(), shop.setBarter.getResultItems());

        SStringBuilder builder = new SStringBuilder().darkGray().text(new SItemStack(shop.setBarter.getResultItems()[0]).getDisplayName());

        int maxTradeItemCount = shop.getPlayerAvailableTransactionCount(p);
        builder.text("§b§lにトレードする");
        if(maxTradeItemCount != 0){
            builder.text(" 残り " + maxTradeItemCount + "個");
        }
        setTitle(builder.build());
    }

    public void renderMenu(){
        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        fillItem(background);

        SInventoryItem arrow = new SInventoryItem(new SItemStack(banner.getSymbol("right")).setDisplayName(" ").build());
        arrow.clickable(false);
        setItem(32, arrow);

        SInventoryItem noItem = new SInventoryItem(new SItemStack(Material.GRAY_STAINED_GLASS_PANE).setDisplayName(" ").build());
        noItem.clickable(false);
        setItem(slots, noItem);

        for(int i = 0; i < current.length; i++){
            if(current[i] == null) continue;
            SInventoryItem item = new SInventoryItem(current[i]);
            item.clickable(false);
            setItem(slots[i], item);
        }

        SInventoryItem confirm = new SInventoryItem(new SItemStack(Material.LIME_STAINED_GLASS_PANE).setDisplayName("§a§l決定").build());
        confirm.clickable(false);
        confirm.setAsyncEvent(e -> {
            if(orderRequested) return;
            Man10ShopV2.api.addTransaction(new Man10ShopOrder(player, shop.getShopId(), 1));
            orderRequested = true;
            close(player);
        });
        setItem(new int[]{48, 49, 50}, confirm);

        renderInventory();
    }


}
