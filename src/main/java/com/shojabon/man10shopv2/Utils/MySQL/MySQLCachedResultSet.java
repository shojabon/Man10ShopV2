package com.shojabon.man10shopv2.Utils.MySQL;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Set;

public class MySQLCachedResultSet{

    HashMap<String, Object> result;

    public MySQLCachedResultSet(HashMap<String, Object> data){
        this.result = data;
    }

    public String getString(String colName){
        return String.valueOf(result.get(colName));
    }
    public int getInt(String colName){
        Object obj = result.get(colName);
        if(obj instanceof BigDecimal){
            return ((BigDecimal) obj).intValue();
        }
        return (Integer) obj;
    }
    public boolean getBoolean(String colName){
        return Boolean.parseBoolean((String)result.get(colName));
    }
    public double getDouble(String colName){
        return (Double) result.get(colName);
    }

    public Object getObject(String colName) {
        return result.get(colName);
    }


    public Set<String> getKeys(){
        return result.keySet();
    }

}