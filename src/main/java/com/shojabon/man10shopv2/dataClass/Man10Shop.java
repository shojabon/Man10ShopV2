package com.shojabon.man10shopv2.dataClass;

import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.shopFunctions.*;
import com.shojabon.man10shopv2.dataClass.shopFunctions.agent.SetItemCountFunction;
import com.shojabon.man10shopv2.dataClass.shopFunctions.agent.SetStorageSizeFunction;
import com.shojabon.man10shopv2.dataClass.shopFunctions.allowedToUse.AllowedPermissionFunction;
import com.shojabon.man10shopv2.dataClass.shopFunctions.allowedToUse.DisabledFromFunction;
import com.shojabon.man10shopv2.dataClass.shopFunctions.allowedToUse.EnabledFromFunction;
import com.shojabon.man10shopv2.dataClass.shopFunctions.allowedToUse.WeekDayToggleFunction;
import com.shojabon.man10shopv2.dataClass.shopFunctions.barter.SetBarterFunction;
import com.shojabon.man10shopv2.dataClass.shopFunctions.general.*;
import com.shojabon.man10shopv2.dataClass.shopFunctions.lootBox.LootBoxBigWinFunction;
import com.shojabon.man10shopv2.dataClass.shopFunctions.lootBox.LootBoxGroupFunction;
import com.shojabon.man10shopv2.dataClass.shopFunctions.lootBox.LootBoxPaymentFunction;
import com.shojabon.man10shopv2.dataClass.shopFunctions.mQuest.MQuestGroupFunction;
import com.shojabon.man10shopv2.dataClass.shopFunctions.mQuest.MQuestQuestCountFunction;
import com.shojabon.man10shopv2.dataClass.shopFunctions.mQuest.MQuestRefreshQuest;
import com.shojabon.man10shopv2.dataClass.shopFunctions.mQuest.MQuestTimeWindowFunction;
import com.shojabon.man10shopv2.dataClass.shopFunctions.storage.StorageCapFunction;
import com.shojabon.man10shopv2.dataClass.shopFunctions.storage.StorageFunction;
import com.shojabon.man10shopv2.dataClass.shopFunctions.storage.StorageRefillFunction;
import com.shojabon.man10shopv2.dataClass.shopFunctions.tradeAmount.CoolDownFunction;
import com.shojabon.man10shopv2.dataClass.shopFunctions.tradeAmount.PerMinuteCoolDownFunction;
import com.shojabon.man10shopv2.dataClass.shopFunctions.tradeAmount.SingleTransactionModeFunction;
import com.shojabon.man10shopv2.dataClass.shopFunctions.tradeAmount.TotalPerMinuteCoolDownFunction;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.menus.action.*;
import com.shojabon.mcutils.Utils.MySQL.MySQLCachedResultSet;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SItemStack;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class Man10Shop {

    private boolean shopSafety = false;

    public Man10ShopV2 plugin = (Man10ShopV2) Bukkit.getPluginManager().getPlugin("Man10ShopV2");
    public UUID shopId;

    public boolean admin = false;

    public HashMap<String, Man10ShopSign> signs = new HashMap<>();
    public static HashMap<Man10ShopSetting<?>, Type> settingTypeMap = new HashMap<>();

    //functions

    public ArrayList<ShopFunction> functions = new ArrayList<>();
    public ArrayList<LootBoxFunction> lootBoxFunctions = new ArrayList<>();

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
    public CategoryFunction categoryFunction;
    public IpLimitFunction ipLimitFunction;

    //storage
    public StorageRefillFunction storageRefill;
    public StorageCapFunction storageCap;
    public StorageFunction storage;

    //agent
    public SetStorageSizeFunction setStorageSizeFunction;
    public SetItemCountFunction setItemCountFunction;

    //tradeAmount
    public CoolDownFunction coolDown;
    public PerMinuteCoolDownFunction perMinuteCoolDown;
    public SingleTransactionModeFunction singleTransactionMode;
    public TotalPerMinuteCoolDownFunction totalPerMinuteCoolDown;

    //barter
    public SetBarterFunction setBarter;

    //loot Box
    public LootBoxGroupFunction lootBoxFunction;
    //public LootBoxSpinTimeFunction lootBoxSpinTimeFunction;
    public LootBoxPaymentFunction lootBoxPaymentFunction;
    public LootBoxBigWinFunction lootBoxBigWinFunction;

    //mquest
    public MQuestGroupFunction mQuestFunction;
    public MQuestTimeWindowFunction mQuestTimeWindowFunction;
    public MQuestQuestCountFunction mQuestCountFunction;
    public MQuestRefreshQuest mQuestForceRefreshButton;

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
        for(Field field: getClass().getFields()){
            try{
                if(ShopFunction.class.isAssignableFrom(field.getType())){
                    field.set(this, field.getType().getConstructor(Man10Shop.class, Man10ShopV2.class).newInstance(this, plugin));

                    //set shop id in setting fields
                    ShopFunction func = (ShopFunction) field.get(this);
                    for(Field innerField: func.getClass().getFields()){
                        if(Man10ShopSetting.class.isAssignableFrom(innerField.getType())) {
                            Man10ShopSetting setting = ((Man10ShopSetting) innerField.get(func));
                            settingTypeMap.put(setting, ((ParameterizedType) innerField.getGenericType()).getActualTypeArguments()[0]);
                            setting.shopId = shopId;
                            //setting.typeMap = settingTypeMap;
                        }
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
                e.printStackTrace();
                shopSafety = true;
            }
        }

        this.shopType.shopType = shopType;
        this.price.price = price;
        storage.itemCount = itemCount;
        this.name.name = name;
        this.money.money = money;
        this.targetItem.targetItem = targetItem;


        try {
            for(Field field: getClass().getFields()){
                if(ShopFunction.class.isAssignableFrom(field.getType())) {
                    ShopFunction func = (ShopFunction) field.get(this);
                    func.init();
                    functions.add(func);
                }
                if(LootBoxFunction.class.isAssignableFrom(field.getType())) {
                    lootBoxFunctions.add((LootBoxFunction) field.get(this));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


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
            if(!func.getClass().isAnnotationPresent(ShopFunctionDefinition.class)) continue;
            if(!func.isFunctionEnabled()) continue;
            if(func.getDefinition().enabledShopType().length != 0 && !ArrayUtils.contains(func.getDefinition().enabledShopType(), shopType.getShopType())) continue;
            func.perMinuteExecuteTask();
        }
    }

    public boolean allowedToUseShop(Player p){
        if(shopSafety) {
            p.sendMessage(Man10ShopV2.prefix + "§c§lエラーにより停止中です");
            return false;
        }
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
            if(!func.getClass().isAnnotationPresent(ShopFunctionDefinition.class)) continue;
            if(!func.isFunctionEnabled()) continue;
            if(func.getDefinition().enabledShopType().length != 0 && !ArrayUtils.contains(func.getDefinition().enabledShopType(), shopType.getShopType())) continue;
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
            if(!func.getClass().isAnnotationPresent(ShopFunctionDefinition.class)) continue;
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
            if(!func.getClass().isAnnotationPresent(ShopFunctionDefinition.class)) continue;
            if(!func.isFunctionEnabled()) continue;
            if(func.getDefinition().enabledShopType().length != 0 && !ArrayUtils.contains(func.getDefinition().enabledShopType(), shopType.getShopType())) continue;
            if(!func.isAllowedToUseShopWithAmount(p, amount)){
                return;
            }
        }

        for(ShopFunction func: functions){
            if(!func.getClass().isAnnotationPresent(ShopFunctionDefinition.class)) continue;
            if(!func.isFunctionEnabled()) continue;
            if(func.getDefinition().enabledShopType().length != 0 && !ArrayUtils.contains(func.getDefinition().enabledShopType(), shopType.getShopType())) continue;
            if(!func.performAction(p, amount)){
                //operation failed
                return;
            }
        }

        for(ShopFunction func: functions){
            if(!func.getClass().isAnnotationPresent(ShopFunctionDefinition.class)) continue;
            if(!func.isFunctionEnabled()) continue;
            if(func.getDefinition().enabledShopType().length != 0 && !ArrayUtils.contains(func.getDefinition().enabledShopType(), shopType.getShopType())) continue;
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
            Man10ShopV2API.tradeLog(shopId, "BARTER", amount, 0, p.getName(), p.getUniqueId()); //log
            p.sendMessage(Man10ShopV2.prefix + "§a§l" + new SItemStack(setBarter.resultItems.get().get(0)).getDisplayName() + "§a§lにトレードしました");
        }else if(shopType.getShopType() == Man10ShopType.LOOT_BOX){
            Man10ShopV2API.tradeLog(shopId, "LOOTBOX", 1, lootBoxPaymentFunction.balancePrice.get(), p.getName(), p.getUniqueId()); //log
            Bukkit.getScheduler().runTask(plugin, ()->{
                LootBoxPlayMenu menu = new LootBoxPlayMenu(p, this, (Man10ShopV2) plugin);
                menu.open(p);
            });
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

    public SInventory getActionMenu(Player p){
        if(shopType.getShopType() == Man10ShopType.BUY || shopType.getShopType() == Man10ShopType.SELL){
            return new BuySellActionMenu(p, this, (JavaPlugin) plugin);
        }

        if(shopType.getShopType() == Man10ShopType.BARTER){
            return new BarterActionMenu(p, this, (Man10ShopV2) plugin);
        }

        if(shopType.getShopType() == Man10ShopType.LOOT_BOX){
            return new LootBoxActionMenu(p, this, (Man10ShopV2) plugin);
        }

        if(shopType.getShopType() == Man10ShopType.QUEST){
            return new QuestActionMenu(p, this, (Man10ShopV2) plugin);
        }
        return null;
    }

    public void openActionMenu(Player p){
        Bukkit.getScheduler().runTask(plugin, ()->{getActionMenu(p).open(p);});
    }


}
