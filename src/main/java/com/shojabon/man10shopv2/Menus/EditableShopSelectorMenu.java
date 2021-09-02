package com.shojabon.man10shopv2.Menus;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Utils.SInventory.ToolMenu.LargeSInventoryMenu;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.function.Consumer;

public class EditableShopSelectorMenu extends LargeSInventoryMenu{

    Man10ShopV2 plugin;
    Player player;
    Consumer<Man10Shop> onClick = null;

    public EditableShopSelectorMenu(Player p, Man10ShopV2 plugin){
        super(new SStringBuilder().aqua().bold().text("管理可能ショップ一覧").build(), plugin);
        this.player = p;
        this.plugin = plugin;
    }

    public void setOnClick(Consumer<Man10Shop> event){
        this.onClick = event;
    }

    public void renderMenu(){
        ArrayList<SInventoryItem> items = new ArrayList<>();

        ArrayList<Man10Shop> shops = plugin.api.getShopsWithPermission(player.getUniqueId());
        for(Man10Shop shop: shops){

            SItemStack icon = new SItemStack(shop.targetItem.getTypeItem());
            icon.setDisplayName(new SStringBuilder().green().bold().text(shop.name).build());
            icon.addLore(new SStringBuilder().lightPurple().bold().text("権限: ").yellow().bold().text(shop.moderators.get(player.getUniqueId()).getPermissionString()).build());

            SInventoryItem item = new SInventoryItem(icon.build());
            item.clickable(false);
            item.setEvent(e -> {
                if(onClick != null) onClick.accept(shop);
            });

            items.add(item);
        }
        setItems(items);
    }

    public void afterRenderMenu() {
        renderInventory(0);
    }
}
