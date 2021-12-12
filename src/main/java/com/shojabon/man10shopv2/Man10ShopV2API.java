package com.shojabon.man10shopv2;

import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopModerator;
import com.shojabon.man10shopv2.dataClass.Man10ShopOrder;
import com.shojabon.man10shopv2.dataClass.Man10ShopSign;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.MySQL.MySQLAPI;
import com.shojabon.mcutils.Utils.MySQL.MySQLCachedResultSet;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

public class Man10ShopV2API {

    public static Man10ShopV2 plugin;

    public static HashMap<UUID, Man10Shop> shopCache = new HashMap<>();
    public static HashMap<UUID, ArrayList<UUID>> userModeratingShopList = new HashMap<>();
    public static HashMap<String, Man10ShopSign> signs = new HashMap<>();

    public Man10ShopV2API(Man10ShopV2 plugin){
        Man10ShopV2API.plugin = plugin;
        loadAllShops();
        startTransactionThread();
        startPerMinuteExecutionTask();
    }

    public Man10Shop getShop(UUID shopId){
        if(shopCache.containsKey(shopId)){
            return shopCache.get(shopId);
        }
        ArrayList<MySQLCachedResultSet> result = Man10ShopV2.mysql.query("SELECT * FROM man10shop_shops WHERE shop_id = '" + shopId + "' AND deleted = 0 LIMIT 1;");
        if(result == null) return null;
        Man10Shop shop = null;
        for(MySQLCachedResultSet rs: result){
            shop = new Man10Shop(UUID.fromString(rs.getString("shop_id")),
                    rs.getString("name"),
                    rs.getInt("item_count"),
                    rs.getInt("price"),
                    rs.getInt("money"),
                    SItemStack.fromBase64(rs.getString("target_item")),
                    Man10ShopType.valueOf(rs.getString("shop_type")),
                    rs.getBoolean("admin"));
        }
        if(shop == null){
            return null;
        }



        shopCache.put(shop.shopId, shop);
        return shop;
    }

    public ArrayList<Man10Shop> getShops(ArrayList<UUID> ids){
        ArrayList<Man10Shop> shops = new ArrayList<>();
        for(UUID shopId: ids){
            Man10Shop shop = getShop(shopId);
            if(shop == null) continue;
            shops.add(shop);
        }
        return shops;
    }

    public UUID createShop(Player p, String name, int price, SItemStack targetItem, Man10ShopType shopType, boolean admin){
        UUID shopId = UUID.randomUUID();
        Man10Shop shop = new Man10Shop(shopId, name, 0, price, 0, targetItem, shopType, admin);

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("shop_id", shop.getShopId());
        payload.put("name", shop.name.getName());
        payload.put("item_count", shop.storage.getItemCount());
        payload.put("price", shop.price.getPrice());
        payload.put("money", shop.money.getMoney());
        payload.put("target_item", shop.targetItem.getTargetItem().getItemTypeBase64(true));
        payload.put("target_item_hash", shop.targetItem.getTargetItem().getItemTypeMD5(true));
        payload.put("shop_type", shop.shopType.getShopType().name());
        payload.put("deleted", 0);
        payload.put("admin", admin);
        Man10ShopV2.mysql.execute(MySQLAPI.buildInsertQuery(payload, "man10shop_shops"));
        if(!admin){
            if(!shop.permission.addModerator(new Man10ShopModerator(p.getName(), p.getUniqueId(), Man10ShopPermission.OWNER, true))){
                return null;
            }
        }
        return shopId;
    }

    public ArrayList<Man10Shop> getShopsWithPermission(UUID uuid){
        if(userModeratingShopList.containsKey(uuid)){
            ArrayList<UUID> shopIds = userModeratingShopList.get(uuid);
            return getShops(shopIds);
        }

        ArrayList<UUID> ids = new ArrayList<>();
        ArrayList<MySQLCachedResultSet> results = Man10ShopV2.mysql.query("SELECT * FROM man10shop_permissions WHERE UUID ='" + uuid.toString() + "'");
        for(MySQLCachedResultSet rs: results){
            ids.add(UUID.fromString(rs.getString("shop_id")));
        }
        userModeratingShopList.put(uuid, ids);
        return getShops(ids);
    }

    public ArrayList<Man10Shop> getAdminShops(){
        ArrayList<UUID> ids = new ArrayList<>();
        ArrayList<MySQLCachedResultSet> results = Man10ShopV2.mysql.query("SELECT shop_id FROM man10shop_shops WHERE admin = 'true'");
        for(MySQLCachedResultSet rs: results){
            ids.add(UUID.fromString(rs.getString("shop_id")));
        }
        return getShops(ids);
    }

    public void loadAllShops(){
        ArrayList<MySQLCachedResultSet> result = Man10ShopV2.mysql.query("SELECT shop_id FROM man10shop_shops WHERE deleted = 0;");
        for(MySQLCachedResultSet rs: result){
            Man10ShopV2.threadPool.execute(()-> {getShop(UUID.fromString(rs.getString("shop_id")));});
        }
    }

    //per minute task
    public void startPerMinuteExecutionTask(){
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, ()->{
            for(Man10Shop shop: shopCache.values()){
                shop.perMinuteExecuteTask();
            }
        }, (60- LocalDateTime.now().getSecond())*20, 20*60L);
    }

    //perform thread
    public static LinkedBlockingQueue<Man10ShopOrder> orders = new LinkedBlockingQueue<>();
    public static boolean transactionThreadActive = false;
    public static Thread transactionThread = new Thread(()->{
        while(transactionThreadActive){
            try {
                Man10ShopOrder order = orders.take();
                if(order.amount == -1) {
                    transactionThreadActive = false;
                    return;
                }
                if(order.player == null || !order.player.isOnline()) continue;
                if(order.amount <= 0) continue;
                Man10Shop shop = Man10ShopV2.api.getShop(order.shopId);
                if(shop == null) continue;
                shop.performAction(order.player, order.amount);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });

    public void startTransactionThread(){
        if(transactionThread.isAlive()) return;
        if(transactionThreadActive) return;
        transactionThreadActive = true;
        transactionThread.start();
    }

    public void stopTransactionThread(){
        if(!transactionThreadActive) return;
        if(!transactionThread.isAlive()) return;
        orders.add(new Man10ShopOrder(null, null, -1));
    }

    public void addTransaction(Man10ShopOrder order){
        if(!transactionThreadActive) return;
        orders.add(order);
    }

    //sign

    public String generateLocationId(Location l){
        return l.getWorld().getName() + "|" + l.getBlockX() + "|" + l.getBlockY() + "|" + l.getBlockZ();
    }

    public boolean createSign(Man10ShopSign sign){
        Man10Shop shop = getShop(sign.shopId);
        if(shop == null){
            return false;
        }
        signs.put(sign.generateLocationId(), sign);
        shop.signs.put(sign.generateLocationId(), sign);
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("shop_id", shop.shopId);
        payload.put("location_id", sign.generateLocationId());
        payload.put("world", sign.world);
        payload.put("x", sign.x);
        payload.put("y", sign.y);
        payload.put("z", sign.z);
        return Man10ShopV2.mysql.execute(MySQLAPI.buildReplaceQuery(payload, "man10shop_signs"));
    }

    public Man10ShopSign getSign(Location location){
        String locationId = generateLocationId(location);
        if(signs.containsKey(locationId)) return signs.get(locationId);
        ArrayList<MySQLCachedResultSet> result = Man10ShopV2.mysql.query("SELECT * FROM man10shop_signs WHERE location_id = '" + locationId + "' LIMIT 1;");
        if(result.size() == 0){
            signs.put(locationId, null);
            return null;
        }
        for(MySQLCachedResultSet rs: result){
            Man10Shop shop = getShop(UUID.fromString(rs.getString("shop_id")));
            if(shop == null) continue;
            Man10ShopSign sign = new Man10ShopSign(shop.shopId,
                    rs.getString("world"),
                    rs.getInt("x"),
                    rs.getInt("y"),
                    rs.getInt("z"));
            shop.signs.put(locationId, sign);
            signs.put(locationId, sign);
            return signs.get(locationId);
        }
        return null;
    }

    public boolean deleteSign(Man10ShopSign sign){
        System.out.println("DELETING " + sign.generateLocationId());

        boolean result = Man10ShopV2.mysql.execute("DELETE FROM man10shop_signs WHERE location_id = '" + sign.generateLocationId() + "'");
        if(!result) return false;

        signs.remove(sign.generateLocationId());
        Man10Shop shop = getShop(sign.shopId);
        if(shop == null) return false;
        shop.signs.remove(sign.generateLocationId());
        return true;
    }

    public void destroyAllSigns(Man10Shop shop){
        for(Man10ShopSign sign: shop.signs.values()){
            Location l = sign.getLocation();
            Block b = l.getBlock();
            deleteSign(sign);
            if(!(b.getState() instanceof Sign)){
                continue;
            }
            b.breakNaturally();
        }
    }

    public void updateAllSigns(Man10Shop shop){
        plugin.getServer().getScheduler().runTask(plugin, ()->{
            for(Man10ShopSign signObject: shop.signs.values()){
                Location l = signObject.getLocation();
                if(l == null) continue;
                if(!(l.getBlock().getState() instanceof Sign)){
                    deleteSign(signObject);
                    continue;
                }
                Sign sign = (Sign) l.getBlock().getState();

                if(shop.shopType.getShopType() == Man10ShopType.BUY){
                    sign.setLine(0, "§a§l販売ショップ");
                }else if(shop.shopType.getShopType() == Man10ShopType.SELL){
                    sign.setLine(0, "§c§l買取ショップ");
                }else if(shop.shopType.getShopType() == Man10ShopType.BARTER){
                    sign.setLine(0, "§b§lトレードショップ");
                }else if(shop.shopType.getShopType() == Man10ShopType.LOOT_BOX){
                    sign.setLine(0, "§d§lガチャ");
                }else if(shop.shopType.getShopType() == Man10ShopType.QUEST){
                    sign.setLine(0, "§6§lクエスト");
                }

                if(shop.shopEnabled.enabled.get()){
                    if(shop.shopType.getShopType() == Man10ShopType.BUY || shop.shopType.getShopType() == Man10ShopType.SELL){
                        if(shop.secretPrice.isFunctionEnabled()){
                            sign.setLine(1, "§b??????円");
                        }else{
                            sign.setLine(1, "§b" + BaseUtils.priceString(shop.price.getPrice()) + "円");
                        }
                    }else if(shop.shopType.getShopType() == Man10ShopType.BARTER){
                        sign.setLine(1, "");
                    }else if(shop.shopType.getShopType() == Man10ShopType.LOOT_BOX){
                        SStringBuilder priceString = new SStringBuilder().text("§b");
                        if(shop.lootBoxPaymentFunction.balancePrice.get() != 0){
                            priceString.text(BaseUtils.priceString(shop.lootBoxPaymentFunction.balancePrice.get()) + "円");
                        }
                        if(shop.lootBoxPaymentFunction.itemPayment.get() != null){
                            if(shop.lootBoxPaymentFunction.balancePrice.get() != 0) priceString.text("+");
                            priceString.text("アイテム");
                        }
                        sign.setLine(1, priceString.build());
                    }else if(shop.shopType.getShopType() == Man10ShopType.QUEST){
                        sign.setLine(1, "");
                    }

                }else{
                    sign.setLine(1, "§c取引停止中");
                }
                sign.update();


            }
        });
    }

    //close inventory

    public static void closeInventoryGroup(UUID group){
        SInventory.closeInventoryGroup(group, plugin);
    }

    //log

    public static void log(UUID shopId, String logType, Object value, String name, UUID uuid){
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("shop_id", shopId);
        payload.put("log_type", logType);
        payload.put("value", value);
        payload.put("name", name);
        payload.put("uuid", uuid.toString());

        Man10ShopV2.mysql.futureExecute(MySQLAPI.buildInsertQuery(payload, "man10shop_log"));
    }

    public static void tradeLog(UUID shopId, String action, int amount, int total_price, String name, UUID uuid){
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("shop_id", shopId);
        payload.put("action", action);
        payload.put("amount", amount);
        payload.put("total_price", total_price);
        payload.put("name", name);
        payload.put("uuid", uuid.toString());

        Man10ShopV2.mysql.futureExecute(MySQLAPI.buildInsertQuery(payload, "man10shop_trade_log"));
    }

    //cache clear

    public void clearCache(){
        shopCache.clear();
        userModeratingShopList.clear();
        signs.clear();
        orders.clear();
    }
}
