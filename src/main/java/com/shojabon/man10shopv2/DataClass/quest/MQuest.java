package com.shojabon.man10shopv2.DataClass.quest;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class MQuest {

    public ArrayList<MQuestGroupData> groupData = new ArrayList<>();
    public ArrayList<UUID> currentQuests = new ArrayList<>();

    public int getLackingWeight(){
        return 10000 - getTotalWeight();
    }

    public int getTotalWeight(){
        int total = 0;
        for(MQuestGroupData data: groupData){
            total += data.percentageWeight;
        }
        return total;
    }

    public boolean isAvailable(){
        if(getLackingWeight() != 0) return false;
        for(MQuestGroupData data: groupData){
            if(data.getTotalItemCount() == 0) return false;
        }
        return true;
    }

    public ArrayList<UUID> getQuests(int questCount){
        ArrayList<UUID> resultShopIDs = new ArrayList<>();
        if(!isAvailable()) return resultShopIDs;

        for(int i = 0; i < questCount; i++){

            if(groupData.size() == 0) break;

            Random rand = new Random();

            int totalWeight = 0;
            for(MQuestGroupData localData: groupData){
                if(localData.temporaryDisabled) continue;
                totalWeight += localData.percentageWeight;
            }


            int result = rand.nextInt(totalWeight)+1;
            int currentCompound = 0;
            for(MQuestGroupData data: groupData){
                if(data.temporaryDisabled) continue;
                currentCompound += data.percentageWeight;
                if(result <= currentCompound){
                    UUID pickedShopId = data.pickRandomShopInGroup();
                    data.temporaryIgnored.add(pickedShopId);
                    if(data.shopCountDictionary.size() == data.temporaryIgnored.size()) data.temporaryDisabled = true;
                    resultShopIDs.add(pickedShopId);
                    break;
                }
            }

        }
        for(MQuestGroupData data: groupData){
            data.temporaryDisabled = false;
            data.temporaryIgnored.clear();
        }
        return resultShopIDs;

    }

    public YamlConfiguration exportQuest(){
        YamlConfiguration config = new YamlConfiguration();

        //export group
        for(int i = 0; i < groupData.size(); i++){
            config.set("group." + i + ".material", groupData.get(i).icon.toString());
            config.set("group." + i + ".percentageWeight", groupData.get(i).percentageWeight);

            //export item count
            for(UUID shopId: groupData.get(i).shopCountDictionary.keySet()){
                config.set("group." + i + ".itemCount." + shopId, groupData.get(i).shopCountDictionary.get(shopId));
            }
        }
        return config;
    }

    public void loadLootBox(YamlConfiguration config){
        //load group
        ConfigurationSection groupSection = config.getConfigurationSection("group");
        if(groupSection == null) return;
        for(int i = 0; i < groupSection.getKeys(false).size(); i++){

            MQuestGroupData data = new MQuestGroupData(Material.valueOf(groupSection.getString(i + ".material")), groupSection.getInt(i + ".percentageWeight"));

            //load item count
            ConfigurationSection itemHashSection = groupSection.getConfigurationSection(i + ".itemCount");
            if(itemHashSection != null){
                ArrayList<String> hash = new ArrayList<>(itemHashSection.getKeys(false));
                for(String shopId: hash){
                    data.shopCountDictionary.put(UUID.fromString(shopId), itemHashSection.getInt(shopId));
                }
            }
            groupData.add(data);
        }

    }

}

