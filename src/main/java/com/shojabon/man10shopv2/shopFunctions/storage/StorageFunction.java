package com.shojabon.man10shopv2.shopFunctions.storage;

import ToolMenu.ConfirmationMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

@ShopFunctionDefinition(
        name = "ストレージを購入する",
        explanation = {},
        enabledShopType = {Man10ShopType.BUY, Man10ShopType.SELL},
        iconMaterial = Material.CHEST,
        category = "倉庫設定",
        allowedPermission = Man10ShopPermission.STORAGE_ACCESS,
        isAdminSetting = false
)
public class StorageFunction extends ShopFunction {
    //variables
    public Man10ShopSetting<Integer> boughtStorageUnits = new Man10ShopSetting<>("storage.bought", Man10ShopV2.config.getInt("itemStorage.defaultUnits"));

    public int storageSize;
    public int itemCount;

    //init
    public StorageFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }

    @Override
    public void init() {
        storageSize = calculateCurrentStorageSize(0);
    }

    //storage space functions

    public int calculateCurrentStorageSize(int addedUnits){
        int boughtCurrentStorageCount = boughtStorageUnits.get();
        int defaultUnit = Man10ShopV2.config.getInt("itemStorage.unitDefinition");
        if(defaultUnit == 0) defaultUnit = 1;
        return (addedUnits+boughtCurrentStorageCount)*defaultUnit;
    }

    public int calculateNextUnitPrice(int addUnits){
        int totalPrice = 0;

        MemorySection section = ((MemorySection)Man10ShopV2.config.get("itemStorage.prices"));
        if(section == null) return -1;

        int nextUnit = boughtStorageUnits.get();

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
        int boughtCurrentStorageCount = boughtStorageUnits.get();
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
        return boughtStorageUnits.set(boughtStorageUnits.get()+units);

    }

    public int getStorageSize(){
        return storageSize;
    }

    //====================
    // settings
    //====================

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
    public String currentSettingString() {
        String result  = storageSize + "個\n";
        if(calculateNextUnitPrice(1) != -1){
            int unitsTillMax = Man10ShopV2.config.getInt("itemStorage.maxStorageUnits") - boughtStorageUnits.get();
            result += new SStringBuilder().red().text("次のサイズ: ").text(calculateCurrentStorageSize(1)).text("個").build() + "\n";
            result += new SStringBuilder().yellow().text("価格: ").text(BaseUtils.priceString(calculateNextUnitPrice(1))).text("円").build() + "\n";
            result += new SStringBuilder().white().bold().text("左クリックで購入").build() + "\n";
            result += new SStringBuilder().white().bold().text("左シフトクリックで最大まで買う").yellow().text("価格:")
                    .text(BaseUtils.priceString(calculateNextUnitPrice(unitsTillMax))).text("円").build() + "\n";
        }
        return result;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        int unitsTillMax = Man10ShopV2.config.getInt("itemStorage.maxStorageUnits") - boughtStorageUnits.get();

        item.setAsyncEvent(e -> {
            int buyingUnits = 1;

            if(e.getClick() == ClickType.SHIFT_LEFT) buyingUnits = unitsTillMax;


            //confirmation menu
            ConfirmationMenu menu = new ConfirmationMenu("確認", plugin);
            menu.setOnClose(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnCancel(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));


            int finalBuyingUnits = buyingUnits;
            menu.setOnConfirm(ee -> {
                if(!buyStorageSpace(player, finalBuyingUnits)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
            });
            menu.open(player);

        });


        return item;
    }
}
