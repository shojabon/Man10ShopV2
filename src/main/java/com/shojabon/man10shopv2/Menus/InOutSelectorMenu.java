package com.shojabon.man10shopv2.Menus;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.function.Consumer;

public class InOutSelectorMenu extends SInventory{
    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;

    Consumer<InventoryClickEvent> onInClicked;
    Consumer<InventoryClickEvent> onOutClicked;
    Consumer<InventoryCloseEvent> onClose;

    String inText = "";
    String outText = "";

    public void setInText(String text){
        this.inText = text;
    }

    public void setOutText(String text){
        this.outText = text;
    }


    public void setOnInClicked(Consumer<InventoryClickEvent> event){
        this.onInClicked = event;
    }

    public void setOnOutClicked(Consumer<InventoryClickEvent> event){
        this.onOutClicked = event;
    }

    public void setOnClose(Consumer<InventoryCloseEvent> event){
        this.onClose = event;
    }

    public InOutSelectorMenu(Player p, Man10Shop shop, Man10ShopV2 plugin){
        super(new SStringBuilder().darkGray().text("操作の種類を選択してください").build(), 3, plugin);
        this.player = p;
        this.shop = shop;
        this.plugin = plugin;


    }

    public void renderMenu(){
        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        fillItem(background);

        SInventoryItem in = new SInventoryItem(new SItemStack(Material.HOPPER).setDisplayName(new SStringBuilder().gray().bold().text(inText).build()).build());
        in.clickable(false);
        in.setEvent(e -> {
            if(onInClicked != null) onInClicked.accept(e);
        });
        setItem(11, in);

        SInventoryItem out = new SInventoryItem(new SItemStack(Material.DISPENSER).setDisplayName(new SStringBuilder().gray().bold().text(outText).build()).build());
        out.clickable(false);
        out.setEvent(e -> {
            if(onOutClicked != null) onOutClicked.accept(e);
        });
        setItem(15, out);

    }

    public void registerEvents(){
        setOnCloseEvent(e -> {
            if(onClose != null) onClose.accept(e);
        });
    }
}
