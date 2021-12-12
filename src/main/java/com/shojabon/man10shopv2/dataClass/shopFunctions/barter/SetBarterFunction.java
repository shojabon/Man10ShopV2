package com.shojabon.man10shopv2.dataClass.shopFunctions.barter;

import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.menus.settings.innerSettings.BarterSettingMenu;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@ShopFunctionDefinition(
        name = "トレード設定",
        explanation = {"トレード対象のアイテムなどを設定します"},
        enabledShopType = {Man10ShopType.BARTER},
        iconMaterial = Material.VILLAGER_SPAWN_EGG,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class SetBarterFunction extends ShopFunction {

    //variables

    public Man10ShopSetting<List<ItemStack>> requiredItems = new Man10ShopSetting<>("shop.barter.required", new ArrayList<>(Arrays.asList(null,null,null,null,null,null,null,null,null,null,null,null)));
    public Man10ShopSetting<List<ItemStack>> resultItems = new Man10ShopSetting<>("shop.barter.result", Arrays.asList(new ItemStack[]{null}));
    //init
    public SetBarterFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions


    //====================
    // settings
    //====================


    @Override
    public boolean performAction(Player p, int amount) {
        //remove items
        HashMap<String, Integer> checkingMap = new HashMap<>();
        for(ItemStack requiredItem: requiredItems.get()){
            if(requiredItem == null) continue;

            String itemHash = new SItemStack(requiredItem).getItemTypeMD5(true);
            if(!checkingMap.containsKey(itemHash)){
                checkingMap.put(itemHash, 0);
            }
            checkingMap.put(itemHash, checkingMap.get(itemHash) + requiredItem.getAmount());

            if(!p.getInventory().containsAtLeast(requiredItem, checkingMap.get(itemHash))){
                p.sendMessage(Man10ShopV2.prefix + "§c§lトレードのためのアイテムが不足しています");
                return false;
            }
        }
        for(ItemStack requiredItem: requiredItems.get()){
            if(requiredItem == null) continue;
            p.getInventory().removeItemAnySlot(requiredItem);
        }
        //give item
        for(ItemStack resultItem: resultItems.get()){
            if(resultItem == null) continue;
            p.getInventory().addItem(resultItem);
        }
        return true;
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        return isAllowedToUseShopWithAmount(p, 1);
    }

    @Override
    public boolean isAllowedToUseShopWithAmount(Player p, int amount) {
        for(ItemStack item: requiredItems.get()){
            if(item != null) return true;
        }
        for(ItemStack item: resultItems.get()){
            if(item != null) return true;
        }
        p.sendMessage(Man10ShopV2.prefix + "§c§lトレードが設定されていません");
        return false;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //required
            List<ItemStack> required = requiredItems.get();
            System.out.println("a");
            List<ItemStack> result = resultItems.get();
            System.out.println("b");
            List<ItemStack> both = new ArrayList<>();
            both.addAll(required);
            both.addAll(result);

            //confirmation menu
            BarterSettingMenu menu = new BarterSettingMenu(player, shop, both, plugin);

            menu.setOnCloseEvent(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));

            menu.setOnConfirm(items -> {
                if(!requiredItems.set(items.subList(0, 12)) || !resultItems.set(List.of(items.get(12)))){
                    warn(player, "内部エラーが発生しました");
                }
                Man10ShopV2API.closeInventoryGroup(shop.getShopId());
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);

            });

            menu.open(player);

        });

        return item;
    }

}
