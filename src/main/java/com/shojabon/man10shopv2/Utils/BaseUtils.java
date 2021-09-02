package com.shojabon.man10shopv2.Utils;

import com.shojabon.man10shopv2.Enums.Man10ShopType;
import org.bukkit.Material;

public class BaseUtils {

    public static boolean isInt(String testing){
        try{
            Integer.parseInt(testing);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
    }

    public static boolean isBoolean(String testing){
        return "true".equalsIgnoreCase(testing) || "false".equalsIgnoreCase(testing);
    }

    static Material[] signs = new Material[]{
            Material.OAK_SIGN,
            Material.OAK_WALL_SIGN,
            Material.SPRUCE_SIGN,
            Material.SPRUCE_WALL_SIGN,
            Material.ACACIA_SIGN,
            Material.ACACIA_WALL_SIGN,
            Material.BIRCH_SIGN,
            Material.BIRCH_WALL_SIGN,
            Material.CRIMSON_SIGN,
            Material.CRIMSON_WALL_SIGN,
            Material.DARK_OAK_SIGN,
            Material.DARK_OAK_WALL_SIGN,
            Material.JUNGLE_SIGN,
            Material.JUNGLE_WALL_SIGN,
            Material.WARPED_SIGN,
            Material.WARPED_WALL_SIGN
    };

    public static boolean isSign(Material type){
        for(Material sign: signs){
            if(type == sign) return true;
        }
        return false;
    }

    public static String priceString(int price){
        return String.format("%,d", price);
    }

    public static String booleanToJapaneseText(boolean bool){
        if(bool){
            return "有効";
        }
        return "無効";
    }

    public static String buySellToString(Man10ShopType type){
        if(type == Man10ShopType.BUY) return "販売ショップ";
        return "買取ショップ";
    }


}
