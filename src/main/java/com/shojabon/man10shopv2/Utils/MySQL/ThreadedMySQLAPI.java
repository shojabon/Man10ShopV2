package com.shojabon.man10shopv2.Utils.MySQL;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.concurrent.*;

public class ThreadedMySQLAPI {

    ExecutorService threadPool = Executors.newCachedThreadPool();

    JavaPlugin plugin;
    public ThreadedMySQLAPI(JavaPlugin plugin){
        this.plugin = plugin;
    }

    public Future<ArrayList<MySQLCachedResultSet>> futureQuery(String query){
        return threadPool.submit(()-> new MySQLAPI(plugin).cachedQuery(query));
    }

    public Future<Boolean> futureExecute(String query){
        return threadPool.submit(()-> new MySQLAPI(plugin).execute(query));
    }

    public ArrayList<MySQLCachedResultSet> query(String query){
        try {
            return futureQuery(query).get();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean execute(String query){
        try {
            return futureExecute(query).get();
        } catch (Exception e) {
            return false;
        }
    }


}
