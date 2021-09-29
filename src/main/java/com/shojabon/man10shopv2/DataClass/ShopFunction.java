package com.shojabon.man10shopv2.DataClass;

import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Utils.MySQL.MySQLAPI;
import com.shojabon.man10shopv2.Utils.MySQL.MySQLCachedResultSet;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import org.bukkit.entity.Player;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class ShopFunction {

    private final Man10Shop shop;

    public ShopFunction(Man10Shop shop){
        this.shop = shop;
    }

    // override functions
    public boolean isAllowedToUseShop(Player p){
        return true;
    }

    public SInventoryItem getSettingItem(){return null;}


    //setting
    private HashMap<String, String> settings = new HashMap<>();

    private String calculateUniqueSettingsHash(String key){
        try {
            byte[] result = new byte[0];
            result = MessageDigest.getInstance("MD5")
                    .digest((shop.shopId.toString() + "." + key).getBytes(StandardCharsets.UTF_8));
            return String.format("%020x", new BigInteger(1, result));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteSetting(String key){
        boolean result = Man10ShopV2.mysql.execute("DELETE FROM man10shop_settings WHERE unique_setting_hash = '" + calculateUniqueSettingsHash(key) + "';");
        if(!result) return false;
        settings.remove(key);
        return true;
    }

    public boolean setSetting(String key, String value){
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("shop_id", shop.shopId);
        payload.put("unique_setting_hash", calculateUniqueSettingsHash(key));
        payload.put("key", key);
        payload.put("value", value);

        boolean result = Man10ShopV2.mysql.execute(MySQLAPI.buildReplaceQuery(payload, "man10shop_settings"));
        if(!result) return false;
        settings.put(key, value);

        return true;
    }

    public boolean setSetting(String key, int value){
        return setSetting(key, String.valueOf(value));
    }

    public boolean setSetting(String key, boolean value){
        return setSetting(key, String.valueOf(value));
    }

    public String getSetting(String key){
        if(settings.containsKey(key)){
            return settings.get(key);
        }
        ArrayList<MySQLCachedResultSet> result = Man10ShopV2.mysql.query("SELECT * FROM man10shop_settings WHERE shop_id = '" + shop.shopId + "' AND `key` = '" + key + "' LIMIT 1;");
        for(MySQLCachedResultSet rs: result){
            settings.put(rs.getString("key"), rs.getString("value"));
        }
        return settings.get(key);
    }



}
