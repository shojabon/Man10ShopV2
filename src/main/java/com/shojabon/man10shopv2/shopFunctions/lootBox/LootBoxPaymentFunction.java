package com.shojabon.man10shopv2.shopFunctions.lootBox;

import ToolMenu.AutoScaledMenu;
import ToolMenu.NumericInputMenu;
import ToolMenu.SingleItemStackSelectorMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.LootBoxFunction;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

    public Man10ShopSetting<Integer> balancePrice = new Man10ShopSetting<>("lootBox.payment.cash", 0, true);
    public Man10ShopSetting<ItemStack> itemPayment = new Man10ShopSetting<>("lootBox.payment.item", null, true);

    //init
    public LootBoxPaymentFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions

    //====================
    // settings
    //====================

    @Override
    public boolean performAction(Player p, int amount) {
        if(itemPayment.get() != null){
            p.getInventory().removeItemAnySlot(itemPayment.get().clone());
        }
        if(balancePrice.get() != 0){
            if(!Man10ShopV2.vault.withdraw(p.getUniqueId(), balancePrice.get())){
                warn(p, "内部エラーが発生しました");
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isAllowedToUseShopWithAmount(Player p, int amount) {
        if(shop.shopType.getShopType() == Man10ShopType.LOOT_BOX){
            if(itemPayment.get() != null){
                SItemStack item = new SItemStack(itemPayment.get().clone());
                if(!p.getInventory().containsAtLeast(item.build(), item.getAmount())){
                    warn(p, "ガチャを回すためのアイテムがありません");
                    return false;
                }
            }
            if(balancePrice.get() != 0){
                if(Man10ShopV2.vault.getBalance(p.getUniqueId()) < balancePrice.get()){
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
        if(itemPayment.get() != null || balancePrice.get() != 0){
            if(balancePrice.get() != 0){
                result += "現金 " + BaseUtils.priceString(balancePrice.get()) + "円";
                return result;
            }
            if(itemPayment.get() != null){
                result += "アイテム " + new SItemStack(itemPayment.get()).getDisplayName() + " " + new SItemStack(itemPayment.get()).getAmount();
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
                if(!balancePrice.set(number)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "値段を設定しました");
                Man10ShopV2.api.updateAllSigns(shop);
                getInnerSettingMenu(player, plugin).open(player);
            });
            menu.setOnCancel(eee -> getInnerSettingMenu(player, plugin).open(player));
            menu.setOnClose(eee -> getInnerSettingMenu(player, plugin).open(player));

            menu.open(player);
        });

        ItemStack settingItem = new SItemStack(Material.BARRIER).setDisplayName(new SStringBuilder().green().text("アイテム設定").build()).build();

        if(itemPayment.get() != null){
            settingItem = new SItemStack(itemPayment.get().clone()).setDisplayName(new SStringBuilder().green().text("アイテム設定").build()).build();
        }

        SInventoryItem itemSetting = new SInventoryItem(settingItem);
        itemSetting.clickable(false);
        itemSetting.setEvent(ee -> {

            SingleItemStackSelectorMenu menu = new SingleItemStackSelectorMenu("支払いアイテム選択", itemPayment.get(), plugin);
            menu.allowNullItem(true);
            menu.setOnConfirm(item -> {
                if(!itemPayment.set(item)){
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
