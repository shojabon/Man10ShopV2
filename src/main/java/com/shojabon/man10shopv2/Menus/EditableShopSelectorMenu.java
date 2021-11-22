package com.shojabon.man10shopv2.Menus;

import ToolMenu.CategoricalSInventoryMenu;
import ToolMenu.LargeSInventoryMenu;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.Consumer;

public class EditableShopSelectorMenu extends CategoricalSInventoryMenu {

    Man10ShopV2 plugin;
    Player player;
    Consumer<Man10Shop> onClick = null;

    public EditableShopSelectorMenu(Player p, Man10ShopV2 plugin){
        super(new SStringBuilder().aqua().bold().text("管理可能ショップ一覧").build(), "その他", plugin);
        this.player = p;
        this.plugin = plugin;
    }

    public void setOnClick(Consumer<Man10Shop> event){
        this.onClick = event;
    }

    public void renderMenu(){
        addInitializedCategory("その他");

        ArrayList<Man10Shop> shops = Man10ShopV2.api.getShopsWithPermission(player.getUniqueId());
        for(Man10Shop shop: shops){

            SItemStack icon = new SItemStack(shop.targetItem.getTargetItem().getTypeItem());
            icon.setDisplayName(new SStringBuilder().green().bold().text(shop.name.getName()).build());
            icon.addLore(new SStringBuilder().lightPurple().bold().text("権限: ").yellow().bold().text(shop.permission.getPermissionString(shop.permission.getPermission(player.getUniqueId()))).build());
            icon.addLore("");
            icon.addLore(new SStringBuilder().red().bold().text("在庫: ").yellow().bold().text(BaseUtils.priceString(shop.storage.getItemCount())).text("個").build());
            icon.addLore(new SStringBuilder().red().bold().text("残金: ").yellow().bold().text(BaseUtils.priceString(shop.money.getMoney())).text("円").build());

            SInventoryItem item = new SInventoryItem(icon.build());
            item.clickable(false);
            item.setEvent(e -> {
                if(onClick != null) onClick.accept(shop);
            });

            addItem(shop.categoryFunction.getCategory(), item);
        }
        //setItems(items);
    }

//    public void afterRenderMenu() {
//        renderInventory();
//    }
}
