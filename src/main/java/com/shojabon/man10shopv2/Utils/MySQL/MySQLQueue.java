package com.shojabon.man10shopv2.Utils.MySQL;


import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class MySQLQueue {

    int executePool;
    int queryPool;
    JavaPlugin plugin;

    ArrayList<LinkedBlockingQueue<ThreadedQueryRequest>> executeQueue = new ArrayList<>();
    ArrayList<LinkedBlockingQueue<ThreadedQueryRequest>> queryQueue = new ArrayList<>();

    ConcurrentHashMap<String, ArrayList<MySQLCachedResultSet>> queryResponse = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, Boolean> executeResult = new ConcurrentHashMap<>();

    public final String queryChecker = "";
    public final String executeChecker = "";

    Timer timer = new Timer();
    int nextExecuteThread = 0;
    int nextQueryThread = 0;

    public MySQLQueue(int executePool, int queryPool, JavaPlugin plugin){
        this.executePool = executePool;
        this.queryPool = queryPool;
        this.plugin = plugin;

        for(int i = 0; i < executePool; i++){
            executeQueue.add(new LinkedBlockingQueue<>());
            startExecuteThread(i);
        }

        for(int i = 0; i < queryPool; i++){
            queryQueue.add(new LinkedBlockingQueue<>());
            startQueryThread(i);
        }
    }

    public ArrayList<MySQLCachedResultSet> query(String query){
        String responseId = UUID.randomUUID().toString();
        ThreadedQueryRequest request = new ThreadedQueryRequest(query, responseId);
        queryQueue.get(nextQueryThread).add(request);
        nextQueryThread += 1;
        if(nextQueryThread > queryPool-1){
            nextQueryThread = 0;
        }
        try {
            synchronized (queryChecker){
                while(!queryResponse.containsKey(responseId)){
                    queryChecker.wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ArrayList<MySQLCachedResultSet> result = queryResponse.get(responseId);
        queryResponse.remove(responseId);
        return result;
    }

    public synchronized boolean execute(String query){
        String responseId = UUID.randomUUID().toString();
        ThreadedQueryRequest request = new ThreadedQueryRequest(query, responseId);
        executeQueue.get(nextExecuteThread).add(request);
        nextExecuteThread += 1;
        if(nextExecuteThread > executePool-1){
            nextExecuteThread = 0;
        }
        try {
            synchronized (executeChecker){
                while(!executeResult.containsKey(responseId)){
                    executeChecker.wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean result = executeResult.get(responseId);
        executeResult.remove(responseId);
        return result;
    }

    public synchronized void stop(){
        for(LinkedBlockingQueue<ThreadedQueryRequest> q: executeQueue){
            q.add(new ThreadedQueryRequest("quit", ""));
        }

        for(LinkedBlockingQueue<ThreadedQueryRequest> q: queryQueue){
            q.add(new ThreadedQueryRequest("quit", ""));
        }
    }

    private void startExecuteThread(int id){
        new Thread(()->{
            MySQLAPI manager = new MySQLAPI(plugin);
            while(true){
                try {
                    ThreadedQueryRequest take = executeQueue.get(id).take();
                    if(take.query.equalsIgnoreCase("quit")){
                        manager.close();
                        break;
                    }
                    executeResult.putIfAbsent(take.id, manager.execute(take.query));
                    synchronized (executeChecker){
                        executeChecker.notifyAll();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                executeResult.remove(take.id);
                            }
                        }, 10000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startQueryThread(int id){
        new Thread(()->{
            MySQLAPI manager = new MySQLAPI(plugin);
            while(true){
                try {
                    ThreadedQueryRequest take = queryQueue.get(id).take();
                    if(take.query.equalsIgnoreCase("quit")){
                        break;
                    }
                    ArrayList<MySQLCachedResultSet> resultSets = manager.cachedQuery(take.query);
                    queryResponse.putIfAbsent(take.id, resultSets);
                    synchronized (queryChecker){
                        queryChecker.notifyAll();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                queryResponse.remove(take.id);
                            }
                        }, 10000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
