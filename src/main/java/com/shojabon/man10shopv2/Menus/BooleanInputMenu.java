package com.shojabon.man10shopv2.Menus;

import com.shojabon.man10shopv2.Utils.BannerDictionary;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public class BooleanInputMenu {

    String title;
    public SInventory inventory;
    JavaPlugin plugin;

    Consumer<Boolean> onConfirm;
    Consumer<InventoryClickEvent> onCancel;
    Consumer<InventoryCloseEvent> onClose;

    ItemStack information;

    boolean current;



    public BooleanInputMenu(boolean current, String title, JavaPlugin plugin){
        this.plugin = plugin;
        this.title = title;
        this.current = current;

        inventory = new SInventory(title, 6, plugin);
    }

    public void setInformation(ItemStack item){
        information = item;
    }

    public SInventory getInventory() {
        renderMenu();
        return inventory;
    }

    public void setOnConfirm(Consumer<Boolean> event){
        this.onConfirm = event;
    }

    public void setOnCancel(Consumer<InventoryClickEvent> event){
        this.onCancel = event;
    }

    public void setOnClose(Consumer<InventoryCloseEvent> event){
        this.onClose = event;
    }

    public void renderButtons(){

        SInventoryItem t = new SInventoryItem(new SItemStack(Material.LIME_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().green().bold().text("ture").build()).setGlowingEffect(current).build());
        t.clickable(false);
        t.setEvent(e -> {
            current = true;
            renderButtons();
        });

        SInventoryItem f = new SInventoryItem(new SItemStack(Material.RED_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().darkRed().bold().text("false").build()).setGlowingEffect(!current).build());
        f.clickable(false);
        f.setEvent(e -> {
            current = false;
            renderButtons();
        });

    }

    public void renderMenu(){
        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        inventory.fillItem(background);

        renderButtons();

        if(information != null){
            SInventoryItem invItem = new SInventoryItem(information);
            invItem.clickable(false);
            inventory.setItem(13, invItem);
        }

        SInventoryItem f = new SInventoryItem(new SItemStack(Material.LIME_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().green().bold().text("確認").build()).build());
        f.clickable(false);
        f.setEvent(e-> onConfirm.accept(current));


    }
}
