package com.shojabon.man10shopv2.dataClass;

import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.shopFunctions.*;
import com.shojabon.man10shopv2.shopFunctions.agent.MoneyRefillFunction;
import com.shojabon.man10shopv2.shopFunctions.agent.SetItemCountFunction;
import com.shojabon.man10shopv2.shopFunctions.agent.SetStorageSizeFunction;
import com.shojabon.man10shopv2.shopFunctions.ai.AITargetItemFunction;
import com.shojabon.man10shopv2.shopFunctions.ai.LearningRateFunction;
import com.shojabon.man10shopv2.shopFunctions.ai.PriceUnitFunction;
import com.shojabon.man10shopv2.shopFunctions.ai.SetTargetItemCountFunction;
import com.shojabon.man10shopv2.shopFunctions.allowedToUse.*;
import com.shojabon.man10shopv2.shopFunctions.barter.SetBarterFunction;
import com.shojabon.man10shopv2.shopFunctions.commandShop.CommandShopExplanationFunction;
import com.shojabon.man10shopv2.shopFunctions.commandShop.CommandShopSetCommandFunction;
import com.shojabon.man10shopv2.shopFunctions.general.*;
import com.shojabon.man10shopv2.shopFunctions.lootBox.LootBoxBigWinFunction;
import com.shojabon.man10shopv2.shopFunctions.lootBox.LootBoxGroupFunction;
import com.shojabon.man10shopv2.shopFunctions.lootBox.LootBoxPaymentFunction;
import com.shojabon.man10shopv2.shopFunctions.mQuest.*;
import com.shojabon.man10shopv2.shopFunctions.storage.StorageCapFunction;
import com.shojabon.man10shopv2.shopFunctions.storage.StorageFunction;
import com.shojabon.man10shopv2.shopFunctions.storage.StorageRefillFunction;
import com.shojabon.man10shopv2.shopFunctions.tradeAmount.CoolDownFunction;
import com.shojabon.man10shopv2.shopFunctions.tradeAmount.PerMinuteCoolDownFunction;
import com.shojabon.man10shopv2.shopFunctions.tradeAmount.SingleTransactionModeFunction;
import com.shojabon.man10shopv2.shopFunctions.tradeAmount.TotalPerMinuteCoolDownFunction;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.menus.action.*;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.MySQL.MySQLCachedResultSet;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import it.unimi.dsi.fastutil.Hash;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Man10Shop {

    private boolean shopSafety = false;

    public Man10ShopV2 plugin = (Man10ShopV2) Bukkit.getPluginManager().getPlugin("Man10ShopV2");
    public UUID shopId;

    public boolean admin = false;

    //public HashMap<String, Man10ShopSign> signs = new HashMap<>();
    public static ConcurrentHashMap<String, Type> settingTypeMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> settingValueMap = new ConcurrentHashMap<>();

    //functions

    public ArrayList<ShopFunction> functions = new ArrayList<>();
    public ArrayList<LootBoxFunction> lootBoxFunctions = new ArrayList<>();

    //allowed to use shop settings
    public AllowedPermissionFunction allowedPermission;
    public EnabledFromFunction enabledFrom;
    public DisabledFromFunction disabledFrom;
    public WeekDayToggleFunction weekDayToggle;
    public ProximityFriendshipAllowFunction proximityFriendshipAllowFunction;

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
    public LimitUseFunction limitUseFunction;

    //storage
    public StorageRefillFunction storageRefill;
    public StorageCapFunction storageCap;
    public StorageFunction storage;

    //agent
    public SetStorageSizeFunction setStorageSizeFunction;
    public SetItemCountFunction setItemCountFunction;
    public MoneyRefillFunction moneyRefillFunction;

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

    //mquestt
    public MQuestGroupFunction mQuestFunction;
    public MQuestTimeWindowFunction mQuestTimeWindowFunction;
    public MQuestQuestCountFunction mQuestCountFunction;
    public MQuestRefreshQuest mQuestForceRefreshButton;
    public MQuestDialogue mQuestDialogue;

    //command shop
    public CommandShopSetCommandFunction commandShopSetCommandFunction;
    public CommandShopExplanationFunction commandShopExplanationFunction;

    //ai
    public LearningRateFunction learningRateFunction;
    public PriceUnitFunction priceUnitFunction;
    public SetTargetItemCountFunction setTargetItemCountFunction;
    public AITargetItemFunction aiTargetItemFunction;

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

        //loadSigns();

        //load functions
        for(Field field: getClass().getFields()){
            try{
                if(ShopFunction.class.isAssignableFrom(field.getType())){
                    field.set(this, field.getType().getConstructor(Man10Shop.class, Man10ShopV2.class).newInstance(this, plugin));

                    //set shop id in setting fields
                    ShopFunction func = (ShopFunction) field.get(this);
                    for(Field innerField: func.getClass().getFields()){
                        if(Man10ShopSetting.class.isAssignableFrom(innerField.getType())) {
                            Man10ShopSetting<?> setting = ((Man10ShopSetting) innerField.get(func));
                            settingTypeMap.put(setting.settingId, ((ParameterizedType) innerField.getGenericType()).getActualTypeArguments()[0]);
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

        if(!allowedToUseShop(p)) {
            SInventory.closeNoEvent(p, plugin);
            return;
        }

        //all function check
        for(ShopFunction func: functions){
            if(!func.getClass().isAnnotationPresent(ShopFunctionDefinition.class)) continue;
            if(!func.isFunctionEnabled()) continue;
            if(func.getDefinition().enabledShopType().length != 0 && !ArrayUtils.contains(func.getDefinition().enabledShopType(), shopType.getShopType())) continue;
            if(!func.isAllowedToUseShopWithAmount(p, amount)){
                SInventory.closeNoEvent(p, plugin);
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
        }else if(shopType.getShopType() == Man10ShopType.COMMAND){
            Man10ShopV2API.tradeLog(shopId, "COMMAND", 1, price.getPrice(), p.getName(), p.getUniqueId()); //log
            p.sendMessage(Man10ShopV2.prefix + "§a§l購入しました");
        }

    }

    public void deleteShop(){
        Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET `deleted` = 1 WHERE shop_id = '" + shopId + "'");
        Man10ShopV2API.shopCache.remove(shopId);
        if(admin) Man10ShopV2API.adminShopIds.remove(shopId);
        Man10ShopV2API.closeInventoryGroup(shopId);
    }

    public SInventory getActionMenu(Player p){
        if(shopType.getShopType() == Man10ShopType.BUY || shopType.getShopType() == Man10ShopType.SELL){
            return new BuySellActionMenu(p, this, plugin);
        }

        if(shopType.getShopType() == Man10ShopType.BARTER){
            return new BarterActionMenu(p, this, plugin);
        }

        if(shopType.getShopType() == Man10ShopType.LOOT_BOX){
            return new LootBoxActionMenu(p, this, plugin);
        }

        if(shopType.getShopType() == Man10ShopType.QUEST){
            return new QuestActionMenu(p, this, plugin);
        }

        if(shopType.getShopType() == Man10ShopType.COMMAND){
            return new CommandActionMenu(p, this, plugin);
        }
        return null;
    }

    public void openActionMenu(Player p){
        Bukkit.getScheduler().runTask(plugin, ()->{getActionMenu(p).open(p);});
    }
    
    public ArrayList<String> getSignData(){
        ArrayList<String> result = new ArrayList<>();
        result.add("");
        result.add("");
        result.add("");
        result.add("");

        if(shopType.getShopType() == Man10ShopType.BUY){
            result.set(0, "§a§l販売ショップ");
        }else if(shopType.getShopType() == Man10ShopType.SELL){
            result.set(0, "§c§l買取ショップ");
        }else if(shopType.getShopType() == Man10ShopType.BARTER){
            result.set(0, "§b§lトレードショップ");
        }else if(shopType.getShopType() == Man10ShopType.LOOT_BOX){
            result.set(0, "§d§lガチャ");
        }else if(shopType.getShopType() == Man10ShopType.QUEST){
            result.set(0, "§6§lクエスト");
        }else if(shopType.getShopType() == Man10ShopType.COMMAND){
            result.set(0, "§e§lコマンドショップ");
        }

        if(shopEnabled.enabled.get()){
            if(shopType.getShopType() == Man10ShopType.BUY || shopType.getShopType() == Man10ShopType.SELL || shopType.getShopType() == Man10ShopType.COMMAND){
                if(secretPrice.isFunctionEnabled()){
                    result.set(1, "§b??????円");
                }else{
                    result.set(1, "§b" + BaseUtils.priceString(price.getPrice()) + "円");
                }
            }else if(shopType.getShopType() == Man10ShopType.BARTER){
                result.set(1, "");
            }else if(shopType.getShopType() == Man10ShopType.LOOT_BOX){
                SStringBuilder priceString = new SStringBuilder().text("§b");
                if(lootBoxPaymentFunction.balancePrice.get() != 0){
                    priceString.text(BaseUtils.priceString(lootBoxPaymentFunction.balancePrice.get()) + "円");
                }
                if(lootBoxPaymentFunction.itemPayment.get() != null){
                    if(lootBoxPaymentFunction.balancePrice.get() != 0) priceString.text("+");
                    priceString.text("アイテム");
                }
                result.set(1, priceString.build());
            }else if(shopType.getShopType() == Man10ShopType.QUEST){
                result.set(1, "");
            }

        }else{
            result.set(1, "§c取引停止中");
        }
        return result;
    }


}
