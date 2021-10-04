package com.shojabon.man10shopv2.DataClass;

import org.bukkit.entity.Player;

import java.util.UUID;

public class Man10ShopOrder {
    public Player player;
    public UUID shopId;
    public int amount;

    public Man10ShopOrder(Player player, UUID shopId, int amount){
        this.player = player;
        this.shopId = shopId;
        this.amount = amount;
    }
}
