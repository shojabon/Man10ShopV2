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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
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
        if(currentSettingString() !=  null) item.addLore("§d現在設定");
        if(currentSettingString() != null){
            for(String current: currentSettingString().split("\n")){
                item.addLore("§e" + current);
            }
        }
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

    public boolean isFunctionEnabled() {
        try{
            for(Field innerField: getClass().getFields()){
                if(Man10ShopSetting.class.isAssignableFrom(innerField.getType())) {
                    Man10ShopSetting setting = ((Man10ShopSetting) innerField.get(this));
                    if(setting.get() == setting.defaultValue) return false;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean performAction(Player p, int amount){return true;}

    public boolean afterPerformAction(Player p, int amount){return true;}

    public void init(){}

    public String currentSettingString(){
        return null;
    }

    public void perMinuteExecuteTask(){}


}
