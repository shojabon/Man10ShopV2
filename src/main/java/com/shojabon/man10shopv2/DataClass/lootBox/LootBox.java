package com.shojabon.man10shopv2.DataClass.lootBox;

import com.shojabon.mcutils.Utils.SItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class LootBox {

    public HashMap<String, ItemStack> itemDictionary = new HashMap<>();
    public ArrayList<LootBoxGroupData> groupData = new ArrayList<>();

    public int getLackingWeight(){
        int total = 0;
        for(LootBoxGroupData data: groupData){
            total += data.percentageWeight;
        }
        return 10000 - total;
    }

    public boolean canPlay(){
        if(getLackingWeight() != 0) return false;
        for(LootBoxGroupData data: groupData){
            if(data.getTotalItemCount() == 0) return false;
        }
        return true;
    }

    public ItemStack pickRandomItem(){
        if(!canPlay()) return null;
        Random rand = new Random();
        int result = rand.nextInt(10000)+1;
        int currentCompound = 0;
        for(LootBoxGroupData data: groupData){
            currentCompound += data.percentageWeight;
            if(result <= currentCompound){
                return itemDictionary.get(data.pickRandomItemHash());
            }
        }
        return null;
    }


    public YamlConfiguration exportLootBox(){
        YamlConfiguration config = new YamlConfiguration();

        //export group
        for(int i = 0; i < groupData.size(); i++){
            config.set("group." + i + ".material", groupData.get(i).icon.toString());
            config.set("group." + i + ".percentageWeight", groupData.get(i).percentageWeight);

            //export item count
            for(String itemHash: groupData.get(i).itemCountDictionary.keySet()){
                config.set("group." + i + ".itemCount." + itemHash, groupData.get(i).itemCountDictionary.get(itemHash));
            }
        }

        //export dictionary
        for(String itemHash: itemDictionary.keySet()){
            config.set("itemDictionary." + itemHash, new SItemStack(itemDictionary.get(itemHash)).getBase64());
        }
        return config;
    }

    public void loadLootBox(YamlConfiguration config){
        //load group
        ConfigurationSection groupSection = config.getConfigurationSection("group");
        if(groupSection == null) return;
        for(int i = 0; i < groupSection.getKeys(false).size(); i++){

            LootBoxGroupData data = new LootBoxGroupData(Material.valueOf(groupSection.getString(i + ".material")), groupSection.getInt(i + ".percentageWeight"));

            //load item count
            ConfigurationSection itemHashSection = groupSection.getConfigurationSection(i + ".itemCount");
            if(itemHashSection != null){
                ArrayList<String> hash = new ArrayList<>(itemHashSection.getKeys(false));
                for(String itemHash: hash){
                    data.itemCountDictionary.put(itemHash, itemHashSection.getInt(itemHash));
                }
            }
            groupData.add(data);
        }

        //load item dictionary
        ConfigurationSection itemDictionarySection = config.getConfigurationSection("itemDictionary.");
        if(itemDictionarySection == null) return;
        ArrayList<String> md5 = new ArrayList<>(itemDictionarySection.getKeys(false));
        for (String s : md5) {
            itemDictionary.put(s, SItemStack.fromBase64(itemDictionarySection.getString(s)).build());
        }
    }

}

