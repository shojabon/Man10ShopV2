package com.shojabon.man10shopv2.DataClass;

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

import java.util.*;

public class Man10Shop {

    public String name;
    public UUID shopId;
    public int storageSize;
    public int itemCount;
    public int price;

    public boolean admin = false;

    public int money;

    public SItemStack targetItem;
    public int targetItemCount;
    public ItemStack icon;
    public Man10ShopType shopType;

    public HashMap<UUID, Man10ShopModerator> moderators = new HashMap<>();
    public HashMap<String, Man10ShopSign> signs = new HashMap<>();
    public Man10ShopSettings settings;

    public boolean currentlyEditingStorage = false;

    public HashMap<UUID, Long> coolDownMap = new HashMap<>();
    public HashMap<UUID, LinkedList<Man10ShopLogObject>> perMinuteCoolDownMap = new HashMap<>();

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
        this.itemCount = itemCount;
        this.targetItem = targetItem;
        this.targetItemCount = targetItemCount;
        this.icon = new ItemStack(targetItem.getType());
        this.shopType = shopType;
        this.settings = new Man10ShopSettings(this.shopId);
        this.admin = admin;

        loadPermissions();
        loadSigns();
        loadPerMinuteMap();
        storageSize = calculateCurrentStorageSize(0);
    }


    //storage

    public boolean removeItemCount(int count){
        itemCount = itemCount - count;
        boolean result = Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET item_count = item_count - " + count + " WHERE shop_id = '" + shopId + "'");
        if(!result) return false;
        return true;
    }

    public boolean addItemCount(int count){
        itemCount = itemCount + count;
        boolean result = Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET item_count = item_count + " + count + " WHERE shop_id = '" + shopId + "'");
        if(!result) return false;
        //log here
        return true;
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

    //permissions settings

    public Man10ShopPermission getPermission(UUID uuid){
        //admin mode admin
        if(admin){
            Player targetPlayer = Bukkit.getPlayer(uuid);
            if(targetPlayer != null && targetPlayer.isOnline()){
                if(targetPlayer.hasPermission("man10shopv2.admin")) return Man10ShopPermission.OWNER;
            }
        }

        if(!moderators.containsKey(uuid)) return null;
        return moderators.get(uuid).permission;
    }

    public boolean hasPermissionAtLeast(UUID uuid, Man10ShopPermission permission){
        Man10ShopPermission actualPerm = getPermission(uuid);
        if(actualPerm == null) return false;

        int userPermissionLevel = calculatePermissionLevel(actualPerm);

        int requiredPermissionLevel = calculatePermissionLevel(permission);
        return userPermissionLevel >= requiredPermissionLevel;
    }

    public boolean hasPermission(UUID uuid, Man10ShopPermission permission){
        Man10ShopPermission actualPerm = getPermission(uuid);
        if(actualPerm == null) return false;
        return actualPerm == permission;
    }

    public int ownerCount(){
        if(admin) return 1;
        int result = 0;
        for(Man10ShopModerator mod: moderators.values()){
            if(mod.permission == Man10ShopPermission.OWNER) result ++;
        }
        return result;
    }

    private int calculatePermissionLevel(Man10ShopPermission permission){
        int permissionLevel = 0;
        switch (permission){
            case OWNER: permissionLevel = 10; break;
            case MODERATOR: permissionLevel = 9; break;
            case ACCOUNTANT: permissionLevel = 7; break;
            case STORAGE_ACCESS: permissionLevel = 7; break;
        }
        return permissionLevel;
    }

    public boolean addModerator(Man10ShopModerator moderator){
        Man10ShopV2.mysql.execute("DELETE FROM man10shop_permissions WHERE shop_id ='" + shopId + "' AND uuid = '" + moderator.uuid + "'");

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("name", moderator.name);
        payload.put("uuid", moderator.uuid.toString());
        payload.put("shop_id", shopId.toString());
        payload.put("permission", moderator.permission.name());
        payload.put("notification", moderator.notificationEnabled);
        if(!Man10ShopV2.mysql.execute(MySQLAPI.buildInsertQuery(payload, "man10shop_permissions"))) return false;
        moderators.put(moderator.uuid, moderator);
        Man10ShopV2API.userModeratingShopList.remove(moderator.uuid);
        return true;
    }

    public boolean setModerator(Man10ShopModerator moderator){
        Man10ShopV2.mysql.execute("DELETE FROM man10shop_permissions WHERE shop_id ='" + shopId + "' AND uuid = '" + moderator.uuid + "'");

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("name", moderator.name);
        payload.put("uuid", moderator.uuid.toString());
        payload.put("shop_id", shopId.toString());
        payload.put("permission", moderator.permission.name());
        payload.put("notification", moderator.notificationEnabled);
        if(!Man10ShopV2.mysql.execute(MySQLAPI.buildInsertQuery(payload, "man10shop_permissions"))) return false;
        moderators.put(moderator.uuid, moderator);
        Man10ShopV2API.userModeratingShopList.remove(moderator.uuid);
        return true;
    }

    public boolean removeModerator(Man10ShopModerator moderator){
        boolean result = Man10ShopV2.mysql.execute("DELETE FROM man10shop_permissions WHERE shop_id ='" + shopId + "' AND uuid = '" + moderator.uuid + "'");
        if(!result) return false;
        moderators.remove(moderator.uuid);
        Man10ShopV2API.userModeratingShopList.remove(moderator.uuid);
        return true;
    }

    public void loadPermissions(){
        ArrayList<MySQLCachedResultSet> results = Man10ShopV2.mysql.query("SELECT * FROM man10shop_permissions WHERE shop_id = '" + shopId + "'");
        for(MySQLCachedResultSet rs: results) {
            UUID uuid = UUID.fromString(rs.getString("uuid"));
            Man10ShopModerator permission = new Man10ShopModerator(rs.getString("name"), uuid, Man10ShopPermission.valueOf(rs.getString("permission")), rs.getBoolean("notification"));
            moderators.put(uuid, permission);
        }
    }

    //storage space

    public int calculateCurrentStorageSize(int addedUnits){
        int boughtCurrentStorageCount = settings.getBoughtStorageUnits();
        int defaultUnit = Man10ShopV2.config.getInt("itemStorage.unitDefinition");
        if(defaultUnit == 0) defaultUnit = 1;
        return (addedUnits+boughtCurrentStorageCount)*defaultUnit;
    }

    public int calculateNextUnitPrice(int addUnits){
        int totalPrice = 0;

        MemorySection section = ((MemorySection)Man10ShopV2.config.get("itemStorage.prices"));
        if(section == null) return -1;

        int nextUnit = settings.getBoughtStorageUnits();

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
        int boughtCurrentStorageCount = settings.getBoughtStorageUnits();
        if(boughtCurrentStorageCount+units > Man10ShopV2.config.getInt("itemStorage.maxStorageUnits")){
            p.sendMessage(Man10ShopV2.prefix + "§c§l倉庫ユニットの上限を超えました");
            return false;
        }
        if(Man10ShopV2.vault.getBalance(p.getUniqueId()) < calculateNextUnitPrice(units)){
            p.sendMessage(Man10ShopV2.prefix + "§c§lお金が足りません");
            return false;
        }
        Man10ShopV2.vault.withdraw(p.getUniqueId(), calculateNextUnitPrice(units));
        Man10ShopV2API.log(shopId, "buyStorageSpace", units, p.getName(), p.getUniqueId()); //log
        p.sendMessage(Man10ShopV2.prefix + "§a§l倉庫スペースを購入しました");
        storageSize = calculateCurrentStorageSize(units);
        return buyStorageSpace(units);
    }

    public boolean buyStorageSpace(int units){
        return settings.setBoughtStorageUnits(settings.getBoughtStorageUnits()+units);

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

    //actions

//    public boolean playerHasEnoughItems(Player p,int amount){
//        HashMap<String, Integer> invCount = new HashMap<>();
//        for(ItemStack item: p.getInventory().getContents()){
//            String hash = new SItemStack(item).getItemTypeMD5();
//            if(!invCount.containsKey(hash)) invCount.put(hash, 0);
//            if(item == null) continue;
//            int currentCount = invCount.get(hash);
//            invCount.put(hash, currentCount + item.getAmount());
//        }
//        if(!invCount.containsKey(targetItem.getItemTypeMD5())) return false;
//        int result = invCount.get(targetItem.getItemTypeMD5());
//        return result >= amount;
//    }

    public boolean allowedToUseShop(Player p){
        //permission to use
        if(!p.hasPermission("man10shopv2.use")){
            p.sendMessage(Man10ShopV2.prefix + "§c§lあなたには権限がありません");
            return false;
        }

        //weekday toggle
        if(!settings.getWeekdayShopToggle()[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1]){
            p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップを本日ご利用することはできません");
            StringBuilder availableWeekDays = new StringBuilder();
            int i = 0;
            for(boolean enabled: settings.getWeekdayShopToggle()){
                if(enabled){
                    availableWeekDays.append(BaseUtils.weekToString(i)).append(" ");
                }
                i++;
            }
            if(availableWeekDays.toString().length() != 0){
                p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップは" + availableWeekDays.substring(0, availableWeekDays.length()-1) + "に利用することができます");
            }
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

        //shop disabled
        if(!settings.getShopEnabled()){
            p.sendMessage(Man10ShopV2.prefix + "§c§l現在このショップは停止しています");
            return false;
        }

        //if player has permission
        if(settings.getAllowedPermission() != null && !p.hasPermission("man10shopv2.use." + settings.getAllowedPermission())){
            p.sendMessage(Man10ShopV2.prefix + "§c§lあなたはこのショップを使う権限がありません");
            return false;
        }

        //if player is in coolDown
        if(checkCoolDown(p)){
            p.sendMessage(Man10ShopV2.prefix + "§c§l" + settings.getCoolDownTime() + "秒の取引クールダウン中です");
            return false;
        }

        //if player is in per minute cool down
        if(checkPerMinuteCoolDown(p, 1)){
            p.sendMessage(Man10ShopV2.prefix + "§c§l時間内の最大取引数に達しました");
            return false;
        }

        if(shopType == Man10ShopType.BUY){
            if(itemCount <= 0 && !admin){
                p.sendMessage(Man10ShopV2.prefix + "§c§l在庫がありません");
                return false;
            }
            //no items (buy)
        }else{
            //no money (sell)
            if(money < price && !admin){
                p.sendMessage(Man10ShopV2.prefix + "§c§lショップの残高が不足しています");
                return false;
            }

            //no money (sell)
            if(settings.getStorageCap() != 0 && itemCount >= settings.getStorageCap()){
                p.sendMessage(Man10ShopV2.prefix + "§c§l現在このショップは買取をしていません");
                return false;
            }
            if(itemCount >= calculateCurrentStorageSize(0)){
                p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップは在庫がいっぱいです");
                return false;
            }
        }
        return true;
    }

    public void performAction(Player p, int amount){

        if(!allowedToUseShop(p)) return;

        if(checkPerMinuteCoolDown(p, amount)){
            p.sendMessage(Man10ShopV2.prefix + "§c§l時間内の最大取引数に達しました");
            return;
        }

        if(shopType == Man10ShopType.BUY){
            if(amount > itemCount && !admin){
                amount = itemCount;
            }
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
            boolean removeItemResult =  removeItemCount(amount*item.getAmount());
            if(!removeItemResult){
                p.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return;
            }

            for(int i = 0; i < amount; i++){
                p.getInventory().addItem(item.build());
            }

            Man10ShopV2API.tradeLog(shopId,"BUY", amount*item.getAmount() , totalPrice, p.getName(), p.getUniqueId()); //log
            addPerMinuteCoolDownLog(p.getUniqueId(), new Man10ShopLogObject(System.currentTimeMillis() / 1000L, amount));

            p.sendMessage(Man10ShopV2.prefix + "§a§l" + item.getDisplayName() + "§a§lを" + amount*item.getAmount() + "個購入しました");
            notifyModerators(amount*item.getAmount());
            setCoolDown(p); //set coolDown

        }else if(shopType == Man10ShopType.SELL){
            //if item storage hits storage cap
            if(itemCount + amount > settings.getStorageCap() && settings.getStorageCap() != 0){
                p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップは現在買取を行っていません");
                return;
            }
            SItemStack item = new SItemStack(targetItem.build().clone());
            if(itemCount + amount > calculateCurrentStorageSize(0) && !admin){
                p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップは現在買取を行っていません");
                return;
            }
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
            boolean addItemResult =  addItemCount(amount);
            if(!addItemResult){
                p.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return;
            }

            //perform transaction
            for( int i =0; i < amount; i++){
                p.getInventory().removeItemAnySlot(item.build());
            }

            Man10ShopV2API.tradeLog(shopId,"SELL", amount*item.getAmount() , totalPrice, p.getName(), p.getUniqueId()); //log
            addPerMinuteCoolDownLog(p.getUniqueId(), new Man10ShopLogObject(System.currentTimeMillis() / 1000L, amount));

            p.sendMessage(Man10ShopV2.prefix + "§a§l" + item.getDisplayName() + "§a§lを" + amount*item.getAmount() + "個売却しました");
            notifyModerators(amount*item.getAmount());
            setCoolDown(p); //set coolDown


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

    //notification

    public boolean setEnableNotification(Man10ShopModerator mod, boolean enabled){
        if(!moderators.containsKey(mod.uuid)){
            return false;
        }
        if(mod.notificationEnabled == enabled) return true;
        mod.notificationEnabled = enabled;
        moderators.put(mod.uuid, mod);
        if(!Man10ShopV2.mysql.execute("UPDATE man10shop_permissions SET notification = '" + enabled + "' WHERE shop_id = '" + shopId + "' AND uuid = '" + mod.uuid + "'")){
            return false;
        }
        return true;
    }

    public void notifyModerators(int amount){
        for(Man10ShopModerator mod: moderators.values()){
            if(!mod.notificationEnabled) continue;
            Player p = Bukkit.getServer().getPlayer(mod.uuid);
            if(p == null) continue;
            if(!p.isOnline()) continue;
            p.sendMessage(Man10ShopV2.prefix + "§a§l" + name + "で" + amount + "個のアイテム取引がありました");
        }
    }

    //coolDown

    public boolean checkCoolDown(Player p){
        int coolDown = settings.getCoolDownTime();
        if(coolDown == 0) return false;
        if(!coolDownMap.containsKey(p.getUniqueId())) coolDownMap.put(p.getUniqueId(), 0L);
        long currentTime = System.currentTimeMillis() / 1000L;

        return currentTime - coolDownMap.get(p.getUniqueId()) < coolDown;
    }

    public void setCoolDown(Player p){
        long currentTime = System.currentTimeMillis() / 1000L;
        coolDownMap.put(p.getUniqueId(), currentTime);
    }

    //per minute cool down
    public void loadPerMinuteMap(){
        perMinuteCoolDownMap.clear();
        if(settings.getPerMinuteCoolDownTime() == 0 || settings.getPerMinuteCoolDownAmount() == 0){
            return;
        }

        ArrayList<MySQLCachedResultSet> result = Man10ShopV2.mysql.query("SELECT SUM(amount) AS amount,uuid,UNIX_TIMESTAMP(date_time) AS time FROM man10shop_trade_log WHERE shop_id = \"" + shopId + "\" and UNIX_TIMESTAMP(date_time) >= UNIX_TIMESTAMP(CURRENT_TIMESTAMP()) - " + settings.getPerMinuteCoolDownTime()*60L + " GROUP BY UUID, YEAR(date_time), MONTH(date_time), DATE(date_time), HOUR(date_time), MINUTE(date_time) ORDER BY date_time DESC");
        for(MySQLCachedResultSet rs: result){
            addPerMinuteCoolDownLog(UUID.fromString(rs.getString("uuid")), new Man10ShopLogObject(rs.getLong("time"), rs.getInt("amount")));
        }
    }

    public void addPerMinuteCoolDownLog(UUID uuid, Man10ShopLogObject obj){
        if(!perMinuteCoolDownMap.containsKey(uuid)){
            perMinuteCoolDownMap.put(uuid, new LinkedList<>());
        }
        perMinuteCoolDownMap.get(uuid).addFirst(obj);
    }

    public int perMinuteCoolDownTotalAmountInTime(Player p){
        if(settings.getPerMinuteCoolDownTime() == 0 || settings.getPerMinuteCoolDownAmount() == 0){
            return 0;
        }

        if(!perMinuteCoolDownMap.containsKey(p.getUniqueId())){
            return 0;
        }
        int totalAmountInTime = 0;

        LinkedList<Man10ShopLogObject> logs = perMinuteCoolDownMap.get(p.getUniqueId());
        long currentTime = System.currentTimeMillis() / 1000L;
        //count amount
        for(int i = 0; i < logs.size(); i++){
            Man10ShopLogObject log = logs.get(i);
            if(currentTime - log.time >= settings.getPerMinuteCoolDownTime()* 60L) continue;
            totalAmountInTime += log.amount;
        }

        //delete unneeded logs
        for(int i = 0; i < logs.size(); i++){
            Man10ShopLogObject log = logs.getLast();
            if(currentTime - log.time >= settings.getPerMinuteCoolDownTime()* 60L) {
                logs.removeLast();
            }else{
                break;
            }
        }
        return totalAmountInTime;
    }

    public boolean checkPerMinuteCoolDown(Player p, int addingAmount){
        if(settings.getPerMinuteCoolDownTime() == 0 || settings.getPerMinuteCoolDownAmount() == 0){
            return false;
        }

        if(!perMinuteCoolDownMap.containsKey(p.getUniqueId())){
            if(addingAmount > settings.getPerMinuteCoolDownAmount()) return true;//if not trade within time and amount is bigger than limit
            return false;
        }

        return perMinuteCoolDownTotalAmountInTime(p) + addingAmount > settings.getPerMinuteCoolDownAmount();
    }


}
