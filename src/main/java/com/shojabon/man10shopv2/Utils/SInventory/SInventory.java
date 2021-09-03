package com.shojabon.man10shopv2.Utils.SInventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public abstract class SInventory implements Listener {


    HashMap<String, Consumer<Object>> events = new HashMap<>();
    HashMap<Integer, SInventoryItem> items = new HashMap<>();

    ArrayList<Consumer<InventoryCloseEvent>> onCloseEvents = new ArrayList<>();
    ArrayList<Consumer<InventoryCloseEvent>> onForcedCloseEvents = new ArrayList<>();
    ArrayList<Consumer<InventoryClickEvent>> clickEvents = new ArrayList<>();

    ArrayList<Consumer<InventoryCloseEvent>> asyncOnCloseEvents = new ArrayList<>();
    ArrayList<Consumer<InventoryCloseEvent>> asyncOnForcedCloseEvents = new ArrayList<>();
    ArrayList<Consumer<InventoryClickEvent>> asyncClickEvents = new ArrayList<>();

    ArrayList<Consumer<Player>> asyncAfterInventoryOpenEvents = new ArrayList<>();
    ArrayList<Consumer<Player>> afterInventoryOpenEvents = new ArrayList<>();

    public static ArrayList<UUID> playersInInventoryGlobal = new ArrayList<>();
    public static Executor threadPool = Executors.newCachedThreadPool();

    public static HashMap<UUID, UUID> inventoryGroup = new HashMap<>();


    public Inventory activeInventory = null;

    //inventory status
    int rows;
    JavaPlugin plugin;
    String title;

    ArrayList<UUID> playerInMenu = new ArrayList<>();
    public static ArrayList<UUID> movingPlayer = new ArrayList<>();

    public SInventory(String title, int inventoryRows, JavaPlugin plugin){
        this.title = title;
        this.rows = inventoryRows;
        this.plugin = plugin;
    }

    //set items

    public SInventory setItem(int slot, SInventoryItem data){
        items.put(slot, data);
        return this;
    }

    public SInventory setItem(int[] slots, SInventoryItem data){
        for(int slot: slots){
            items.put(slot, data);
        }
        return this;
    }

    public SInventory setItem(int slot, ItemStack data){
        items.put(slot, new SInventoryItem(data));
        return this;
    }

    public SInventory setItem(int[] slots, ItemStack data){
        for(int slot: slots){
            items.put(slot, new SInventoryItem(data));
        }
        return this;
    }

    public SInventory removeItem(int[] slots){
        for(int slot: slots){
            items.remove(slot);
        }
        return this;
    }

    public SInventory removeItem(int slot){
        removeItem(new int[]{slot});
        return this;
    }

    public SInventoryItem getItem(int slot){
        return items.get(slot);
    }

    public SInventory fillItem(SInventoryItem data){
        for(int i = 0; i < rows*9; i++){
            items.put(i, data);
        }
        return this;
    }

    public SInventory fillItem(ItemStack data){
        for(int i = 0; i < rows*9; i++){
            items.put(i, new SInventoryItem(data));
        }
        return this;
    }

    public SInventory clear(){
        for(int i = 0; i < rows*9; i++){
            items.remove(i);
            if(activeInventory != null) activeInventory.setItem(i, null);
        }
        return this;
    }

    public void renderInventory(){
        if(activeInventory == null){
            activeInventory = plugin.getServer().createInventory(null, this.rows*9, title);
        }
        for(int key: items.keySet()){
            activeInventory.setItem(key, items.get(key).getItemStack());
        }
    }

    //======================

    public SInventory setTitle(String title){
        this.title = title;
        return this;
    }

    public void open(Player p){
        registerEvents();
        renderMenu();
        afterRenderMenu();
        renderInventory();
        playersInInventoryGlobal.add(p.getUniqueId());
        p.openInventory(activeInventory);

        for(Consumer<Player> event: afterInventoryOpenEvents){
            event.accept(p);
        }
        for(Consumer<Player> event: asyncAfterInventoryOpenEvents){
            threadPool.execute(() -> event.accept(p));
        }
        if(plugin != null){
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            playerInMenu.add(p.getUniqueId());
        }
    }

    public void close(Player p){
        if(!playerInMenu.contains(p.getUniqueId())){
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, ()->{
            movingPlayer.add(p.getUniqueId());
            p.closeInventory();
            movingPlayer.remove(p.getUniqueId());
        });
    }

    public void moveToMenu(Player p, SInventory inv){
        plugin.getServer().getScheduler().runTask(plugin, ()->{
            movingPlayer.add(p.getUniqueId());
            inv.open(p);
            movingPlayer.remove(p.getUniqueId());
        });
    }

    //abstract ish functions

    public void renderMenu(){

    }

    public void afterRenderMenu(){

    }

    public void registerEvents(){

    }


    //======== custom event =====

    public void setEvent(String eventName, Consumer<Object> event){
        events.put(eventName, event);
    }

    public void activateEvent(String eventName, Object data){
        if(!events.containsKey(eventName)){
            return;
        }
        events.get(eventName).accept(data);

    }

    //======== synced events ====
    public void setOnCloseEvent(Consumer<InventoryCloseEvent> event){
        this.onCloseEvents.add(event);
    }

    public void setOnClickEvent(Consumer<InventoryClickEvent> event){
        clickEvents.add(event);
    }

    public void setOnForcedCloseEvent(Consumer<InventoryCloseEvent> event){
        this.onForcedCloseEvents.add(event);
    }

    public void setAfterInventoryOpenEvents(Consumer<Player> event){
        this.afterInventoryOpenEvents.add(event);
    }

    //======== async events ====

    public void setAsyncOnCloseEvent(Consumer<InventoryCloseEvent> event){
        this.asyncOnCloseEvents.add(event);
    }

    public void setAsyncOnClickEvent(Consumer<InventoryClickEvent> event){
        asyncClickEvents.add(event);
    }

    public void setAsyncOnForcedCloseEvent(Consumer<InventoryCloseEvent> event){
        this.asyncOnForcedCloseEvents.add(event);
    }

    public void setAsyncAfterInventoryOpenEvents(Consumer<Player> event){
        this.asyncAfterInventoryOpenEvents.add(event);
    }
    //========= inventory group =====

    public static void closeInventoryGroup(UUID group,JavaPlugin plugin){
        for(UUID user: inventoryGroup.keySet()){
            if(inventoryGroup.get(user).equals(group)){
                Player p = Bukkit.getServer().getPlayer(user);
                if(p == null) continue;
                Bukkit.getServer().getScheduler().runTask(plugin, (@NotNull Runnable) p::closeInventory);
                inventoryGroup.remove(user);
            }
        }
    }

    public static void setInventoryGroup(UUID user, UUID group){
        inventoryGroup.put(user, group);
    }

    public static ArrayList<UUID> getPlayersInInventoryGlobal(){
        return new ArrayList<>(playersInInventoryGlobal);
    }

    public static void closeAllSInventories(){
        for(UUID uuid: SInventory.getPlayersInInventoryGlobal()){
            Player p = Bukkit.getServer().getPlayer(uuid);
            if(p == null) continue;
            movingPlayer.add(p.getUniqueId());
            p.closeInventory();
            movingPlayer.remove(p.getUniqueId());
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(!playerInMenu.contains(e.getWhoClicked().getUniqueId())) return;
        for(Consumer<InventoryClickEvent> clickEvent: clickEvents){
            clickEvent.accept(e);
        }

        for(Consumer<InventoryClickEvent> clickEvent: asyncClickEvents){
            threadPool.execute(() -> clickEvent.accept(e));
        }

        if(!items.containsKey(e.getRawSlot())){
            return;
        }
        SInventoryItem item = items.get(e.getRawSlot());
        item.activateClick(e);

    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        if(!playerInMenu.contains(e.getPlayer().getUniqueId())) return;
        playerInMenu.remove(e.getPlayer().getUniqueId());
        HandlerList.unregisterAll(this);
        playersInInventoryGlobal.remove(e.getPlayer().getUniqueId());

        inventoryGroup.remove(e.getPlayer().getUniqueId());
        //execute events
        if(!movingPlayer.contains(e.getPlayer().getUniqueId())){
            for(Consumer<InventoryCloseEvent> event: onCloseEvents){
                event.accept(e);
            }
            for(Consumer<InventoryCloseEvent> event: asyncOnCloseEvents){
                threadPool.execute(() -> event.accept(e));
            }
        }else{
            movingPlayer.remove(e.getPlayer().getUniqueId());
        }
        for(Consumer<InventoryCloseEvent> event: onForcedCloseEvents){
            event.accept(e);
        }
        for(Consumer<InventoryCloseEvent> event: asyncOnForcedCloseEvents){
            threadPool.execute(() -> event.accept(e));
        }
    }

}
