package com.shojabon.man10shopv2.menus;

import ToolMenu.CategoricalSInventoryMenu;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.menus.action.AgentActionMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.function.Consumer;

public class EditableShopSelectorMenu extends CategoricalSInventoryMenu {

    Man10ShopV2 plugin;
    Player player;
    Consumer<Man10Shop> onClick = null;

    public EditableShopSelectorMenu(Player p, String startingCategory, Man10ShopV2 plugin){
        super(new SStringBuilder().aqua().bold().text("管理可能ショップ一覧").build(), startingCategory, plugin);
        this.player = p;
        this.plugin = plugin;
        sort(true);
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
            icon.addLore("§d§lショップタイプ: " + shop.shopType.getShopType().displayName);
            icon.addLore(new SStringBuilder().lightPurple().bold().text("権限: ").yellow().bold().text(shop.permission.getPermissionString(shop.permission.getPermission(player.getUniqueId()))).build());
            icon.addLore("");
            icon.addLore(new SStringBuilder().red().bold().text("在庫: ").yellow().bold().text(BaseUtils.priceString(shop.storage.getItemCount())).text("個").build());
            icon.addLore(new SStringBuilder().red().bold().text("残金: ").yellow().bold().text(BaseUtils.priceString(shop.money.getMoney())).text("円").build());

            SInventoryItem item = new SInventoryItem(icon.build());
            item.clickable(false);
            item.setEvent(e -> {
                if(onClick != null) onClick.accept(shop);
            });

            addItem(shop.categoryFunction.category.get(), item);
        }
        //setItems(items);
    }

    public void afterRenderMenu() {
        super.afterRenderMenu();
        if(player.hasPermission("man10shopv2.admin.agent") && !player.hasPermission("man10shopv2.admin.debug")){
            SInventoryItem debug = new SInventoryItem(new SItemStack(Material.COMMAND_BLOCK).setDisplayName("§c§lデバッグ").build()).clickable(false);
            debug.setEvent(e -> {
                new AgentActionMenu(player, plugin).open(player);
            });
            setItem(47, debug);
            renderInventory();
        }
    }
}
