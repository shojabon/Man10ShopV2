package com.shojabon.man10shopv2.dataClass;

import com.shojabon.man10shopv2.enums.Man10ShopPermission;

import java.util.UUID;

public class Man10ShopModerator {

    public UUID uuid;
    public String name;
    public Man10ShopPermission permission;
    public boolean notificationEnabled = false;

    public Man10ShopModerator(String name, UUID uuid, Man10ShopPermission permission, boolean notificationEnabled){
        this.name = name;
        this.uuid = uuid;
        this.permission = permission;
        this.notificationEnabled = notificationEnabled;
    }

}
