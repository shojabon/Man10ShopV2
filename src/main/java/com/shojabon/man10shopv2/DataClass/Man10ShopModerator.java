package com.shojabon.man10shopv2.DataClass;

import com.shojabon.man10shopv2.Enums.Man10ShopPermission;

import java.util.UUID;

public class Man10ShopModerator {

    public UUID uuid;
    public String name;
    public Man10ShopPermission permission;

    public Man10ShopModerator(String name, UUID uuid, Man10ShopPermission permission){
        this.name = name;
        this.uuid = uuid;
        this.permission = permission;
    }

}
