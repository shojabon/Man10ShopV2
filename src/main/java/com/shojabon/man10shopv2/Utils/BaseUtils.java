package com.shojabon.man10shopv2.Utils;

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
        try{
            Boolean.parseBoolean(testing);
            return true;
        }catch (Exception e){
            return false;
        }
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

    public static String booleanToJapaneseText(boolean bool){
        if(bool){
            return "有効";
        }
        return "無効";
    }
}
