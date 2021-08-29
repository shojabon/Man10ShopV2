package com.shojabon.man10shopv2.Menus;

import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public class ConfirmationMenu extends SInventory{

    Consumer<InventoryClickEvent> onConfirm;
    Consumer<InventoryClickEvent> onCancel;
    Consumer<InventoryCloseEvent> onClose;

    public ConfirmationMenu(String title, JavaPlugin plugin) {
        super(title, 4, plugin);
    }

    public void setOnConfirm(Consumer<InventoryClickEvent> event){
        this.onConfirm = event;
    }

    public void setOnCancel(Consumer<InventoryClickEvent> event){
        this.onCancel = event;
    }

    public void setOnClose(Consumer<InventoryCloseEvent> event){
        this.onClose = event;
    }

    public void renderMenu(){
        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        fillItem(background);

        SInventoryItem no = new SInventoryItem(new SItemStack(Material.RED_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().darkRed().bold().text("キャンセル").build()).build());
        no.clickable(false);
        no.setAsyncEvent(e -> {
            if(onCancel != null) onCancel.accept(e);
        });
        setItem(new int[]{10, 11, 19, 20}, no);

        SInventoryItem yes = new SInventoryItem(new SItemStack(Material.LIME_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().green().bold().text("確認").build()).build());
        yes.clickable(false);
        yes.setAsyncEvent(e -> {
            if(onCancel != null) onConfirm.accept(e);
        });
        setItem(new int[]{15, 16, 24, 25}, yes);

        setAsyncOnCloseEvent(e -> {
            if(onClose != null) onClose.accept(e);
        });
    }
}
