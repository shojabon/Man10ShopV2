package com.shojabon.man10shopv2.Menus.Settings;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.PerMinuteCoolDownSelectorMenu;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.ShopTypeSelectorMenu;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.WeekdayShopToggleMenu;
import com.shojabon.man10shopv2.Utils.SInventory.ToolMenu.BooleanInputMenu;
import com.shojabon.man10shopv2.Utils.SInventory.ToolMenu.ConfirmationMenu;
import com.shojabon.man10shopv2.Utils.SInventory.ToolMenu.LargeSInventoryMenu;
import com.shojabon.man10shopv2.Utils.SInventory.ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.Menus.ShopMainMenu;
import com.shojabon.man10shopv2.Utils.BaseUtils;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SLongTextInput;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;

public class SettingsMainMenu extends LargeSInventoryMenu{
    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;


    public SettingsMainMenu(Player p, Man10Shop shop, Man10ShopV2 plugin) {
        super(new SStringBuilder().darkGray().text("ショップ設定").build(), plugin);
        this.player = p;
        this.shop = shop;
        this.plugin = plugin;
    }

    public void renderMenu(){
        ArrayList<SInventoryItem> items = new ArrayList<>();
        //define items here

        items.add(shopEnabledItem());
        items.add(sellPriceItem());
        items.add(shopTypeSelectItem());
        items.add(setNameItem());
        items.add(buyStorageItem());
        items.add(sellCapItem());
        items.add(singleTransactionItem());
        items.add(coolDownTimeItem());
        items.add(setAllowedPermissionItem());
        items.add(weekdayShopToggleItem());
        items.add(perMinuteCoolDownItem());




        items.add(setDeleteShopItem());

        setItems(items);
        setOnCloseEvent(e -> moveToMenu(player, new ShopMainMenu(player, shop, plugin)));
    }

    public SInventoryItem setNameItem(){
        SItemStack item = new SItemStack(Material.NAME_TAG).setDisplayName(new SStringBuilder().gold().text("ショップの名前を変更する").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(shop.name).build());

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setAsyncEvent(e -> {

            //text input
            SLongTextInput textInput = new SLongTextInput("§d§lショップ名を入力してください", plugin);
            textInput.setOnConfirm(shopName -> {
                if(shopName.length() > 64 || shopName.length() == 0){
                    player.sendMessage(Man10ShopV2.prefix + "§c§lショップ名は64文字以内でなくてはなりません");
                    return;
                }
                threadPool.execute(() -> {
                    if(!shop.setName(shopName)){
                        player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                        return;
                    }
                    player.sendMessage(Man10ShopV2.prefix + "§a§lショップ名を変更しました");
                });
            });

            textInput.setOnCancel(ee -> player.sendMessage(Man10ShopV2.prefix + "§c§lキャンセルしました"));


            textInput.open(player);
            close(player);
        });

        return inventoryItem;
    }

    public SInventoryItem setAllowedPermissionItem(){
        SItemStack item = new SItemStack(Material.IRON_DOOR).setDisplayName(new SStringBuilder().gold().text("ショップを使用可能な権限を設定する").build());
        SStringBuilder currentSetting = new SStringBuilder().lightPurple().text("現在の設定: ").yellow();
        if(shop.allowedPermission.getAllowedPermission() == null){
            currentSetting.text("なし");
        }else{
            currentSetting.text("man10shopv2.use." + shop.allowedPermission.getAllowedPermission());
        }
        item.addLore(currentSetting.build());

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setAsyncEvent(e -> {

            //text input
            SLongTextInput textInput = new SLongTextInput("§d§l権限を入力してください man10shopv2.use.XXXX 空白の場合はなし", plugin);
            textInput.setOnConfirm(permissionName -> {
                if(permissionName.length() > 64){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l権限は64文字以内でなくてはなりません");
                    return;
                }
                threadPool.execute(() -> {
                    if(!shop.allowedPermission.setAllowedPermission(permissionName)){
                        player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                        return;
                    }
                    Man10ShopV2API.log(shop.shopId, "setShopAllowedPermission", permissionName, player.getName(), player.getUniqueId()); //log
                    player.sendMessage(Man10ShopV2.prefix + "§a§l権限を変更しました");
                });
            });

            textInput.setOnCancel(ee -> player.sendMessage(Man10ShopV2.prefix + "§c§lキャンセルしました"));


            textInput.open(player);
            close(player);
        });

        return inventoryItem;
    }

    public SInventoryItem sellPriceItem(){
        SItemStack item = new SItemStack(Material.EMERALD).setDisplayName(new SStringBuilder().green().text("取引価格設定").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(BaseUtils.priceString(shop.price)).text("円").build());

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(e -> {

            //number input menu
            NumericInputMenu menu = new NumericInputMenu(new SStringBuilder().green().text("取引値段設定").build(), plugin);
            menu.setOnClose(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));
            menu.setOnCancel(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));
            menu.setOnConfirm(newValue -> {
                if(shop.setPrice(newValue)){
                    Man10ShopV2API.log(shop.shopId, "setPrice", newValue, player.getName(), player.getUniqueId()); //log
                }
                plugin.api.updateAllSigns(shop);
                menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin));
            });
            moveToMenu(player, menu);

        });

        return inventoryItem;
    }

    public SInventoryItem buyStorageItem(){
        SItemStack item = new SItemStack(Material.CHEST).setDisplayName(new SStringBuilder().gray().text("ショップの倉庫を拡張する").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の倉庫サイズ: ").yellow().text(shop.storage.storageSize).text("個").build());
        item.addLore("");

        int unitsTillMax = Man10ShopV2.config.getInt("itemStorage.maxStorageUnits") - shop.storage.getBoughtStorageUnits();

        if(shop.storage.calculateNextUnitPrice(1) != -1){
            item.addLore(new SStringBuilder().red().text("次のサイズ: ").text(shop.storage.calculateCurrentStorageSize(1)).text("個").build());
            item.addLore(new SStringBuilder().yellow().text("価格: ").text(BaseUtils.priceString(shop.storage.calculateNextUnitPrice(1))).text("円").build());
            item.addLore(new SStringBuilder().white().bold().text("左クリックで購入").build());
            item.addLore(new SStringBuilder().white().bold().text("左シフトクリックで最大まで買う").yellow().text("価格:")
                    .text(BaseUtils.priceString(shop.storage.calculateNextUnitPrice(unitsTillMax))).text("円").build());
        }
        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setAsyncEvent(e -> {
            int buyingUnits = 1;

            if(e.getClick() == ClickType.SHIFT_LEFT) buyingUnits = unitsTillMax;


            //confirmation menu
            ConfirmationMenu menu = new ConfirmationMenu("確認", plugin);
            menu.setOnClose(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));
            menu.setOnCancel(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));


            int finalBuyingUnits = buyingUnits;
            menu.setOnConfirm(ee -> {
                if(shop.storage.buyStorageSpace(player, finalBuyingUnits)){
                    Man10ShopV2API.log(shop.shopId, "buyStorageSpace", 1, player.getName(), player.getUniqueId()); //log
                }
                menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin));
            });

            moveToMenu(player, menu);

        });


        return inventoryItem;
    }

    public SInventoryItem sellCapItem(){
        SItemStack item = new SItemStack(Material.HOPPER).setDisplayName(new SStringBuilder().green().text("買取制限").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(shop.storageCap.getStorageCap()).build());
        item.addLore("");
        item.addLore("§f※買取ショップの場合のみ有効");
        item.addLore("§f買取数の上限を設定する");
        item.addLore("§f買取数上限を0にすると倉庫があるだけ買い取ります");

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(e -> {

            //number input menu
            NumericInputMenu menu = new NumericInputMenu(new SStringBuilder().green().text("購入制限設定").build(), plugin);
            if(!shop.admin) menu.setMaxValue(shop.storage.storageSize);
            menu.setOnClose(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));
            menu.setOnCancel(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));
            menu.setOnConfirm(newValue -> {
                if(newValue > shop.storage.storageSize && !shop.admin){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l購入制限は倉庫以上の数にはできません");
                    return;
                }
                if(newValue < 0){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l購入制限は正の数でなくてはならない");
                    return;
                }

                if(shop.storageCap.setStorageCap(newValue)){
                    Man10ShopV2API.log(shop.shopId, "setStorageCap", newValue, player.getName(), player.getUniqueId()); //log
                }
                menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin));
            });
            moveToMenu(player, menu);

        });

        return inventoryItem;
    }

    public SInventoryItem coolDownTimeItem(){
        SItemStack item = new SItemStack(Material.CLOCK).setDisplayName(new SStringBuilder().yellow().text("取引クールダウン").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(shop.coolDown.getCoolDownTime()).text("秒").build());
        item.addLore("");
        item.addLore("§f取引を制限する");
        item.addLore("§f設定秒に1回のみしか取引できなくなります");
        item.addLore("§f0の場合はクールダウンなし");

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(e -> {

            //number input menu
            NumericInputMenu menu = new NumericInputMenu(new SStringBuilder().green().text("取引クールダウン").build(), plugin);
            menu.setOnClose(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));
            menu.setOnCancel(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));
            menu.setOnConfirm(newValue -> {
                if(newValue < 0){
                    player.sendMessage(Man10ShopV2.prefix + "§c§lクールダウンタイムは正の数でなくてはならない");
                    return;
                }

                if(shop.coolDown.setCoolDown(newValue)){
                    Man10ShopV2API.log(shop.shopId, "setCoolDownTime", newValue, player.getName(), player.getUniqueId()); //log
                }
                menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin));
            });
            moveToMenu(player, menu);

        });

        return inventoryItem;
    }

    public SInventoryItem shopEnabledItem(){
        SItemStack item = new SItemStack(Material.LEVER).setDisplayName(new SStringBuilder().gray().text("ショップ取引有効").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(BaseUtils.booleanToJapaneseText(shop.shopEnabled.getShopEnabled())).build());
        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(e -> {
            //confirmation menu
            BooleanInputMenu menu = new BooleanInputMenu(shop.shopEnabled.getShopEnabled(), "ショップ有効化設定", plugin);
            menu.setOnClose(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));
            menu.setOnCancel(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));
            menu.setOnConfirm(bool -> {
                if(shop.shopEnabled.setShopEnabled(bool)){
                    Man10ShopV2API.log(shop.shopId, "enableShop", bool, player.getName(), player.getUniqueId()); //log
                }
                plugin.api.updateAllSigns(shop);
                menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin));
            });

            moveToMenu(player, menu);

        });


        return inventoryItem;
    }

    public SInventoryItem singleTransactionItem(){
        SItemStack item = new SItemStack(Material.BOWL).setDisplayName(new SStringBuilder().gray().text("単品取引モード").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(BaseUtils.booleanToJapaneseText(shop.singleTransactionMode.isSingleTransactionMode())).build());
        item.addLore("");
        item.addLore("§fまとめて取引ができなくなります");
        item.addLore("§f1個ずつのみの取引になります");
        item.addLore("§fイベントなど盛り上げたいときに使います");


        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(e -> {
            //confirmation menu
            BooleanInputMenu menu = new BooleanInputMenu(shop.singleTransactionMode.isSingleTransactionMode(), "単品取引モード", plugin);
            menu.setOnClose(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));
            menu.setOnCancel(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));
            menu.setOnConfirm(bool -> {
                if(shop.singleTransactionMode.setSingleSellMode(bool)){
                    Man10ShopV2API.log(shop.shopId, "setSingleSellMode", bool, player.getName(), player.getUniqueId()); //log
                }
                menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin));
            });

            moveToMenu(player, menu);

        });


        return inventoryItem;
    }

    public SInventoryItem shopTypeSelectItem(){
        SItemStack item = new SItemStack(Material.OAK_FENCE_GATE).setDisplayName(new SStringBuilder().yellow().text("ショップタイプ設定").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(BaseUtils.buySellToString(shop.shopType)).build());
        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);

        inventoryItem.setEvent(e -> {
            if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //confirmation menu
            moveToMenu(player, new ShopTypeSelectorMenu(player, shop, plugin));

        });


        return inventoryItem;
    }

    public SInventoryItem weekdayShopToggleItem(){
        SItemStack item = new SItemStack(Material.COMPARATOR).setDisplayName(new SStringBuilder().red().text("曜日有効化設定").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定").build());
        int i = 0;
        for(boolean res: shop.weekDayToggle.getWeekdayShopToggle()){
            SStringBuilder builder = new SStringBuilder();
            builder.yellow().text(BaseUtils.weekToString(i) + ": ");
            if(res){
                builder.green().text("有効");
            }else{
                builder.red().text("無効");
            }
            item.addLore(builder.build());
            i++;
        }

        item.addLore("");
        item.addLore("§f特定の曜日にショップを有効かするかを設定する");


        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);

        inventoryItem.setEvent(e -> {
            if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //confirmation menu
            WeekdayShopToggleMenu menu = new WeekdayShopToggleMenu(player, shop, plugin);

            menu.setAsyncOnCloseEvent(ee -> {
                if(shop.weekDayToggle.setWeekdayShopToggle(menu.states)){
                    Man10ShopV2API.log(shop.shopId, "setWeekdayShopToggle", shop.weekDayToggle.getWeekdayShopToggle(), player.getName(), player.getUniqueId()); //log
                }
                player.sendMessage(Man10ShopV2.prefix + "§a§l曜日設定をしました");
                moveToMenu(player, new SettingsMainMenu(player, shop, plugin));
            });

            moveToMenu(player, menu);

        });


        return inventoryItem;
    }

    public SInventoryItem perMinuteCoolDownItem(){
        SItemStack item = new SItemStack(Material.DISPENSER).setDisplayName(new SStringBuilder().yellow().text("分間毎ごとのクールダウン設定").build());
        if(shop.perMinuteCoolDown.getPerMinuteCoolDownAmount() != 0 && shop.perMinuteCoolDown.getPerMinuteCoolDownTime() != 0){
            item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(shop.perMinuteCoolDown.getPerMinuteCoolDownTime()).text("分毎に").text(shop.perMinuteCoolDown.getPerMinuteCoolDownAmount()).text("個").build());
        }else{
            item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text("なし").build());
        }

        item.addLore("");
        item.addLore("§f取引を制限します");
        item.addLore("§f分間毎の取引を設定した個数までとします");
        item.addLore("§fどちらかが0の場合設定は無効化");

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);

        inventoryItem.setEvent(e -> {
            if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //confirmation menu
            moveToMenu(player, new PerMinuteCoolDownSelectorMenu(player, shop, plugin));

        });


        return inventoryItem;
    }

    public SInventoryItem setDeleteShopItem(){
        SItemStack item = new SItemStack(Material.LAVA_BUCKET).setDisplayName(new SStringBuilder().yellow().obfuscated().text("OO")
                .darkRed().bold().text("ショップを削除")
                .yellow().obfuscated().text("OO")
                .build());

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setAsyncEvent(e -> {
            if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.OWNER)){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //confirmation menu
            ConfirmationMenu menu = new ConfirmationMenu("確認", plugin);
            menu.setOnCancel(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));
            menu.setOnConfirm(ee -> {
                //delete shop
                shop.deleteShop();
                plugin.getServer().getScheduler().runTask(plugin, () -> plugin.api.destroyAllSigns(shop));
                Man10ShopV2API.log(shop.shopId, "deleteShop", null, player.getName(), player.getUniqueId()); //log
                menu.close(player);
            });

            moveToMenu(player, menu);
        });

        return inventoryItem;
    }
}
