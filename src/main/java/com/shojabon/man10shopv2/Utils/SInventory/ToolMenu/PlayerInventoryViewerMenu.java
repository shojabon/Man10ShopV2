package com.shojabon.man10shopv2.Utils.SInventory.ToolMenu;

import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class PlayerInventoryViewerMenu extends SInventory{


    HashMap<Integer, ItemStack> inventoryContents = new HashMap<>();
    HashMap<Integer, ItemStack> armorContents = new HashMap<>();

    public PlayerInventoryViewerMenu(String title, JavaPlugin plugin) {
        super(title, 6, plugin);
    }

    public void setInventoryContents(HashMap<Integer, ItemStack> contents){
        this.inventoryContents = contents;
    }

    public void setArmorContents(HashMap<Integer, ItemStack> contents){
        this.armorContents = contents;
    }

    public void renderMenu(){
        for(int i = 0; i < 9; i++){
            if(!inventoryContents.containsKey(i)) continue;
            setItem(i+45, new SInventoryItem(inventoryContents.get(i).clone()).clickable(false));
        }

        for(int i = 9; i < 36; i++){
            if(!inventoryContents.containsKey(i)) continue;
            setItem(i+9, new SInventoryItem(inventoryContents.get(i).clone()).clickable(false));
        }
        setItem(new int[]{9,10,11,12,13,14,15,16,17}, new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build()).clickable(false));
        for(int i = 0; i < 5; i++){
            if(!armorContents.containsKey(i)) continue;
            setItem(i+2, new SInventoryItem(armorContents.get(i).clone()).clickable(false));
        }

        setOnClickEvent(e -> e.setCancelled(true));
    }
}
