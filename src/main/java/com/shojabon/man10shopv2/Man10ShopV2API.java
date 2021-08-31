package com.shojabon.man10shopv2;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.Man10ShopModerator;
import com.shojabon.man10shopv2.DataClass.Man10ShopSign;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Utils.MySQL.MySQLAPI;
import com.shojabon.man10shopv2.Utils.MySQL.MySQLCachedResultSet;
import com.shojabon.man10shopv2.Utils.SItemStack;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Man10ShopV2API {

    Man10ShopV2 plugin;

    public static HashMap<UUID, Man10Shop> shopCache = new HashMap<>();
    public static HashMap<UUID, ArrayList<UUID>> userModeratingShopList = new HashMap<>();
    public static HashMap<String, Man10ShopSign> signs = new HashMap<>();

    public Man10ShopV2API(Man10ShopV2 plugin){
        this.plugin = plugin;
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
                    rs.getInt("target_item_count"),
                    Man10ShopType.valueOf(rs.getString("shop_type")));
        }
        if(shop == null){
            return null;
        }


        //load permissions
        ArrayList<MySQLCachedResultSet> permissionsResult = Man10ShopV2.mysql.query("SELECT * FROM man10shop_permissions WHERE shop_id = '" + shopId + "';");
        if(permissionsResult == null) return null;
        HashMap<UUID, Man10ShopModerator> moderators = new HashMap<>();
        for (MySQLCachedResultSet rs : permissionsResult) {
            UUID uuid = UUID.fromString(rs.getString("uuid"));
            moderators.put(uuid,
                    new Man10ShopModerator(rs.getString("name"), uuid, Man10ShopPermission.valueOf(rs.getString("permission"))));
        }
        shop.moderators = moderators;


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

    public boolean createShop(Player p, String name, int price, SItemStack targetItem, Man10ShopType shopType){
        UUID shopId = UUID.randomUUID();
        Man10Shop shop = new Man10Shop(shopId, name, 0, price, 0, targetItem, 1, shopType);

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("shop_id", shop.shopId);
        payload.put("name", shop.name);
        payload.put("item_count", shop.itemCount);
        payload.put("price", shop.price);
        payload.put("money", shop.money);
        payload.put("target_item", shop.targetItem.getItemTypeBase64());
        payload.put("target_item_hash", shop.targetItem.getItemTypeMD5());
        payload.put("target_item_count", 1);
        payload.put("shop_type", shop.shopType.name());
        payload.put("deleted", 0);
        Man10ShopV2.mysql.execute(MySQLAPI.buildInsertQuery(payload, "man10shop_shops"));
        return shop.addModerator(new Man10ShopModerator(p.getName(), p.getUniqueId(), Man10ShopPermission.OWNER));
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
        Block b = location.getBlock();
        if(!(b.getState() instanceof Sign)) return null;
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

        boolean result = Man10ShopV2.mysql.execute("DELETE FROM man10shop_signs WHERE location_id = '" + sign.generateLocationId() + "'");
        if(!result) return false;

        signs.remove(sign.generateLocationId());
        Man10Shop shop = getShop(sign.shopId);
        if(shop == null) return false;
        shop.signs.remove(sign.generateLocationId());
        return true;
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
}
