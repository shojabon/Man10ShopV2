package com.shojabon.man10shopv2.Menus.action;

import ToolMenu.CategoricalSInventoryMenu;
import ToolMenu.LargeSInventoryMenu;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.function.Consumer;

public class QuestActionMenu extends LargeSInventoryMenu {

    Man10ShopV2 plugin;
    Player player;
    Man10Shop shop;
    public QuestActionMenu(Player p, Man10Shop shop, Man10ShopV2 plugin){
        super(new SStringBuilder().aqua().bold().text("管理可能ショップ一覧").build(), plugin);
        this.player = p;
        this.plugin = plugin;
        this.shop = shop;
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, this::renderMenu, 20, 20);
        setOnCloseEvent(e -> {task.cancel();});
    }

    public void renderMenu(){
        ArrayList<SInventoryItem> items = new ArrayList<>();


        setAfterInventoryOpenEvents(e -> inventoryGroup.put(player.getUniqueId(), shop.shopId));
        ArrayList<Man10Shop> shops = Man10ShopV2.api.getShops(shop.mQuestFunction.getQuest().currentQuests);
        for(Man10Shop shop: shops){

            SItemStack icon = new SItemStack(shop.targetItem.getTargetItem().getTypeItem());
            icon.setDisplayName(new SStringBuilder().green().bold().text(shop.name.getName()).build());

            SInventoryItem item = new SInventoryItem(icon.build());
            item.clickable(false);
            item.setEvent(e -> {
                if(shop.allowedToUseShop(player)){
                    close(player);
                    SInventory inventory = shop.getActionMenu(player);
                    inventory.setOnCloseEvent(ee -> {
                        inventory.moveToMenu(player, this);
                    });
                    moveToMenu(player, inventory);
                }
            });

            items.add(item);

        }

        setItems(items);
        renderInventory();
        //setItems(items);
    }
}
