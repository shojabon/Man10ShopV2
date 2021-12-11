package com.shojabon.man10shopv2.dataClass.shopFunctions.barter;

import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
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

    //init
    public SetBarterFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
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
    public boolean performAction(Player p, int amount) {
        //remove items
        HashMap<String, Integer> checkingMap = new HashMap<>();
        for(ItemStack requiredItem: getRequiredItems()){
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
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //required
            ItemStack[] required = getRequiredItems();
            ItemStack[] result = getResultItems();
            ItemStack[] both = (ItemStack[]) ArrayUtils.addAll(required, result);
            //confirmation menu
            BarterSettingMenu menu = new BarterSettingMenu(player, shop, both, plugin);

            menu.setOnCloseEvent(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));

            menu.setOnConfirm(items -> {
                if(!setRequiredItems(Arrays.copyOfRange(items, 0, 11)) || !setResultItems(new ItemStack[]{items[12]})){
                    warn(player, "内部エラーが発生しました");
                }
            });

            menu.open(player);

        });

        return item;
    }

}
