package com.shojabon.man10shopv2.Utils.SInventory.ToolMenu;

import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class LargeSInventoryMenu extends SInventory{

    ArrayList<SInventoryItem> items = new ArrayList<>();
    int rows = 5;
    int currentPage = 0;

    public LargeSInventoryMenu(String title, JavaPlugin plugin) {
        super(title, 6, plugin);
    }

    public void setItems(ArrayList<SInventoryItem> items){
        this.items = items;
    }

    public void renderControlBar(){
        int[] slots = new int[9];
        for(int i = 0; i < 9; i++){
            slots[i] = rows*9+i;
        }

        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        setItem(slots, background);

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

        if(currentPage != 0) setItem(slots[0], left);        //has left
        if((currentPage+1)*rows*9 <= items.size()-1) setItem(slots[8], right);    //has right
    }

    public void renderInventory(int page){
        clear();
        renderControlBar();
        int startingIndex = page*rows*9;
        int ending = items.size() - startingIndex;
        if(ending> rows*9) ending = rows*9;
        for(int i = 0; i < ending; i++){
            setItem(i, items.get(startingIndex+i));
        }
        renderInventory();
    }

    public void afterRenderMenu() {
        renderInventory(0);
    }
}
