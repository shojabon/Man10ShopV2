package com.shojabon.man10shopv2.Menus.action;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.Man10ShopOrder;
import com.shojabon.man10shopv2.DataClass.lootBox.LootBox;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

public class LootBoxPlayMenu extends SInventory{

    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;
    ItemStack[] items = new ItemStack[9];
    int[] slots = new int[]{9,10, 11,12,13,14,15,16,17};
    LootBox box;

    public static ArrayList<UUID> playerInGame = new ArrayList<>();


    public LootBoxPlayMenu(Player p, Man10Shop shop, Man10ShopV2 plugin){
        super(shop.name.getName() + " 残り " + shop.getPlayerAvailableTransactionCount(p) + "回",3, plugin);
        this.shop = shop;
        this.player = p;
        this.plugin = plugin;
        this.box = shop.lootBoxFunction.getLootBox();

        SStringBuilder builder = new SStringBuilder().darkGray().text(new SItemStack(shop.setBarter.getResultItems()[0]).getDisplayName());


        setTitle(builder.build());

        setOnClickEvent(e -> {
            if(e.getClickedInventory() == null) return;
            if(e.getClickedInventory().getType() == InventoryType.PLAYER)e.setCancelled(true);
        });

    }

    public void renderMenu(){
        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        fillItem(background);

        startSpinning();

        renderInventory();
    }

    public void startSpinning(){
        playerInGame.add(player.getUniqueId());
        setItem(slots, new ItemStack(Material.AIR));

        SInventoryItem pointer = new SInventoryItem(new SItemStack(Material.RED_STAINED_GLASS_PANE).setDisplayName(" ").build());
        pointer.clickable(false);
        setItem(new int[]{4, 22}, pointer);
        Runnable r = () -> {
            long speed =  5;
            int stage = 0;

            for(int i = 0; i < 10000; i++){
                Bukkit.getScheduler().runTask(plugin, this::spin);
                try {
                    Thread.sleep(speed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(i >= 150){
                    speed += 1;
                }
                if(speed >= 75){
                    speed += 10;
                }
                if(speed >= 200){
                    speed += 150;
                }
                if(speed >= 900){
                    speed = 900;
                    if(stage == 3){
                        speed = 1000;
                    }
                    stage ++;
                }

                if(speed >= 1000){
                    break;
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            player.getInventory().addItem(items[4]);
            player.sendMessage(Man10ShopV2.prefix + "§a§lおめでとうございます『" + new SItemStack(items[4]).getDisplayName() + "』§a§lが当たりました!");
            playerInGame.remove(player.getUniqueId());
        };
        SInventory.threadPool.execute(r);
    }

    public void spin(){
        ItemStack newItem = box.pickRandomItem().clone();
        SInventoryItem newSItem = new SInventoryItem(newItem.clone());
        newSItem.clickable(false);

        for(int i = 0; i < slots.length-1; i ++){
            setItem(slots[i], getItem(slots[i+1]));
            items[i] = items[i+1];
        }
        setItem(slots[slots.length-1], newSItem);
        items[items.length-1] = newItem.clone();
        renderInventory();
        player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1, 1);
    }


}
