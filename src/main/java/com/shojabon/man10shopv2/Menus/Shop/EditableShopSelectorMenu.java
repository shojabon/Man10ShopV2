package com.shojabon.man10shopv2.Menus.Shop;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.Man10ShopModerator;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.LargeSInventoryMenu;
import com.shojabon.man10shopv2.Menus.Shop.Permission.PermissionSettingsMenu;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

public class EditableShopSelectorMenu {

    Man10ShopV2 plugin;
    Player player;
    public LargeSInventoryMenu inventory;
    Consumer<Man10Shop> onClick = null;

    public EditableShopSelectorMenu(Player p, Man10ShopV2 plugin){
        this.player = p;
        this.plugin = plugin;
        inventory = new LargeSInventoryMenu(new SStringBuilder().aqua().bold().text("管理可能ショップ一覧").build(), 5, plugin);;
    }

    public void setOnClick(Consumer<Man10Shop> event){
        this.onClick = event;
    }

    public SInventory getInventory(){
        render();
        return inventory.getInventory();
    }

    public void render(){
        ArrayList<SInventoryItem> items = new ArrayList<>();

        ArrayList<Man10Shop> shops = plugin.api.getShopsWithPermission(player.getUniqueId());
        for(Man10Shop shop: shops){

            SItemStack icon = new SItemStack(shop.targetItem.getTypeItem());
            icon.setDisplayName(new SStringBuilder().green().bold().text(shop.name).build());
            icon.addLore(new SStringBuilder().lightPurple().bold().text("権限: ").yellow().bold().text(String.valueOf(shop.getPermission(player.getUniqueId()))).build());

            SInventoryItem item = new SInventoryItem(icon.build());
            item.clickable(false);
            item.setEvent(e -> {
                if(onClick != null) onClick.accept(shop);
            });

            items.add(item);
        }
        inventory.setItems(items);
    }


}
