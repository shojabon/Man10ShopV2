package com.shojabon.man10shopv2.Utils.SInventory;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SInventoryItem {

    private ItemStack item;
    ArrayList<Consumer<InventoryClickEvent>> events = new ArrayList<>();
    ArrayList<Consumer<InventoryClickEvent>> asyncEvents = new ArrayList<>();
    public static Executor threadPool = Executors.newCachedThreadPool();

    boolean clickable = true;

    public SInventoryItem(ItemStack item){
        this.item = item;
        this.setEvent(event -> {if(!clickable) event.setCancelled(true);});
    }

    public SInventoryItem setEvent(Consumer<InventoryClickEvent> consumer){
        events.add(consumer);
        return this;
    }

    public SInventoryItem setAsyncEvent(Consumer<InventoryClickEvent> consumer){
        asyncEvents.add(consumer);
        return this;
    }

    public SInventoryItem clickable(boolean clickable){
        this.clickable = clickable;
        return this;
    }

    public void activateClick(InventoryClickEvent e){
        for(Consumer<InventoryClickEvent> event: asyncEvents){
            threadPool.execute(() -> event.accept(e));
        }
        for(Consumer<InventoryClickEvent> event: events){
            event.accept(e);
        }
    }

    public ItemStack getItemStack(){
        return item;
    }


}
