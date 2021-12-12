package com.shojabon.man10shopv2.menus.settings.innerSettings;

import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WeekdayShopToggleMenu extends SInventory{

    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;
    public List<Boolean> states = new ArrayList<>();

    public WeekdayShopToggleMenu(Player p, Man10Shop shop, Man10ShopV2 plugin){
        super("曜日有効化設定",4, plugin);
        this.shop = shop;
        this.player = p;
        this.plugin = plugin;

        setOnClickEvent(e -> e.setCancelled(true));

        //change toggle
        setOnClickEvent(e -> {
            if(!(19 <= e.getRawSlot() && e.getRawSlot() <= 26)) return;
            states.set(e.getRawSlot()-19, !states.get(e.getRawSlot()-19));
            renderSelector();
        });

        states = shop.weekDayToggle.enabledDays.get();

    }

    public void renderDisplay(){
        setItem(10, new SInventoryItem(new SItemStack(Material.SUNFLOWER).setDisplayName("§a§l日曜日").build()).clickable(false));
        setItem(11, new SInventoryItem(new SItemStack(Material.END_STONE).setDisplayName("§a§l月曜日").build()).clickable(false));
        setItem(12, new SInventoryItem(new SItemStack(Material.CAMPFIRE).setDisplayName("§a§l火曜日").build()).clickable(false));
        setItem(13, new SInventoryItem(new SItemStack(Material.WATER_BUCKET).setDisplayName("§a§l水曜日").build()).clickable(false));
        setItem(14, new SInventoryItem(new SItemStack(Material.OAK_LOG).setDisplayName("§a§l木曜日").build()).clickable(false));
        setItem(15, new SInventoryItem(new SItemStack(Material.GOLD_BLOCK).setDisplayName("§a§l金曜日").build()).clickable(false));
        setItem(16, new SInventoryItem(new SItemStack(Material.DIRT).setDisplayName("§a§l土曜日").build()).clickable(false));
    }

    public void renderSelector(){
        for(int i = 19; i < 26; i++){
            if(states.get(i-19)){
                //if on
                setItem(i, new SInventoryItem(new SItemStack(Material.LIME_STAINED_GLASS_PANE).setDisplayName("§a§l有効").build()).clickable(false));
            }else{
                //if off
                setItem(i, new SInventoryItem(new SItemStack(Material.RED_STAINED_GLASS_PANE).setDisplayName("§c§l無効").build()).clickable(false));
            }
        }
        renderInventory();
    }

    public void renderMenu(){
        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        fillItem(background);

        renderDisplay();
        renderSelector();
    }


}
