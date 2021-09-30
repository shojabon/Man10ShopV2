package com.shojabon.man10shopv2.DataClass;

import com.shojabon.man10shopv2.DataClass.ShopFunctions.*;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Utils.BaseUtils;
import com.shojabon.man10shopv2.Utils.MySQL.MySQLAPI;
import com.shojabon.man10shopv2.Utils.MySQL.MySQLCachedResultSet;
import com.shojabon.man10shopv2.Utils.SItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.A;

import java.util.*;

public class Man10Shop {

    public String name;
    public UUID shopId;
    public int price;

    public boolean admin = false;

    public int money;

    public SItemStack targetItem;
    public int targetItemCount;
    public ItemStack icon;
    public Man10ShopType shopType;

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
        this.money = money;
        this.price = price;
        this.shopId = shopId;
        this.name = name;
        this.targetItem = targetItem;
        this.targetItemCount = targetItemCount;
        this.icon = new ItemStack(targetItem.getType());
        this.shopType = shopType;
        this.admin = admin;

        loadSigns();

        //load functions
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
    }


    //money storage

    public boolean addMoney(int value){
        money = money + value;
        boolean result = Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET money = money + " + value + " WHERE shop_id = '" + shopId + "'");
        if(!result) return false;
        //log here
        return true;
    }

    public boolean removeMoney(int value){
        money = money - value;
        boolean result = Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET money = money - " + value + " WHERE shop_id = '" + shopId + "'");
        if(!result) return false;
        //log here
        return true;
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


    //shop type
    public boolean setShopType(Man10ShopType type){
        shopType = type;
        if(!Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET shop_type ='" + type.name() + "' WHERE shop_id = '" + shopId + "'")){
            return false;
        }
        Man10ShopV2API.closeInventoryGroup(shopId);
        return true;
    }

    //price

    public boolean setPrice(int value){
        if(value < 0) return false;
        price = value;
        Man10ShopV2API.closeInventoryGroup(shopId);
        return Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET price = " + value + " WHERE shop_id = '" + shopId + "'");
    }

    //base gets
    public boolean isAdminShop(){
        return admin;
    }

    public UUID getShopId(){
        return shopId;
    }

    public String getShopName(){
        return name;
    }

    public Man10ShopType getShopType(){
        return shopType;
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

        if(shopType == Man10ShopType.BUY){
        }else{
            //no money (sell)
            if(money < price && !admin){
                p.sendMessage(Man10ShopV2.prefix + "§c§lショップの残高が不足しています");
                return false;
            }

        }
        //all function check
        for(ShopFunction func: functions){
            if(!func.isAllowedToUseShop(p)){
                return false;
            }
        }
        return true;
    }

    public void performAction(Player p, int amount){

        if(!allowedToUseShop(p)) return;

        if(shopType == Man10ShopType.BUY){
            int totalPrice = price*amount;
            if(Man10ShopV2.vault.getBalance(p.getUniqueId()) < totalPrice){
              p.sendMessage(Man10ShopV2.prefix + "§c§l残高が不足しています");
              return;
            }
            if(!Man10ShopV2.vault.withdraw(p.getUniqueId(), totalPrice)){
                p.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return;
            }
            addMoney(totalPrice);
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
            perMinuteCoolDown.addPerMinuteCoolDownLog(p.getUniqueId(), new Man10ShopLogObject(System.currentTimeMillis() / 1000L, amount));

            p.sendMessage(Man10ShopV2.prefix + "§a§l" + item.getDisplayName() + "§a§lを" + amount*item.getAmount() + "個購入しました");
            permission.notifyModerators(amount*item.getAmount());
            coolDown.setCoolDown(p); //set coolDown

        }else if(shopType == Man10ShopType.SELL){
            SItemStack item = new SItemStack(targetItem.build().clone());
            if(!p.getInventory().containsAtLeast(targetItem.build(), amount*item.getAmount())){
                p.sendMessage(Man10ShopV2.prefix + "§c§l買い取るためのアイテムを持っていません");
                return;
            }
            int totalPrice = price*amount;
            if(totalPrice > money && !admin){
                p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップの現金が不足しています");
                return;
            }
            if(!removeMoney(totalPrice)){
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
            perMinuteCoolDown.addPerMinuteCoolDownLog(p.getUniqueId(), new Man10ShopLogObject(System.currentTimeMillis() / 1000L, amount));

            p.sendMessage(Man10ShopV2.prefix + "§a§l" + item.getDisplayName() + "§a§lを" + amount*item.getAmount() + "個売却しました");
            permission.notifyModerators(amount*item.getAmount());
            coolDown.setCoolDown(p); //set coolDown


        }else if(shopType == Man10ShopType.STOPPED){
            p.sendMessage(Man10ShopV2.prefix + "§a§lこのショップは現在取引を停止しています");
        }
    }

    public void deleteShop(){
        Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET `deleted` = 1 WHERE shop_id = '" + shopId + "'");
        Man10ShopV2API.shopCache.remove(shopId);
        Man10ShopV2API.closeInventoryGroup(shopId);
    }

    //name

    public boolean setName(String name){
        if(name.length() > 64 || name.length() == 0) return false;
        this.name = name;
        return Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET name = '" + MySQLAPI.escapeString(name) + "' WHERE shop_id = '" + shopId + "'");
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
