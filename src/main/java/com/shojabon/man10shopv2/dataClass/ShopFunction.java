package com.shojabon.man10shopv2.dataClass;

import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.mcutils.Utils.MySQL.MySQLAPI;
import com.shojabon.mcutils.Utils.MySQL.MySQLCachedResultSet;
import com.shojabon.mcutils.Utils.SConfigFile;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public abstract class ShopFunction {

    public final Man10Shop shop;
    public final Man10ShopV2 plugin;

    public ShopFunction(Man10Shop shop, Man10ShopV2 plugin){
        this.shop = shop;
        this.plugin = plugin;
    }

    //tools

    public void warn(Player p, String message){
        p.sendMessage(Man10ShopV2.prefix + "§c§l" + message);
    }


    public void success(Player p, String message){
        p.sendMessage(Man10ShopV2.prefix + "§a§l" + message);
    }

    public ShopFunctionDefinition getDefinition(){
        if(!this.getClass().isAnnotationPresent(ShopFunctionDefinition.class)) return null;
        return this.getClass().getAnnotation(ShopFunctionDefinition.class);
    }

    public SInventoryItem getSettingBaseItem(){
        ShopFunctionDefinition def = getDefinition();
        SItemStack item = new SItemStack(def.iconMaterial()).setDisplayName("§a" + def.name());
        if(currentSettingString() !=  null) item.addLore("§d現在設定: §e" + currentSettingString());
        item.addLore("");
        for(String explanation: def.explanation()){
            item.addLore("§f" + explanation);
        }
        return new SInventoryItem(item.build()).clickable(false);
    }



    // override functions
    public boolean isAllowedToUseShop(Player p){
        return true;
    }

    public boolean isAllowedToUseShopWithAmount(Player p, int amount){return true;}

    public SInventoryItem getSettingItem(Player player, SInventoryItem item){
        return null;
    }

    public int itemCount(Player p){
        if(shop.shopType.getShopType() == Man10ShopType.SELL){
            return shop.storage.getStorageSize();
        }else{
            return shop.storage.getItemCount();
        }
    }

    public boolean isFunctionEnabled(){return true;}

    public boolean performAction(Player p, int amount){return true;}

    public boolean afterPerformAction(Player p, int amount){return true;}

    public String currentSettingString(){
        return null;
    }

    public void perMinuteExecuteTask(){}


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

    public boolean setSetting(String key, long value){
        return setSetting(key, String.valueOf(value));
    }

    public boolean setSetting(String key, boolean value){
        return setSetting(key, String.valueOf(value));
    }

    public boolean setSetting(String key, YamlConfiguration config){
        return setSetting(key, SConfigFile.base64EncodeConfig(config));
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

    public YamlConfiguration getSettingYaml(String key){
        try{
            if(settings.containsKey(key)){
                return SConfigFile.loadConfigFromBase64(settings.get(key));
            }
            return SConfigFile.loadConfigFromBase64(getSetting(key));
        }catch (Exception e){
            return null;
        }
    }



}
