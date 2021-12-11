package com.shojabon.man10shopv2.dataClass;

import com.shojabon.man10shopv2.Man10ShopV2;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class LootBoxFunction extends ShopFunction{

    public LootBoxFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }

    public void afterLootBoxSpinFinished(Player player, ItemStack item, int groupId){}
}
