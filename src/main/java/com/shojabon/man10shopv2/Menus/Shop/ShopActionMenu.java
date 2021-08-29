package com.shojabon.man10shopv2.Menus.Shop;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Utils.BannerDictionary;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

public class ShopActionMenu extends SInventory{
    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;
    int itemCount = 1;
    BannerDictionary dictionary = new BannerDictionary();

    public ShopActionMenu(Player p, Man10Shop shop, Man10ShopV2 plugin){
        super("", 6, plugin);
        this.player = p;
        this.shop = shop;
        this.plugin = plugin;
        SStringBuilder builder = new SStringBuilder().darkGray().text(shop.targetItem.getDisplayName());
        if(shop.shopType == Man10ShopType.BUY){
            builder.text("§a§lを買う");
        }else{
            builder.text("§c§lを売る");
        }
        setTitle(builder.toString());

    }


    public void renderMenu(){
        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        fillItem(background);

        renderConfirmButton();
        renderDisplay();
        renderButtons();

    }

    public void renderConfirmButton(){
        SItemStack item = new SItemStack(Material.LIME_STAINED_GLASS_PANE).setDisplayName("§a§l確認");
        SStringBuilder lore = new SStringBuilder().yellow().text(itemCount).text("個を").text(itemCount*shop.price).text("円で");
        if(shop.shopType == Man10ShopType.BUY){
            lore.text("買う");
        }else{
            lore.text("売る");
        }
        item.addLore(lore.build());
        SInventoryItem confirm = new SInventoryItem(item.build());
        confirm.clickable(false);

        confirm.setEvent(e -> {
            shop.performAction(player, itemCount);
            player.closeInventory();
        });

        setItem(new int[]{30,31,32,39,40,41,48,49,50}, confirm);
    }

    public Consumer<InventoryClickEvent> createEvent(boolean add){
        return e -> {
            if(add){
                if(e.getClick() == ClickType.LEFT){
                    if(itemCount+1 > shop.targetItem.getMaxStackSize()){
                        return;
                    }
                    itemCount++;
                }else if (e.getClick() == ClickType.SHIFT_LEFT){
                    itemCount = shop.targetItem.getMaxStackSize();
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
            renderInventory();
        };
    }

    public void renderButtons(){
        SInventoryItem increase = new SInventoryItem(new SItemStack(dictionary.getSymbol("plus").clone()).setDisplayName("§a§l購入数を増やす").build());
        increase.clickable(false);
        increase.setEvent(createEvent(true));
        setItem(43, increase);

        SInventoryItem decrease = new SInventoryItem(new SItemStack(dictionary.getSymbol("minus").clone()).setDisplayName("§a§l購入数を減らす").build());
        decrease.clickable(false);
        decrease.setEvent(createEvent(false));
        setItem(37, decrease);
    }

    public void renderDisplay(){
        SInventoryItem item = new SInventoryItem(new SItemStack(shop.targetItem.build().clone()).setAmount(itemCount).build());
        item.clickable(false);
        setItem(13, item);
    }
}
