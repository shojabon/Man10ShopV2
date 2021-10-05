package com.shojabon.man10shopv2.DataClass;

import com.shojabon.man10shopv2.DataClass.ShopFunctions.*;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.StorageRefillMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.MySQL.MySQLAPI;
import com.shojabon.mcutils.Utils.MySQL.MySQLCachedResultSet;
import com.shojabon.mcutils.Utils.SItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.A;

import javax.naming.Name;
import java.util.*;

public class Man10Shop {

    public UUID shopId;

    public boolean admin = false;

    public SItemStack targetItem;
    public int targetItemCount;
    public ItemStack icon;

    public HashMap<String, Man10ShopSign> signs = new HashMap<>();

    //functions

    public ArrayList<ShopFunction> functions = new ArrayList<>();
    public PermissionFunction permission;
    public StorageFunction storage;
    public CoolDownFunction coolDown;
    public PerMinuteCoolDownFunction perMinuteCoolDown;
    public WeekDayToggleFunction weekDayToggle;
    public AllowedPermissionFunction allowedPermission;
    public SingleTransactionModeFunction singleTransactionMode;
    public ShopEnabledFunction shopEnabled;
    public StorageCapFunction storageCap;
    public NameFunction name;
    public ShopTypeFunction shopType;
    public MoneyFunction money;
    public TotalPerMinuteCoolDownFunction totalPerMinuteCoolDown;
    public StorageRefillFunction storageRefill;
    public SetPriceFunction price;
    public DeleteShopFunction deleteShop;

    public boolean currentlyEditingStorage = false;

    public Man10Shop(UUID shopId,
                     String name,
                     int itemCount,
                     int price,
                     int money,
                     SItemStack targetItem,
                     int targetItemCount,
                     Man10ShopType shopType,
                     boolean admin){

        if(targetItem == null){
            targetItem = new SItemStack(Material.DIAMOND);
        }
        this.shopId = shopId;
        this.targetItem = targetItem;
        this.targetItemCount = targetItemCount;
        this.icon = new ItemStack(targetItem.getType());
        this.admin = admin;

        loadSigns();

        //load functions
        this.price = new SetPriceFunction(this);
        this.price.price = price;
        functions.add(this.price);

        permission = new PermissionFunction(this);
        functions.add(permission);

        storage = new StorageFunction(this);
        storage.itemCount = itemCount;
        functions.add(storage);

        coolDown = new CoolDownFunction(this);
        functions.add(coolDown);

        perMinuteCoolDown = new PerMinuteCoolDownFunction(this);
        functions.add(perMinuteCoolDown);

        weekDayToggle = new WeekDayToggleFunction(this);
        functions.add(weekDayToggle);

        allowedPermission = new AllowedPermissionFunction(this);
        functions.add(allowedPermission);

        singleTransactionMode = new SingleTransactionModeFunction(this);
        functions.add(singleTransactionMode);

        shopEnabled = new ShopEnabledFunction(this);
        functions.add(shopEnabled);

        storageCap = new StorageCapFunction(this);
        functions.add(storageCap);

        this.name = new NameFunction(this);
        this.name.name = name;
        functions.add(this.name);

        this.shopType = new ShopTypeFunction(this);
        this.shopType.shopType = shopType;
        functions.add(this.shopType);

        this.money = new MoneyFunction(this);
        this.money.money = money;
        functions.add(this.money);

        totalPerMinuteCoolDown = new TotalPerMinuteCoolDownFunction(this);
        functions.add(totalPerMinuteCoolDown);

        storageRefill = new StorageRefillFunction(this);
        functions.add(storageRefill);

        deleteShop = new DeleteShopFunction(this);
        functions.add(deleteShop);
    }


    //itemstack setting

    public boolean setTargetItem(ItemStack item){
        SItemStack sItem = new SItemStack(item);
        targetItem = sItem;
        boolean result = Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET target_item = '" + sItem.getItemTypeBase64(true) + "', target_item_hash ='" + sItem.getItemTypeMD5(true) + "' WHERE shop_id = '" + shopId + "'");
        if(!result) return false;
        Man10ShopV2API.closeInventoryGroup(shopId);
        return true;
    }

    //base gets
    public boolean isAdminShop(){
        return admin;
    }

    public UUID getShopId(){
        return shopId;
    }


    public boolean allowedToUseShop(Player p){
        //permission to use
        if(!p.hasPermission("man10shopv2.use")){
            p.sendMessage(Man10ShopV2.prefix + "§c§lあなたには権限がありません");
            return false;
        }

        //allowed worlds
        if(!Man10ShopV2.config.getStringList("enabledWorlds").contains(p.getWorld().getName())) return false;

        //if plugin disabled
        if(!Man10ShopV2.config.getBoolean("pluginEnabled")){
            p.sendMessage(Man10ShopV2.prefix + "§c§l現在このプラグインは停止中です");
            return false;
        }

        //editing storage
        if(currentlyEditingStorage){
            p.sendMessage(Man10ShopV2.prefix + "§c§l現在店主がショップの在庫を移動させています");
            return false;
        }

        //all function check
        for(ShopFunction func: functions){
            if(!func.isFunctionEnabled()) continue;
            if(!func.isAllowedToUseShop(p)){
                return false;
            }
        }
        return true;
    }

    public int getPlayerAvailableTransactionCount(Player p){
        int amount = storage.getStorageSize(); //if sell
        if(shopType.getShopType() == Man10ShopType.BUY) amount = storage.getItemCount(); //if buy

        //all function check
        for(ShopFunction func: functions){
            if(!func.isFunctionEnabled()) continue;
            int funcItemCount = func.itemCount(p);
            if(funcItemCount < amount) amount = funcItemCount;
        }
        if(amount < 0) amount = -amount;
        return amount;
    }

    public void performAction(Player p, int amount){

        if(!allowedToUseShop(p)) return;

        //all function check
        for(ShopFunction func: functions){
            if(!func.isFunctionEnabled()) continue;
            if(!func.isAllowedToUseShopWithAmount(p, amount)){
                return;
            }
        }

        if(shopType.getShopType() == Man10ShopType.BUY){
            int totalPrice = price.getPrice()*amount;
            if(Man10ShopV2.vault.getBalance(p.getUniqueId()) < totalPrice){
              p.sendMessage(Man10ShopV2.prefix + "§c§l残高が不足しています");
              return;
            }
            if(!Man10ShopV2.vault.withdraw(p.getUniqueId(), totalPrice)){
                p.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return;
            }
            money.addMoney(totalPrice);
            //remove items from shop storage

            SItemStack item = new SItemStack(targetItem.build().clone());
            boolean removeItemResult =  storage.removeItemCount(amount*item.getAmount());
            if(!removeItemResult){
                p.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return;
            }

            for(int i = 0; i < amount; i++){
                p.getInventory().addItem(item.build());
            }

            Man10ShopV2API.tradeLog(shopId,"BUY", amount*item.getAmount() , totalPrice, p.getName(), p.getUniqueId()); //log

            p.sendMessage(Man10ShopV2.prefix + "§a§l" + item.getDisplayName() + "§a§lを" + amount*item.getAmount() + "個購入しました");

        }else if(shopType.getShopType() == Man10ShopType.SELL){
            SItemStack item = new SItemStack(targetItem.build().clone());
            if(!p.getInventory().containsAtLeast(targetItem.build(), amount*item.getAmount())){
                p.sendMessage(Man10ShopV2.prefix + "§c§l買い取るためのアイテムを持っていません");
                return;
            }
            int totalPrice = price.getPrice()*amount;
            if(totalPrice > money.getMoney() && !admin){
                p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップの現金が不足しています");
                return;
            }
            if(!money.removeMoney(totalPrice)){
                p.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return;
            }
            Man10ShopV2.vault.deposit(p.getUniqueId(), totalPrice);
            //remove items from shop storage
            boolean addItemResult =  storage.addItemCount(amount);
            if(!addItemResult){
                p.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return;
            }

            //perform transaction
            for( int i =0; i < amount; i++){
                p.getInventory().removeItemAnySlot(item.build());
            }

            Man10ShopV2API.tradeLog(shopId,"SELL", amount*item.getAmount() , totalPrice, p.getName(), p.getUniqueId()); //log
            p.sendMessage(Man10ShopV2.prefix + "§a§l" + item.getDisplayName() + "§a§lを" + amount*item.getAmount() + "個売却しました");


        }else if(shopType.getShopType() == Man10ShopType.STOPPED){
            p.sendMessage(Man10ShopV2.prefix + "§a§lこのショップは現在取引を停止しています");
        }
        for(ShopFunction func: functions){
            if(!func.isFunctionEnabled()) continue;
            func.performAction(p, amount);
        }
    }

    public void deleteShop(){
        Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET `deleted` = 1 WHERE shop_id = '" + shopId + "'");
        Man10ShopV2API.shopCache.remove(shopId);
        Man10ShopV2API.closeInventoryGroup(shopId);
    }

    //signs

    public void loadSigns(){
        ArrayList<MySQLCachedResultSet> result = Man10ShopV2.mysql.query("SELECT * FROM man10shop_signs WHERE shop_id = '" + shopId + "'");
        for(MySQLCachedResultSet rs: result){
            Man10ShopSign sign = new Man10ShopSign(shopId,
                    rs.getString("world"),
                    rs.getInt("x"),
                    rs.getInt("y"),
                    rs.getInt("z"));
            signs.put(rs.getString("locationId"), sign);
            signs.put("locationId", sign);
        }
    }



}
