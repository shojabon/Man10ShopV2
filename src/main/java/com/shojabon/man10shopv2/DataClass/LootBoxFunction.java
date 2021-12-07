package com.shojabon.man10shopv2.DataClass;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class LootBoxFunction extends ShopFunction{

    public LootBoxFunction(Man10Shop shop) {
        super(shop);
    }

    public void afterLootBoxSpinFinished(Player player, ItemStack item, int groupId){}
}
