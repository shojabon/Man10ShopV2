package com.shojabon.man10shopv2.DataClass;

import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Utils.BaseUtils;
import com.shojabon.man10shopv2.Utils.MySQL.MySQLAPI;
import com.shojabon.man10shopv2.Utils.MySQL.MySQLCachedResultSet;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Man10ShopSettings {

    public HashMap<String, String> settings = new HashMap<>();
    public UUID shopId;

    public Man10ShopSettings(UUID shopId){
        this.shopId = shopId;
    }

    public String calculateUniquePermissionHash(String key){
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

    public boolean setSetting(String key, String value){
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("shop_id", shopId);
        payload.put("unique_setting_hash", calculateUniquePermissionHash(key));
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

    public String getSetting(String key){
        if(settings.containsKey(key)){
            return settings.get(key);
        }
        ArrayList<MySQLCachedResultSet> result = Man10ShopV2.mysql.query("SELECT * FROM man10shop_permissions WHERE shop_id = '" + shopId + "' AND key = '" + key + "' LIMIT 1;");
        for(MySQLCachedResultSet rs: result){
            settings.put(rs.getString("key"), rs.getString("value"));
        }
        return settings.get(key);
    }

    public int getStorageCap(){
        String currentSetting = getSetting("storage.sell.cap");
        if(!BaseUtils.isInt(currentSetting)) return 0;
        return Integer.parseInt(currentSetting);
    }

    public boolean setStorageCap(int storageCap){
        if(getStorageCap() == storageCap) return false;
        return setSetting("storage.sell.cap", storageCap);
    }




}
