package com.shojabon.man10shopv2.dataClass.lootBox;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Random;

public class LootBoxGroupData {

    public Material icon;
    public int percentageWeight;
    public boolean bigWin;

    public HashMap<String, Integer> itemCountDictionary = new HashMap<>();

    public LootBoxGroupData(Material icon, int percentageWeight) {
        this.icon = icon;
        this.percentageWeight = percentageWeight;
    }

    public float getPercentage() {
        return this.percentageWeight / 10000f * 100;
    }

    public int getTotalItemCount(){
        int total = 0;
        for(int count: itemCountDictionary.values()){
            total += count;
        }
        return total;
    }

    public String pickRandomItemHash(){
        Random rand = new Random();
        int result = rand.nextInt(getTotalItemCount()) + 1;
        int currentCompound = 0;
        for(String itemHash: itemCountDictionary.keySet()){
            currentCompound += itemCountDictionary.get(itemHash);
            if(result <= currentCompound){
                return itemHash;
            }
        }
        return null;
    }




}
