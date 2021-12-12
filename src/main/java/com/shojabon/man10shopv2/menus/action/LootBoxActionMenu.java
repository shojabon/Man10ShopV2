package com.shojabon.man10shopv2.menus.action;

import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopOrder;
import com.shojabon.man10shopv2.dataClass.lootBox.LootBox;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

public class LootBoxActionMenu extends SInventory{

    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;
    LootBox box;

    boolean orderRequested = false;


    public LootBoxActionMenu(Player p, Man10Shop shop, Man10ShopV2 plugin){
        super("",4, plugin);
        this.shop = shop;
        this.player = p;
        this.plugin = plugin;
        this.box = shop.lootBoxFunction.lootBox.get();

        SStringBuilder builder = new SStringBuilder().darkGray().text(shop.name.getName());

        int maxTradeItemCount = shop.getPlayerAvailableTransactionCount(p);
        if(maxTradeItemCount != 0){
            builder.text(" 残り " + maxTradeItemCount + "回");
        }
        setTitle(builder.build());

        setOnClickEvent(e -> {
            if(e.getClickedInventory() == null) return;
            if(e.getClickedInventory().getType() == InventoryType.PLAYER)e.setCancelled(true);
        });

    }

    public void renderMenu(){
        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        fillItem(background);

        SInventoryItem priceItem = new SInventoryItem(new SItemStack(Material.EMERALD).setDisplayName("§a§l" + BaseUtils.priceString(shop.lootBoxPaymentFunction.balancePrice.get())  + "円").build());
        priceItem.clickable(false);


        if(shop.lootBoxPaymentFunction.balancePrice.get() != 0 || shop.lootBoxPaymentFunction.itemPayment.get() != null){
            if(shop.lootBoxPaymentFunction.balancePrice.get() != 0 && shop.lootBoxPaymentFunction.itemPayment.get() != null){
                SInventoryItem paymentItem = new SInventoryItem(shop.lootBoxPaymentFunction.itemPayment.get().clone());
                paymentItem.clickable(false);

                setItem(12, priceItem);
                setItem(14, paymentItem);
            }else{
                if(shop.lootBoxPaymentFunction.balancePrice.get() != 0){
                    setItem(13, priceItem);
                }else{
                    SInventoryItem paymentItem = new SInventoryItem(shop.lootBoxPaymentFunction.itemPayment.get().clone());
                    paymentItem.clickable(false);

                    setItem(13, paymentItem);
                }
            }
        }

        SInventoryItem confirm = new SInventoryItem(new SItemStack(Material.LIME_STAINED_GLASS_PANE).setDisplayName("§a§l確認").build());
        confirm.clickable(false);
        confirm.setEvent(e -> {
            if(LootBoxPlayMenu.playerInGame.contains(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.gachaPrefix + "§c§lガチャは同時には回せません");
                close(player);
                return;
            }
            if(orderRequested) return;
            Man10ShopV2.api.addTransaction(new Man10ShopOrder(player, shop.getShopId(), 1));
            orderRequested = true;
            close(player);
        });

        setItem(new int[]{30, 31, 32}, confirm);

        renderInventory();
    }

}
