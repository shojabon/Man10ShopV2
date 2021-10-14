package com.shojabon.man10shopv2.Menus.Settings.InnerSettings;

import ToolMenu.LargeSInventoryMenu;
import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;

import java.util.ArrayList;
import java.util.function.Consumer;

public class RandomPricePriceSelector extends LargeSInventoryMenu {

    Man10ShopV2 plugin;
    Player player;
    Man10Shop shop;

    public RandomPricePriceSelector(Player p, Man10Shop shop, Man10ShopV2 plugin){
        super(new SStringBuilder().aqua().bold().text("値段群一覧").build(), plugin);
        this.player = p;
        this.plugin = plugin;
        this.shop = shop;
    }

    public void renderMenu(){
        ArrayList<SInventoryItem> items = new ArrayList<>();

        for(int price: shop.randomPrice.getPrices()){

            SItemStack icon = new SItemStack(Material.EMERALD);
            icon.setDisplayName(new SStringBuilder().green().bold().text(price).build());
            icon.addLore("§c§l右クリックで削除");

            SInventoryItem item = new SInventoryItem(icon.build());
            item.clickable(false);
            item.setAsyncEvent(e -> {
                if(e.getClick() != ClickType.RIGHT) return;
                ArrayList<Integer> newPrices = shop.randomPrice.getPrices();
                newPrices.remove((Integer) price);
                if(!shop.randomPrice.setPrices(newPrices)){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                    return;
                }

                moveToMenu(player, new RandomPricePriceSelector(player, shop, plugin));
            });

            items.add(item);
        }
        setItems(items);
        setOnCloseEvent(ee -> moveToMenu(player, new RandomPriceMenu(player, shop, plugin)));
    }

    public void afterRenderMenu() {
        renderInventory(0);

        SInventoryItem addPrice = new SInventoryItem(new SItemStack(Material.DISPENSER).setDisplayName("§a§l値段を追加する").build()).clickable(false);
        addPrice.setEvent(e -> {
            NumericInputMenu priceInput = new NumericInputMenu("§b§l値段を入力してください", plugin);
            priceInput.setOnCloseEvent(ee -> priceInput.moveToMenu(player, new RandomPricePriceSelector(player, shop, plugin)));
            priceInput.setOnConfirm(number -> {
                ArrayList<Integer> newPrices = shop.randomPrice.getPrices();
                newPrices.add(number);
                if(!shop.randomPrice.setPrices(newPrices)){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                    return;
                }

                priceInput.moveToMenu(player, new RandomPricePriceSelector(player, shop, plugin));
            });
            moveToMenu(player, priceInput);
        });

        setItem(51, addPrice);
        renderInventory();
    }
}
