package com.shojabon.man10shopv2.DataClass;

import com.shojabon.man10shopv2.DataClass.ShopFunctions.*;
import com.shojabon.man10shopv2.DataClass.ShopFunctions.allowedToUse.AllowedPermissionFunction;
import com.shojabon.man10shopv2.DataClass.ShopFunctions.allowedToUse.DisabledFromFunction;
import com.shojabon.man10shopv2.DataClass.ShopFunctions.allowedToUse.EnabledFromFunction;
import com.shojabon.man10shopv2.DataClass.ShopFunctions.allowedToUse.WeekDayToggleFunction;
import com.shojabon.man10shopv2.DataClass.ShopFunctions.barter.SetBarterFunction;
import com.shojabon.man10shopv2.DataClass.ShopFunctions.general.*;
import com.shojabon.man10shopv2.DataClass.ShopFunctions.storage.StorageCapFunction;
import com.shojabon.man10shopv2.DataClass.ShopFunctions.storage.StorageFunction;
import com.shojabon.man10shopv2.DataClass.ShopFunctions.storage.StorageRefillFunction;
import com.shojabon.man10shopv2.DataClass.ShopFunctions.tradeAmount.CoolDownFunction;
import com.shojabon.man10shopv2.DataClass.ShopFunctions.tradeAmount.PerMinuteCoolDownFunction;
import com.shojabon.man10shopv2.DataClass.ShopFunctions.tradeAmount.SingleTransactionModeFunction;
import com.shojabon.man10shopv2.DataClass.ShopFunctions.tradeAmount.TotalPerMinuteCoolDownFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.mcutils.Utils.MySQL.MySQLCachedResultSet;
import com.shojabon.mcutils.Utils.SItemStack;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Man10Shop {

    public UUID shopId;

    public boolean admin = false;

    public HashMap<String, Man10ShopSign> signs = new HashMap<>();

    //functions

    public ArrayList<ShopFunction> functions = new ArrayList<>();

    //allowed to use shop settings
    public AllowedPermissionFunction allowedPermission;
    public EnabledFromFunction enabledFrom;
    public DisabledFromFunction disabledFrom;
    public WeekDayToggleFunction weekDayToggle;

    //general
    public SetPriceFunction price;
    public DeleteShopFunction deleteShop;
    public NameFunction name;
    public ShopTypeFunction shopType;
    public ShopEnabledFunction shopEnabled;
    public TargetItemFunction targetItem;
    public RandomPriceFunction randomPrice;
    public SecretPriceModeFunction secretPrice;

    //storage
    public StorageRefillFunction storageRefill;
    public StorageCapFunction storageCap;
    public StorageFunction storage;

    //tradeAmount
    public CoolDownFunction coolDown;
    public PerMinuteCoolDownFunction perMinuteCoolDown;
    public SingleTransactionModeFunction singleTransactionMode;
    public TotalPerMinuteCoolDownFunction totalPerMinuteCoolDown;

    //barter
    public SetBarterFunction setBarter;

    public PermissionFunction permission;
    public MoneyFunction money;
    public boolean currentlyEditingStorage = false;

    public Man10Shop(UUID shopId,
                     String name,
                     int itemCount,
                     int price,
                     int money,
                     SItemStack targetItem,
                     Man10ShopType shopType,
                     boolean admin){

        this.shopId = shopId;
        this.admin = admin;

        loadSigns();

        //load functions

        this.shopType = new ShopTypeFunction(this);
        this.shopType.shopType = shopType;
        functions.add(this.shopType);


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

        this.money = new MoneyFunction(this);
        this.money.money = money;
        functions.add(this.money);

        totalPerMinuteCoolDown = new TotalPerMinuteCoolDownFunction(this);
        functions.add(totalPerMinuteCoolDown);

        storageRefill = new StorageRefillFunction(this);
        functions.add(storageRefill);

        deleteShop = new DeleteShopFunction(this);
        functions.add(deleteShop);

        enabledFrom = new EnabledFromFunction(this);
        functions.add(enabledFrom);

        disabledFrom = new DisabledFromFunction(this);
        functions.add(disabledFrom);

        this.targetItem = new TargetItemFunction(this);
        this.targetItem.targetItem = targetItem;
        if(this.targetItem.getTargetItem() == null){
            this.targetItem.targetItem = new SItemStack(Material.DIAMOND);
        }
        functions.add(this.targetItem);

        setBarter = new SetBarterFunction(this);
        functions.add(setBarter);

        randomPrice = new RandomPriceFunction(this);
        functions.add(randomPrice);

        secretPrice = new SecretPriceModeFunction(this);
        functions.add(secretPrice);

        //async timer task
    }

    //base gets
    public boolean isAdminShop(){
        return admin;
    }

    public UUID getShopId(){
        return shopId;
    }


    public void perMinuteExecuteTask(){
        for(ShopFunction func: functions){
            if(!func.isFunctionEnabled()) continue;
            if(func.enabledShopTypes().length != 0 && !ArrayUtils.contains(func.enabledShopTypes(), shopType.getShopType())) continue;
            func.perMinuteExecuteTask();
        }
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
            if(func.enabledShopTypes().length != 0 && !ArrayUtils.contains(func.enabledShopTypes(), shopType.getShopType())) continue;
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
            if(func.enabledShopTypes().length != 0 && !ArrayUtils.contains(func.enabledShopTypes(), shopType.getShopType())) continue;
            if(!func.isAllowedToUseShopWithAmount(p, amount)){
                return;
            }
        }

        for(ShopFunction func: functions){
            if(!func.isFunctionEnabled()) continue;
            if(func.enabledShopTypes().length != 0 && !ArrayUtils.contains(func.enabledShopTypes(), shopType.getShopType())) continue;
            if(!func.performAction(p, amount)){
                //operation failed
                return;
            }
        }

        for(ShopFunction func: functions){
            if(!func.isFunctionEnabled()) continue;
            if(func.enabledShopTypes().length != 0 && !ArrayUtils.contains(func.enabledShopTypes(), shopType.getShopType())) continue;
            func.afterPerformAction(p, amount);
        }



        if(shopType.getShopType() == Man10ShopType.BUY){
            //remove items from shop storage
            int totalPrice = price.getPrice()*amount;
            Man10ShopV2API.tradeLog(shopId,"BUY", amount , totalPrice, p.getName(), p.getUniqueId()); //log
            p.sendMessage(Man10ShopV2.prefix + "§a§l" + targetItem.getTargetItem().getDisplayName() + "§a§lを" + amount + "個購入しました");

        }else if(shopType.getShopType() == Man10ShopType.SELL){
            int totalPrice = price.getPrice()*amount;
            Man10ShopV2API.tradeLog(shopId,"SELL", amount , totalPrice, p.getName(), p.getUniqueId()); //log
            p.sendMessage(Man10ShopV2.prefix + "§a§l" + targetItem.getTargetItem().getDisplayName() + "§a§lを" + amount + "個売却しました");
        }else if(shopType.getShopType() == Man10ShopType.BARTER) {
            Man10ShopV2API.tradeLog(shopId,"BARTER", amount , 0, p.getName(), p.getUniqueId()); //log
            p.sendMessage(Man10ShopV2.prefix + "§a§l" + new SItemStack(setBarter.getResultItems()[0]).getDisplayName() + "§a§lにトレードしました");
        }else if(shopType.getShopType() == Man10ShopType.STOPPED){
            p.sendMessage(Man10ShopV2.prefix + "§a§lこのショップは現在取引を停止しています");
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
