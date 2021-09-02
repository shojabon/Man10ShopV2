package com.shojabon.man10shopv2.DataClass;

import com.shojabon.man10shopv2.Enums.Man10ShopPermission;

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

    public String getPermissionString(){
        switch (permission){
            case OWNER:
                return "オーナー";
            case MODERATOR:
                return "管理者";
            case ACCOUNTANT:
                return "会計";
            case STORAGE_ACCESS:
                return "倉庫編集権";
        }
        return "エラー";
    }

}
