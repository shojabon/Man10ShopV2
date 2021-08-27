package com.shojabon.man10shopv2.Utils;

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

    public static String booleanToJapaneseText(boolean bool){
        if(bool){
            return "有効";
        }
        return "無効";
    }
}
