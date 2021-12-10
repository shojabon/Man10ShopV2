package com.shojabon.man10shopv2.DataClass.quest;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class MQuestGroupData {

    public Material icon;
    public int percentageWeight;

    //calculating
    public boolean temporaryDisabled = false;
    public ArrayList<UUID> temporaryIgnored = new ArrayList<>();


    public HashMap<UUID, Integer> shopCountDictionary = new HashMap<>();

    public MQuestGroupData(Material icon, int percentageWeight) {
        this.icon = icon;
        this.percentageWeight = percentageWeight;
    }

    public float getPercentage() {
        return this.percentageWeight / 10000f * 100;
    }

    public int getTotalItemCount(){
        int total = 0;
        for(UUID shopId: shopCountDictionary.keySet()){
            if(temporaryIgnored.contains(shopId)) continue;
            total += shopCountDictionary.get(shopId);
        }
        return total;
    }

    public UUID pickRandomShopInGroup(){
        Random rand = new Random();
        int result = rand.nextInt(getTotalItemCount()) + 1;
        int currentCompound = 0;
        for(UUID shop: shopCountDictionary.keySet()){
            if(temporaryIgnored.contains(shop)) continue;
            currentCompound += shopCountDictionary.get(shop);
            if(result <= currentCompound){
                return shop;
            }
        }
        return null;
    }




}
