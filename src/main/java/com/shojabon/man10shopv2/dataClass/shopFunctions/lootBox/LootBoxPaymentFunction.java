package com.shojabon.man10shopv2.dataClass.shopFunctions.lootBox;

import ToolMenu.AutoScaledMenu;
import ToolMenu.NumericInputMenu;
import ToolMenu.SingleItemStackSelectorMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.LootBoxFunction;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
@ShopFunctionDefinition(
        name = "支払い方法設定",
        explanation = {},
        enabledShopType = {Man10ShopType.LOOT_BOX},
        iconMaterial = Material.EMERALD,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = false
)
public class LootBoxPaymentFunction extends LootBoxFunction {

    //init
    public LootBoxPaymentFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions

    public int getPrice(){
        String currentSetting = getSetting("lootBox.payment.cash");
        if(!BaseUtils.isInt(currentSetting)) return 0;
        return Integer.parseInt(currentSetting);
    }

    public boolean setPrice(int price){
        if(getPrice() == price) return true;
        return setSetting("lootBox.payment.cash", price);
    }

    public ItemStack getItem(){
        String currentSetting = getSetting("lootBox.payment.item");
        if(SItemStack.fromBase64(currentSetting) == null) return null;
        return SItemStack.fromBase64(currentSetting).build();
    }

    public boolean setItem(ItemStack item){
        if(getItem() == item) return true;
        if(item == null) return deleteSetting("lootBox.payment.item");
        return setSetting("lootBox.payment.item", new SItemStack(item.clone()).getBase64());
    }

    //====================
    // settings
    //====================

    @Override
    public boolean performAction(Player p, int amount) {
        if(getItem() != null){
            p.getInventory().removeItemAnySlot(getItem().clone());
        }
        if(getPrice() != 0){
            if(!Man10ShopV2.vault.withdraw(p.getUniqueId(), getPrice())){
                warn(p, "内部エラーが発生しました");
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isAllowedToUseShopWithAmount(Player p, int amount) {
        if(shop.shopType.getShopType() == Man10ShopType.LOOT_BOX){
            if(getItem() != null){
                SItemStack item = new SItemStack(getItem().clone());
                if(!p.getInventory().containsAtLeast(item.build(), item.getAmount())){
                    warn(p, "ガチャを回すためのアイテムがありません");
                    return false;
                }
            }
            if(getPrice() != 0){
                if(Man10ShopV2.vault.getBalance(p.getUniqueId()) < getPrice()){
                    warn(p, "残高が不足しています");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String currentSettingString() {
        String result = "現在の設定\n";
        if(getItem() != null || getPrice() != 0){
            if(getPrice() != 0){
                result += "現金 " + BaseUtils.priceString(getPrice()) + "円";
                return result;
            }
            if(getItem() != null){
                result += "アイテム " + new SItemStack(getItem()).getDisplayName() + " " + new SItemStack(getItem()).getAmount();
                return result;
            }
        }else{
            result += "なし";
            return result;
        }
        return result;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            getInnerSettingMenu(player, plugin).open(player);
        });
        return item;
    }

    public AutoScaledMenu getInnerSettingMenu(Player player, Man10ShopV2 plugin){
        AutoScaledMenu autoScaledMenu = new AutoScaledMenu("支払い設定", plugin);

        SInventoryItem cashSetting = new SInventoryItem(new SItemStack(Material.EMERALD).setDisplayName(new SStringBuilder().green().text("現金設定").build()).build());
        cashSetting.clickable(false);
        cashSetting.setEvent(ee -> {

            NumericInputMenu menu = new NumericInputMenu("金額を設定してください", plugin);
            menu.setOnConfirm(number -> {
                if(!setPrice(number)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "値段を設定しました");
                Man10ShopV2.api.updateAllSigns(shop);
                menu.moveToMenu(player, getInnerSettingMenu(player, plugin));
            });
            menu.setOnCancel(eee -> menu.moveToMenu(player, getInnerSettingMenu(player, plugin)));
            menu.setOnClose(eee -> menu.moveToMenu(player, getInnerSettingMenu(player, plugin)));

            autoScaledMenu.moveToMenu(player, menu);
        });

        ItemStack settingItem = new SItemStack(Material.BARRIER).setDisplayName(new SStringBuilder().green().text("アイテム設定").build()).build();

        if(getItem() != null){
            settingItem = new SItemStack(getItem().clone()).setDisplayName(new SStringBuilder().green().text("アイテム設定").build()).build();
        }

        SInventoryItem itemSetting = new SInventoryItem(settingItem);
        itemSetting.clickable(false);
        itemSetting.setEvent(ee -> {

            SingleItemStackSelectorMenu menu = new SingleItemStackSelectorMenu("支払いアイテム選択", getItem(), plugin);
            menu.allowNullItem(true);
            menu.setOnConfirm(item -> {
                if(!setItem(item)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "支払いアイテムを設定しました");
                Man10ShopV2.api.updateAllSigns(shop);
                getInnerSettingMenu(player, plugin).open(player);
            });

            menu.setOnCloseEvent(eee -> getInnerSettingMenu(player, plugin).open(player));
            menu.open(player);
        });
        autoScaledMenu.setOnCloseEvent(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));


        autoScaledMenu.addItem(cashSetting);
        autoScaledMenu.addItem(itemSetting);
        return autoScaledMenu;
    }
}
