package com.shojabon.man10shopv2.menus.action;

import ToolMenu.AutoScaledMenu;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class QuestActionMenu extends AutoScaledMenu {

    Man10ShopV2 plugin;
    Player player;
    Man10Shop shop;
    public QuestActionMenu(Player p, Man10Shop shop, Man10ShopV2 plugin){
        super("§b§l可能ショップ一覧", plugin);
        this.player = p;
        this.plugin = plugin;
        this.shop = shop;
        render();
    }

    public void render(){
        setAfterInventoryOpenEvents(e -> inventoryGroup.put(player.getUniqueId(), shop.shopId));
        ArrayList<Man10Shop> shops = Man10ShopV2.api.getShops(shop.mQuestFunction.quest.get().currentQuests);
        for(Man10Shop shop: shops){

            SItemStack icon = new SItemStack(shop.targetItem.getTargetItem().getTypeItem());
            icon.setDisplayName(new SStringBuilder().green().bold().text(shop.name.getName()).build());
            icon.addLore("");
            if(shop.mQuestDialogue.dialogue.get() != null){
                for(String message: shop.mQuestDialogue.dialogue.get().split("\\|")){
                    icon.addLore("§e" + message);
                }
            }

            if(shop.getPlayerAvailableTransactionCount(player) != 0) icon.addLore("§d§l残り取引数:" + shop.getPlayerAvailableTransactionCount(player));
            long time = (this.shop.mQuestTimeWindowFunction.lastPickedTime.get() + Long.parseLong(this.shop.mQuestTimeWindowFunction.minutes.get().toString())*60);
            String timeUntilString = BaseUtils.unixTimeToString(time);
            icon.addLore("§c" + timeUntilString + "まで");

            SInventoryItem item = new SInventoryItem(icon.build());
            item.clickable(false);
            item.setEvent(e -> {
                if(shop.allowedToUseShop(player)){
                    close(player);
                    SInventory inventory = shop.getActionMenu(player);
                    inventory.setOnCloseEvent(ee -> {
                        this.open(player);
                    });
                    inventory.open(player);
                }
            });

            addItem(item);

        }
        //setItems(items);
    }
}
