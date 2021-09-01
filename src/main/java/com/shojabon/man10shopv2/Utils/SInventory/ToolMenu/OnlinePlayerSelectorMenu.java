package com.shojabon.man10shopv2.Utils.SInventory.ToolMenu;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

public class OnlinePlayerSelectorMenu extends LargeSInventoryMenu{

    Man10ShopV2 plugin;
    Player player;
    Consumer<Player> onClick = null;

    ArrayList<UUID> exceptions = new ArrayList<>();

    public OnlinePlayerSelectorMenu(Player p, Man10ShopV2 plugin){
        super(new SStringBuilder().aqua().bold().text("オンラインプレイヤー一覧").build(), plugin);
        this.player = p;
        this.plugin = plugin;
    }

    public void setOnClick(Consumer<Player> event){
        this.onClick = event;
    }

    public void renderMenu(){
        ArrayList<SInventoryItem> items = new ArrayList<>();

        for(Player p : Bukkit.getServer().getOnlinePlayers()){
            if(exceptions.contains(p.getUniqueId())) continue;

            SItemStack icon = new SItemStack(Material.PLAYER_HEAD);
            icon.setDisplayName(new SStringBuilder().yellow().bold().text(p.getName()).build());
            icon.setHeadOwner(p.getUniqueId());
            SInventoryItem item = new SInventoryItem(icon.build());

            item.clickable(false);
            item.setEvent(e -> {
                if(onClick != null) onClick.accept(p);
            });

            items.add(item);
        }
        setItems(items);
    }

    public void addException(UUID player){
        exceptions.add(player);
    }

    public void afterRenderMenu() {
        renderInventory(0);
    }

}
