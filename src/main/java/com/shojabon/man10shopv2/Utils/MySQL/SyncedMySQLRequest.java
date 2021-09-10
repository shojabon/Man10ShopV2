package com.shojabon.man10shopv2.Utils.MySQL;

import java.util.ArrayList;
import java.util.function.Consumer;

public class SyncedMySQLRequest {
    public Consumer<Boolean> executeCallback;
    public Consumer<ArrayList<MySQLCachedResultSet>> queryCallback;
    public String query;

    public boolean isQuery(){
        return queryCallback != null;
    }
}
