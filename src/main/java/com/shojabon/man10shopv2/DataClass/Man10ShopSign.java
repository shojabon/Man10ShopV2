package com.shojabon.man10shopv2.DataClass;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class Man10ShopSign {

    public UUID shopId;
    public String world;
    public int x;
    public int y;
    public int z;

    public Man10ShopSign(UUID shopId, String world, int x, int y, int z){
        this.shopId = shopId;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String generateLocationId(){
        return world + "|" + x + "|" + y + "|" + z;
    }

    public Location getLocation(){
        World w = Bukkit.getServer().getWorld(world);
        if(w == null) return null;
        return new Location(w,x, y, z);
    }
}
