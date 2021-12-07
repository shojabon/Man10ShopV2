package com.shojabon.man10shopv2.DataClass.lootBox;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Random;

public class LootBoxItem {

    public ItemStack item;
    public int groupId;

    public LootBoxItem(ItemStack item, int groupId) {
        this.item = item;
        this.groupId = groupId;
    }




}
