package com.shojabon.man10shopv2.menus.action;

import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopOrder;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.mcutils.Utils.BannerDictionary;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public class BuySellActionMenu extends SInventory {
    Man10Shop shop;
    JavaPlugin plugin;
    Player player;
    int itemCount = 1;
    boolean orderRequested = false;
    BannerDictionary dictionary = new BannerDictionary();

    public BuySellActionMenu(Player p, Man10Shop shop, JavaPlugin plugin){
        super(shop.name.getName(), 6, plugin);
        this.player = p;
        this.shop = shop;
        this.plugin = plugin;
        SStringBuilder builder = new SStringBuilder().darkGray().text(shop.targetItem.getTargetItem().getDisplayName());

        int maxTradeItemCount = shop.getPlayerAvailableTransactionCount(p);

        if(shop.shopType.getShopType() == Man10ShopType.BUY){
            builder.text("§a§lを買う");
        }else{
            builder.text("§c§lを売る");

        }
        if(maxTradeItemCount != 0){
            builder.text(" 残り " + maxTradeItemCount + "個");
        }
        setTitle(builder.build());

    }

    public void renderMenu(){
        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        fillItem(background);

        renderConfirmButton();
        renderDisplay();
        renderButtons();

        renderInventory();

        setOnClickEvent(e -> {
            if(e.getClickedInventory() == null) return;
            if(e.getClickedInventory().getType() == InventoryType.PLAYER)e.setCancelled(true);
        });

        setAfterInventoryOpenEvents(e -> inventoryGroup.put(player.getUniqueId(), shop.shopId));
    }

    public void renderConfirmButton(){
        Material buttonMaterial = Material.LIME_STAINED_GLASS_PANE;
        if(shop.shopType.getShopType() == Man10ShopType.SELL){
            buttonMaterial = Material.RED_STAINED_GLASS_PANE;
        }
        SItemStack item = new SItemStack(buttonMaterial).setDisplayName("§a§l確認");
        SStringBuilder lore = new SStringBuilder().yellow().text(itemCount).text("個を");
        if(!shop.secretPrice.isFunctionEnabled()){
            lore.text(itemCount*shop.price.getPrice()).text("円で");
        }
        if(shop.shopType.getShopType() == Man10ShopType.BUY){
            lore.text("買う");
        }else{
            lore.text("売る");
        }
        item.addLore(lore.build());
        SInventoryItem confirm = new SInventoryItem(item.build());
        confirm.clickable(false);

        confirm.setAsyncEvent(e -> {
            if(orderRequested) return;
            Man10ShopV2.api.addTransaction(new Man10ShopOrder(player, shop.getShopId(), itemCount));
            setTitle("test");
            //orderRequested = true;
            //close(player);
        });

        setItem(new int[]{30,31,32,39,40,41,48,49,50}, confirm);
    }

    public Consumer<InventoryClickEvent> createEvent(boolean add){
        return e -> {
            if(add){
                if(e.getClick() == ClickType.LEFT){
                    if(itemCount+1 > shop.targetItem.getTargetItem().getMaxStackSize()){
                        return;
                    }
                    itemCount++;
                }else if (e.getClick() == ClickType.SHIFT_LEFT){
                    itemCount = shop.targetItem.getTargetItem().getMaxStackSize();
                }
            }else{
                if(e.getClick() == ClickType.LEFT){
                    if(itemCount-1 <= 0){
                        return;
                    }
                    itemCount--;
                }else if (e.getClick() == ClickType.SHIFT_LEFT){
                    itemCount = 1;
                }
            }
            renderMenu();
        };
    }

    public void renderButtons(){
        if(shop.singleTransactionMode.enabled.get()) return;
        SInventoryItem increase = new SInventoryItem(new SItemStack(dictionary.getSymbol("plus").clone())
                .addLore("§f左クリックで取引数1増加")
                .addLore("§fシフト+左クリックで取引数を最大まで増加")
                .setDisplayName("§a§l取引数を増やす").build());
        increase.clickable(false);
        increase.setEvent(createEvent(true));
        setItem(43, increase);

        SInventoryItem decrease = new SInventoryItem(new SItemStack(dictionary.getSymbol("minus").clone())
                .addLore("§f左クリックで取引数1減らす")
                .addLore("§fシフト+左クリックで取引数を最小まで減らす")
                .setDisplayName("§a§l取引数を減らす").build());
        decrease.clickable(false);
        decrease.setEvent(createEvent(false));
        setItem(37, decrease);
    }

    public void renderDisplay(){
        SInventoryItem item = new SInventoryItem(new SItemStack(shop.targetItem.getTargetItem().build().clone()).setAmount(itemCount).build());
        item.clickable(false);
        setItem(13, item);
    }
}
