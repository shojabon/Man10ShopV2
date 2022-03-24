package com.shojabon.man10shopv2.shopFunctions.general;

import ToolMenu.AutoScaledMenu;
import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@ShopFunctionDefinition(
        name = "ビットコイン連動価格",
        explanation = {},
        enabledShopType = {Man10ShopType.BUY, Man10ShopType.SELL},
        iconMaterial = Material.GOLDEN_APPLE,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class BitcoinPriceFunction extends ShopFunction {

    //variables
    public Man10ShopSetting<Boolean> enabled = new Man10ShopSetting<>("bitcoin.enabled", false);


    public Man10ShopSetting<Integer> top = new Man10ShopSetting<>("bitcoin.top", 0);
    public Man10ShopSetting<Integer> bottom = new Man10ShopSetting<>("bitcoin.bottom", 0);
    public Man10ShopSetting<Integer> multiplier = new Man10ShopSetting<>("bitcoin.multiplier", 0);
    public Man10ShopSetting<Integer> maxPrice = new Man10ShopSetting<>("bitcoin.maxPrice", 0);

    //init
    public BitcoinPriceFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions


    //====================
    // settings
    //====================


    @Override
    public void perMinuteExecuteTask() {
        if(!enabled.get()){
            return;
        }
        if(bottom.get() == 0 || multiplier.get() == 0){
            return;
        }


        long newPrice = (long) (((double) (Man10ShopV2.api.getBitcoinPrice() * multiplier.get())/bottom.get()) * top.get());
        if(newPrice > maxPrice.get()){
            newPrice = maxPrice.get();
        }
        shop.price.setPrice((int) newPrice);
        Man10ShopV2.api.updateAllSigns(shop);
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        return true;
    }

    @Override
    public boolean isAllowedToUseShopWithAmount(Player p, int amount) {
        return true;
    }

    public void openInnerSettingMenu(Player player){
        AutoScaledMenu menu = new AutoScaledMenu("§6§lビットコイン連動価格設定メニュー", plugin);

        Material enabledItemType = Material.REDSTONE_BLOCK;
        if(enabled.get()){
            enabledItemType = Material.EMERALD_BLOCK;
        }
        SInventoryItem enabledItem = new SInventoryItem(new SItemStack(enabledItemType).setDisplayName("§6§l有効化設定").addLore("§c§l現在:" + BaseUtils.booleanToJapaneseText(enabled.get())).build()).clickable(false);
        enabledItem.setEvent(click -> {
            enabled.set(!enabled.get());
            success(player, "設定を変更しました");
            openInnerSettingMenu(player);
        });

        SInventoryItem maxPriceItem = new SInventoryItem(new SItemStack(Material.LEATHER_HELMET).setDisplayName("§6§l最大金額設定").addLore("§c§l現在:" + BaseUtils.priceString(maxPrice.get()) + "円").build()).clickable(false);
        maxPriceItem.setEvent(click -> {
            NumericInputMenu numberInput = new NumericInputMenu("金額を入力してください", plugin);
            numberInput.setOnCloseEvent(close -> {
                openInnerSettingMenu(player);
            });
            numberInput.setDefaultValue(maxPrice.get());
            numberInput.setAllowZero(true);
            numberInput.setOnCancel(cancel -> openInnerSettingMenu(player));
            numberInput.setOnConfirm(confirm -> {
                success(player, "設定を変更しました");
                maxPrice.set(confirm);
                openInnerSettingMenu(player);
            });
            numberInput.open(player);
        });


        SInventoryItem topItem = new SInventoryItem(new SItemStack(Material.GRASS_BLOCK).setDisplayName("§6§l分子設定").addLore("§c§l現在:" + BaseUtils.priceString(top.get())).build()).clickable(false);
        topItem.setEvent(click -> {
            NumericInputMenu numberInput = new NumericInputMenu("分子を入力してください", plugin);
            numberInput.setOnCloseEvent(close -> {
                openInnerSettingMenu(player);
            });
            numberInput.setDefaultValue(top.get());
            numberInput.setAllowZero(true);
            numberInput.setOnCancel(cancel -> openInnerSettingMenu(player));
            numberInput.setOnConfirm(confirm -> {
                success(player, "設定を変更しました");
                top.set(confirm);
                openInnerSettingMenu(player);
            });
            numberInput.open(player);
        });


        SInventoryItem bottomItem = new SInventoryItem(new SItemStack(Material.BEDROCK).setDisplayName("§6§l分母設定").addLore("§c§l現在:" + BaseUtils.priceString(bottom.get())).build()).clickable(false);
        bottomItem.setEvent(click -> {
            NumericInputMenu numberInput = new NumericInputMenu("分母を入力してください", plugin);
            numberInput.setOnCloseEvent(close -> {
                openInnerSettingMenu(player);
            });
            numberInput.setDefaultValue(bottom.get());
            numberInput.setAllowZero(true);
            numberInput.setOnCancel(cancel -> openInnerSettingMenu(player));
            numberInput.setOnConfirm(confirm -> {
                success(player, "設定を変更しました");
                bottom.set(confirm);
                openInnerSettingMenu(player);
            });
            numberInput.open(player);
        });


        SInventoryItem multiplierItem = new SInventoryItem(new SItemStack(Material.COMPARATOR).setDisplayName("§6§l倍率設定設定").addLore("§c§l現在:" + multiplier.get() + "倍").build()).clickable(false);
        multiplierItem.setEvent(click -> {
            NumericInputMenu numberInput = new NumericInputMenu("倍率を入力してください", plugin);
            numberInput.setOnCloseEvent(close -> {
                openInnerSettingMenu(player);
            });
            numberInput.setDefaultValue(multiplier.get());
            numberInput.setAllowZero(true);
            numberInput.setOnCancel(cancel -> openInnerSettingMenu(player));
            numberInput.setOnConfirm(confirm -> {
                success(player, "設定を変更しました");
                multiplier.set(confirm);
                openInnerSettingMenu(player);
            });
            numberInput.open(player);
        });





        menu.addItem(topItem);
        menu.addItem(enabledItem);
        menu.addItem(bottomItem);
        menu.addItem(maxPriceItem);
        menu.addItem(multiplierItem);

        menu.setOnCloseEvent(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
        menu.open(player);
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {

        item.setAsyncEvent(e -> {
            openInnerSettingMenu(player);
        });


        return item;
    }

}
