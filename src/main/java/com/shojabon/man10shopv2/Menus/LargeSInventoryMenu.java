package com.shojabon.man10shopv2.Menus;

import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class LargeSInventoryMenu {

    ArrayList<SInventoryItem> items = new ArrayList<>();
    SInventory inventory;
    int rows = 1;
    int currentPage = 0;


    public LargeSInventoryMenu(String title, int rows, JavaPlugin plugin){
        this.rows = rows;
        if(rows < 1 ) rows = 1;
        if(rows > 5) rows = 5;
        inventory = new SInventory(title, rows+1, plugin);
    }

    public SInventory getInventory(){
        return inventory;
    }

    public void open(Player p, ArrayList<SInventoryItem> items){
        this.items = items;
        renderInventory(currentPage);
        getInventory().open(p);
    }

    public void renderControlBar(){
        int[] slots = new int[9];
        for(int i = 0; i < 9; i++){
            slots[i] = rows*9+i;
        }

        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        inventory.setItem(slots, background);

        //buttons

        SInventoryItem left = new SInventoryItem(new SItemStack(Material.RED_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().green().bold().text("前へ").build()).build());
        SInventoryItem right = new SInventoryItem(new SItemStack(Material.RED_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().red().bold().text("次へ").build()).build());

        left.clickable(false);
        right.clickable(false);

        left.setEvent(e -> {
            currentPage--;
            renderInventory(currentPage);
        });

        right.setEvent(e -> {
            currentPage++;
            renderInventory(currentPage);
        });

        if(currentPage != 0) inventory.setItem(slots[0], left);        //has left
        if((currentPage+1)*rows*9 <= items.size()-1) inventory.setItem(slots[8], right);    //has right
    }

    public void renderInventory(int page){
        inventory.clear();
        renderControlBar();
        int startingIndex = page*rows*9;
        int ending = items.size() - startingIndex;
        if(ending> rows*9) ending = rows*9;
        for(int i = 0; i < ending; i++){
            inventory.setItem(i, items.get(startingIndex+i));
        }
        inventory.renderInventory();
    }


}
