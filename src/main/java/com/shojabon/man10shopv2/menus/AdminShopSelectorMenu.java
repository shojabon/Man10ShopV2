package com.shojabon.man10shopv2.menus;

import ToolMenu.CategoricalSInventoryMenu;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.function.Consumer;

public class AdminShopSelectorMenu extends CategoricalSInventoryMenu {

    Man10ShopV2 plugin;
    Player player;
    Consumer<Man10Shop> onClick = null;

    public AdminShopSelectorMenu(Player p, String startingCategory, Man10ShopV2 plugin){
        super(new SStringBuilder().aqua().bold().text("管理可能ショップ一覧").build(), startingCategory, plugin);
        this.player = p;
        this.plugin = plugin;
    }

    public void setOnClick(Consumer<Man10Shop> event){
        this.onClick = event;
    }

    public void renderMenu(){
        addInitializedCategory("その他");

        ArrayList<Man10Shop> shops = Man10ShopV2.api.getAdminShops();
        for(Man10Shop shop: shops){

            SItemStack icon = new SItemStack(shop.targetItem.getTargetItem().getTypeItem());
            icon.setDisplayName(new SStringBuilder().green().bold().text(shop.name.getName()).build());
            icon.addLore("§d§lショップタイプ: " + shop.shopType.shopTypeToString(shop.shopType.getShopType()));

            SInventoryItem item = new SInventoryItem(icon.build());
            item.clickable(false);
            item.setEvent(e -> {
                if(onClick != null) onClick.accept(shop);
            });

            addItem(shop.categoryFunction.getCategory(), item);
        }
    }

}
