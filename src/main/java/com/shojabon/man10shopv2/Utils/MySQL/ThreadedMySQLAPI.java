package com.shojabon.man10shopv2.Utils.MySQL;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class ThreadedMySQLAPI {

    ExecutorService threadPool = Executors.newCachedThreadPool();

    //synced query
    LinkedBlockingQueue<SyncedMySQLRequest> syncedQueue = new LinkedBlockingQueue<>();
    Thread syncedThread = new Thread(() -> {
        while(true){
            try {
                SyncedMySQLRequest request = syncedQueue.take();
                if(request.isQuery()){
                    request.queryCallback.accept(query(request.query));
                }else{
                    request.executeCallback.accept(execute(request.query));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    });

    JavaPlugin plugin;
    public ThreadedMySQLAPI(JavaPlugin plugin){
        this.plugin = plugin;
        if(!syncedThread.isAlive()){
            syncedThread.start();
        }
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

    public void asyncQuery(String query, Consumer<ArrayList<MySQLCachedResultSet>> callback){
        threadPool.submit(() -> callback.accept(query(query)));
    }

    public void asyncExecute(String query, Consumer<Boolean> callback){
        threadPool.submit(() -> callback.accept(execute(query)));
    }

    public void syncedFutureQuery(String query, Consumer<ArrayList<MySQLCachedResultSet>> callback){
        SyncedMySQLRequest request = new SyncedMySQLRequest();
        request.queryCallback = callback;
        request.query = query;
        syncedQueue.add(request);
    }

    public void syncedFutureExecute(String query, Consumer<Boolean> callback){
        SyncedMySQLRequest request = new SyncedMySQLRequest();
        request.executeCallback = callback;
        request.query = query;
        syncedQueue.add(request);
    }


}
