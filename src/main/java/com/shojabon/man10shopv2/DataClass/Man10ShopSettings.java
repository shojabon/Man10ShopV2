package com.shojabon.man10shopv2.DataClass;

import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Utils.BaseUtils;
import com.shojabon.man10shopv2.Utils.MySQL.MySQLAPI;
import com.shojabon.man10shopv2.Utils.MySQL.MySQLCachedResultSet;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Man10ShopSettings {

    public HashMap<String, String> settings = new HashMap<>();
    public UUID shopId;

    public Man10ShopSettings(UUID shopId){
        this.shopId = shopId;
    }

    public String calculateUniqueSettingsHash(String key){
        try {
            byte[] result = new byte[0];
            result = MessageDigest.getInstance("MD5")
                    .digest((shopId.toString() + "." + key).getBytes(StandardCharsets.UTF_8));
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
        payload.put("shop_id", shopId);
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
        ArrayList<MySQLCachedResultSet> result = Man10ShopV2.mysql.query("SELECT * FROM man10shop_settings WHERE shop_id = '" + shopId + "' AND `key` = '" + key + "' LIMIT 1;");
        for(MySQLCachedResultSet rs: result){
            settings.put(rs.getString("key"), rs.getString("value"));
        }
        return settings.get(key);
    }

    //-------------------- storage cap ------------------

    public int getStorageCap(){
        String currentSetting = getSetting("storage.sell.cap");
        if(!BaseUtils.isInt(currentSetting)) return 0;
        return Integer.parseInt(currentSetting);
    }

    public boolean setStorageCap(int storageCap){
        if(getStorageCap() == storageCap) return true;
        return setSetting("storage.sell.cap", storageCap);
    }


    //----------------------- shop enabled for trade -----


    public boolean getShopEnabled(){
        String currentSetting = getSetting("shop.enabled");
        if(!BaseUtils.isBoolean(currentSetting)) return true;
        return Boolean.parseBoolean(currentSetting);
    }

    public boolean setShopEnabled(boolean enabled){
        if(getShopEnabled() == enabled) return true;
        if(!setSetting("shop.enabled", enabled)) return false;
        Man10ShopV2API.closeInventoryGroup(shopId);
        return true;
    }

    //----------------------- shop single sell mode -----


    public boolean isSingleTransactionMode(){
        String currentSetting = getSetting("shop.transaction.single");
        if(!BaseUtils.isBoolean(currentSetting)) return false;
        return Boolean.parseBoolean(currentSetting);
    }

    public boolean setSingleSellMode(boolean enabled){
        if(isSingleTransactionMode() == enabled) return true;
        if(!setSetting("shop.transaction.single", enabled)) return false;
        Man10ShopV2API.closeInventoryGroup(shopId);
        return true;
    }

    //-------------------- allowed permission ------------------

    public String getAllowedPermission(){
        return getSetting("shop.permission.allowed");
    }

    public boolean setAllowedPermission(String permission){
        if(getAllowedPermission() != null) {
            if(getAllowedPermission().equalsIgnoreCase(permission)) return true;
        }
        if(permission.equalsIgnoreCase("")) return deleteSetting("shop.permission.allowed");
        return setSetting("shop.permission.allowed", permission);
    }



}
