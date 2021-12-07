package com.shojabon.man10shopv2.DataClass.ShopFunctions.lootBox;

import ToolMenu.AutoScaledMenu;
import ToolMenu.NumericInputMenu;
import ToolMenu.SingleItemStackSelectorMenu;
import com.shojabon.man10shopv2.DataClass.LootBoxFunction;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.Settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class LootBoxPaymentFunction extends LootBoxFunction {

    //init
    public LootBoxPaymentFunction(Man10Shop shop) {
        super(shop);
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
    public Man10ShopType[] enabledShopTypes() {
        return new Man10ShopType[]{Man10ShopType.LOOT_BOX};
    }

    @Override
    public String settingCategory() {
        return "一般設定";
    }

    @Override
    public boolean hasPermissionToEdit(UUID uuid) {
        return shop.permission.hasPermissionAtLeast(uuid, Man10ShopPermission.MODERATOR);
    }

    @Override
    public boolean performAction(Player p, int amount) {
        if(getItem() != null){
            p.getInventory().removeItemAnySlot(getItem().clone());
        }
        if(getPrice() != 0){
            if(!Man10ShopV2.vault.withdraw(p.getUniqueId(), getPrice())){
                p.sendMessage(Man10ShopV2.gachaPrefix + "§c§l内部エラーが発生しました");
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
                    p.sendMessage(Man10ShopV2.gachaPrefix + "§c§lガチャを回すためのアイテムがありません");
                    return false;
                }
            }
            if(getPrice() != 0){
                if(Man10ShopV2.vault.getBalance(p.getUniqueId()) < getPrice()){
                    p.sendMessage(Man10ShopV2.gachaPrefix + "§c§l残高が不足しています");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public SInventoryItem getAdminSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.HOPPER).setDisplayName(new SStringBuilder().yellow().text("支払い方法設定").build());
        if(getItem() != null || getPrice() != 0){
            item.addLore(new SStringBuilder().lightPurple().text("現在の設定").build());
            if(getPrice() != 0){
                item.addLore(new SStringBuilder().yellow().text("現金 " + BaseUtils.priceString(getPrice()) + "円").build());
            }
            if(getItem() != null){
                item.addLore(new SStringBuilder().yellow().text("アイテム " + new SItemStack(getItem()).getDisplayName() + " " + new SItemStack(getItem()).getAmount()).build());
            }
        }else{
            item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text("なし").build());
        }

        item.addLore("");

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);

        inventoryItem.setEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.gachaPrefix + "§c§l権限が不足しています");
                return;
            }

            sInventory.moveToMenu(player, getInnerSettingMenu(player, plugin));
        });


        return inventoryItem;
    }

    public AutoScaledMenu getInnerSettingMenu(Player player, Man10ShopV2 plugin){
        AutoScaledMenu autoScaledMenu = new AutoScaledMenu("支払い設定", plugin);

        SInventoryItem cashSetting = new SInventoryItem(new SItemStack(Material.EMERALD).setDisplayName(new SStringBuilder().green().text("現金設定").build()).build());
        cashSetting.clickable(false);
        cashSetting.setEvent(ee -> {

            NumericInputMenu menu = new NumericInputMenu("金額を設定してください", plugin);
            menu.setOnConfirm(number -> {
                if(!setPrice(number)){
                    player.sendMessage(Man10ShopV2.gachaPrefix + "§c§l内部エラーが発生しました");
                    return;
                }
                player.sendMessage(Man10ShopV2.gachaPrefix + "§a§l値段を設定しました");
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
                    player.sendMessage(Man10ShopV2.gachaPrefix + "§c§l内部エラーが発生しました");
                    return;
                }
                player.sendMessage(Man10ShopV2.gachaPrefix + "§a§l支払いアイテムを設定しました");
                Man10ShopV2.api.updateAllSigns(shop);
                menu.moveToMenu(player, getInnerSettingMenu(player, plugin));
            });

            menu.setOnCloseEvent(eee -> menu.moveToMenu(player, getInnerSettingMenu(player, plugin)));

            autoScaledMenu.moveToMenu(player, menu);
        });
        autoScaledMenu.setOnCloseEvent(ee -> autoScaledMenu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin)));


        autoScaledMenu.addItem(cashSetting);
        autoScaledMenu.addItem(itemSetting);
        return autoScaledMenu;
    }
}
