package com.shojabon.man10shopv2.DataClass.ShopFunctions.barter;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.BarterSettingMenu;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.WeekdayShopToggleMenu;
import com.shojabon.man10shopv2.Menus.Settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
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

import java.util.Arrays;
import java.util.UUID;

public class SetBarterFunction extends ShopFunction {

    //variables

    //init
    public SetBarterFunction(Man10Shop shop) {
        super(shop);
    }


    //functions


    //====================
    // settings
    //====================

    public ItemStack[] getResultItems(){
        String currentSetting = getSetting("shop.barter.result");
        if(currentSetting == null) return new ItemStack[1];
        ItemStack[] result = new ItemStack[1];
        String[] items = currentSetting.split("\\|");
        for(int i = 0; i < items.length; i++){
            try{
                result[i] = SItemStack.fromBase64(items[i]).build();
            }catch (Exception e){
                result[i] = null;
            }
        }
        return result;
    }

    public boolean setResultItems(ItemStack[] items){
        if(getResultItems() == items) return true;
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < items.length; i++){
            if(items[i] == null){
                result.append("|");
                continue;
            }
            result.append(new SItemStack(items[i]).getBase64()).append("|");
        }
        if(!setSetting("shop.barter.result", result.substring(0, result.length()-1))) return false;
        Man10ShopV2API.closeInventoryGroup(shop.getShopId());
        return true;
    }


    public ItemStack[] getRequiredItems(){
        String currentSetting = getSetting("shop.barter.required");
        if(currentSetting == null) return new ItemStack[12];
        ItemStack[] result = new ItemStack[12];
            String[] items = currentSetting.split("\\|");
            for(int i = 0; i < items.length; i++){
                try{
                    result[i] = SItemStack.fromBase64(items[i]).build();
                }catch (Exception e){
                    result[i] = null;
                }
            }
        return result;
    }

    public boolean setRequiredItems(ItemStack[] items){
        if(getResultItems() == items) return true;
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < items.length; i++){
            if(items[i] == null){
                result.append("|");
                continue;
            }
            result.append(new SItemStack(items[i]).getBase64()).append("|");
        }
        if(!setSetting("shop.barter.required", result.substring(0, result.length()-1))) return false;
        Man10ShopV2API.closeInventoryGroup(shop.getShopId());
        return true;
    }

    @Override
    public Man10ShopType[] enabledShopTypes() {
        return new Man10ShopType[]{Man10ShopType.BARTER};
    }

    @Override
    public String settingCategory() {
        return "一般設定";
    }

    @Override
    public boolean performAction(Player p, int amount) {
        //remove items
        for(ItemStack requiredItem: getRequiredItems()){
            if(requiredItem == null) continue;
            if(!p.getInventory().containsAtLeast(requiredItem, requiredItem.getAmount())){
                p.sendMessage(Man10ShopV2.prefix + "§c§lトレードのためのアイテムが不足しています");
                return false;
            }
        }
        for(ItemStack requiredItem: getRequiredItems()){
            if(requiredItem == null) continue;
            p.getInventory().removeItemAnySlot(requiredItem);
        }
        //give item
        for(ItemStack resultItem: getResultItems()){
            if(resultItem == null) continue;
            p.getInventory().addItem(resultItem);
        }
        return true;
    }

    @Override
    public boolean hasPermissionToEdit(UUID uuid) {
        return shop.permission.hasPermissionAtLeast(uuid, Man10ShopPermission.MODERATOR);
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        return isAllowedToUseShopWithAmount(p, 1);
    }

    @Override
    public boolean isAllowedToUseShopWithAmount(Player p, int amount) {
        for(ItemStack item: getRequiredItems()){
            if(item != null) return true;
        }
        for(ItemStack item: getResultItems()){
            if(item != null) return true;
        }
        p.sendMessage(Man10ShopV2.prefix + "§c§lトレードが設定されていません");
        return false;
    }

    @Override
    public SInventoryItem getAdminSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.VILLAGER_SPAWN_EGG).setDisplayName(new SStringBuilder().gray().text("トレード設定").build());

        item.addLore("");
        item.addLore("§fトレード対象のアイテムなどを設定します");


        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);

        inventoryItem.setEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }

            //required
            ItemStack[] required = getRequiredItems();
            ItemStack[] result = getResultItems();
            ItemStack[] both = (ItemStack[]) ArrayUtils.addAll(required, result);
            //confirmation menu
            BarterSettingMenu menu = new BarterSettingMenu(player, shop, both, plugin);

            menu.setAsyncOnCloseEvent(ee -> {
                sInventory.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin));
            });

            menu.setOnConfirm(items -> {
                if(!setRequiredItems(Arrays.copyOfRange(items, 0, 11)) || !setResultItems(new ItemStack[]{items[12]})){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                }
                sInventory.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin));
            });

            sInventory.moveToMenu(player, menu);

        });

        return inventoryItem;
    }

}
