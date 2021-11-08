package com.shojabon.man10shopv2.DataClass.ShopFunctions.storage;

import ToolMenu.ConfirmationMenu;
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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.UUID;

public class StorageFunction extends ShopFunction {
    //variables
    public int storageSize;
    public int itemCount;

    //init
    public StorageFunction(Man10Shop shop) {
        super(shop);
        storageSize = calculateCurrentStorageSize(0);
    }

    //storage space functions

    public int calculateCurrentStorageSize(int addedUnits){
        int boughtCurrentStorageCount = getBoughtStorageUnits();
        int defaultUnit = Man10ShopV2.config.getInt("itemStorage.unitDefinition");
        if(defaultUnit == 0) defaultUnit = 1;
        return (addedUnits+boughtCurrentStorageCount)*defaultUnit;
    }

    public int calculateNextUnitPrice(int addUnits){
        int totalPrice = 0;

        MemorySection section = ((MemorySection)Man10ShopV2.config.get("itemStorage.prices"));
        if(section == null) return -1;

        int nextUnit = getBoughtStorageUnits();

        if(nextUnit + 1 > Man10ShopV2.config.getInt("itemStorage.maxStorageUnits")) return -1;

        for(int i = 0; i < addUnits; i++){
            nextUnit += 1;
            if(nextUnit > Man10ShopV2.config.getInt("itemStorage.maxStorageUnits")) continue;

            String currentRangeKey = null;
            for(String key : section.getKeys(false)){
                if(!BaseUtils.isInt(key)) continue;
                if(Integer.parseInt(key) <= nextUnit){
                    currentRangeKey = key;
                }else{
                    break;
                }
            }
            if(currentRangeKey == null){
                totalPrice += 0;
                continue;
            }
            totalPrice += section.getInt(currentRangeKey);
        }

        return totalPrice;
    }

    public boolean buyStorageSpace(Player p, int units){
        int boughtCurrentStorageCount = getBoughtStorageUnits();
        if(boughtCurrentStorageCount+units > Man10ShopV2.config.getInt("itemStorage.maxStorageUnits")){
            p.sendMessage(Man10ShopV2.prefix + "§c§l倉庫ユニットの上限を超えました");
            return false;
        }
        if(Man10ShopV2.vault.getBalance(p.getUniqueId()) < calculateNextUnitPrice(units)){
            p.sendMessage(Man10ShopV2.prefix + "§c§lお金が足りません");
            return false;
        }
        Man10ShopV2.vault.withdraw(p.getUniqueId(), calculateNextUnitPrice(units));
        Man10ShopV2API.log(shop.getShopId(), "buyStorageSpace", units, p.getName(), p.getUniqueId()); //log
        p.sendMessage(Man10ShopV2.prefix + "§a§l倉庫スペースを購入しました");
        storageSize = calculateCurrentStorageSize(units);
        return buyStorageSpace(units);
    }

    public boolean buyStorageSpace(int units){
        return setBoughtStorageUnits(getBoughtStorageUnits()+units);

    }

    public int getStorageSize(){
        return storageSize;
    }

    //====================
    // settings
    //====================

    public int getBoughtStorageUnits(){
        String currentSetting = getSetting("storage.bought");
        if(!BaseUtils.isInt(currentSetting)) return Man10ShopV2.config.getInt("itemStorage.defaultUnits");
        return Integer.parseInt(currentSetting);
    }

    public boolean setBoughtStorageUnits(int units){
        if(getBoughtStorageUnits() == units) return true;
        boolean result = setSetting("storage.bought", units);
        if(result)storageSize = calculateCurrentStorageSize(0);
        return result;
    }

    //item count functions

    public boolean removeItemCount(int count){
        itemCount = itemCount - count;
        boolean result = Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET item_count = item_count - " + count + " WHERE shop_id = '" + shop.getShopId() + "'");
        if(!result) return false;
        return true;
    }

    public boolean addItemCount(int count){
        itemCount = itemCount + count;
        boolean result = Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET item_count = item_count + " + count + " WHERE shop_id = '" + shop.getShopId() + "'");
        if(!result) return false;
        //log here
        return true;
    }

    public boolean setItemCount(int count){
        itemCount = count;
        boolean result = Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET item_count = " + count + " WHERE shop_id = '" + shop.getShopId() + "'");
        if(!result) return false;
        //log here
        return true;
    }

    public int getItemCount(){
        if(itemCount < 0) return 0;
        return itemCount;
    }

    //allowed

    @Override
    public Man10ShopType[] enabledShopTypes() {
        return new Man10ShopType[]{Man10ShopType.BUY, Man10ShopType.SELL};
    }

    @Override
    public String settingCategory() {
        return "倉庫設定";
    }

    @Override
    public boolean hasPermissionToEdit(UUID uuid) {
        if(!shop.permission.hasPermissionAtLeast(uuid, Man10ShopPermission.STORAGE_ACCESS)) return false;
        if(shop.permission.hasPermission(uuid, Man10ShopPermission.ACCOUNTANT)) return false;
        return true;
    }

    @Override
    public int itemCount(Player p) {
        if(shop.isAdminShop()) return 0;
        if(shop.shopType.getShopType() == Man10ShopType.BUY){
            return getItemCount();
        }
        if(shop.shopType.getShopType() == Man10ShopType.SELL){
            return getStorageSize() - getItemCount();
        }
        return getStorageSize();
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {

        //sell shop check
        if(shop.shopType.getShopType() == Man10ShopType.SELL){
            if(itemCount >= calculateCurrentStorageSize(0) && !shop.isAdminShop()){
                p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップは在庫がいっぱいです");
                return false;
            }
        }

        //buy shop check
        if(shop.shopType.getShopType() == Man10ShopType.BUY){
            if(itemCount <= 0 && !shop.isAdminShop()){
                p.sendMessage(Man10ShopV2.prefix + "§c§l在庫がありません");
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isAllowedToUseShopWithAmount(Player p, int amount) {
        if(shop.shopType.getShopType() == Man10ShopType.BUY){
            if(amount > itemCount && !shop.isAdminShop()){
                p.sendMessage(Man10ShopV2.prefix + "§c§l在庫が不足しています");
                return false;
            }
        }
        if(shop.shopType.getShopType() == Man10ShopType.SELL){
            if(itemCount + amount > calculateCurrentStorageSize(0) && !shop.isAdminShop()){
                p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップは現在買取を行っていません");
                return false;
            }
        }
        return true;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.CHEST).setDisplayName(new SStringBuilder().gray().text("ショップの倉庫を拡張する").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の倉庫サイズ: ").yellow().text(storageSize).text("個").build());
        item.addLore("");

        int unitsTillMax = Man10ShopV2.config.getInt("itemStorage.maxStorageUnits") - getBoughtStorageUnits();

        if(calculateNextUnitPrice(1) != -1){
            item.addLore(new SStringBuilder().red().text("次のサイズ: ").text(calculateCurrentStorageSize(1)).text("個").build());
            item.addLore(new SStringBuilder().yellow().text("価格: ").text(BaseUtils.priceString(calculateNextUnitPrice(1))).text("円").build());
            item.addLore(new SStringBuilder().white().bold().text("左クリックで購入").build());
            item.addLore(new SStringBuilder().white().bold().text("左シフトクリックで最大まで買う").yellow().text("価格:")
                    .text(BaseUtils.priceString(calculateNextUnitPrice(unitsTillMax))).text("円").build());
        }
        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setAsyncEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            int buyingUnits = 1;

            if(e.getClick() == ClickType.SHIFT_LEFT) buyingUnits = unitsTillMax;


            //confirmation menu
            ConfirmationMenu menu = new ConfirmationMenu("確認", plugin);
            menu.setOnClose(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin)));
            menu.setOnCancel(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin)));


            int finalBuyingUnits = buyingUnits;
            menu.setOnConfirm(ee -> {
                if(buyStorageSpace(player, finalBuyingUnits)){
                    Man10ShopV2API.log(shop.getShopId(), "buyStorageSpace", 1, player.getName(), player.getUniqueId()); //log
                }
                menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin));
            });

            sInventory.moveToMenu(player, menu);

        });


        return inventoryItem;
    }
}
