package com.shojabon.man10shopv2.enums;

import org.bukkit.Material;

public enum Man10ShopType {
    BUY("販売ショップ", false, Material.DROPPER),
    SELL("買取ショップ", false, Material.HOPPER),
    BARTER("トレードショップ", true, Material.VILLAGER_SPAWN_EGG),
    LOOT_BOX("ガチャ", true, Material.CHEST),
    QUEST("クエストショップ", true, Material.OAK_SIGN);

    public String displayName;
    public boolean admin;
    public Material settingItem;

    Man10ShopType(String displayName, boolean admin, Material settingItem){
        this.displayName = displayName;
        this.admin = admin;
        this.settingItem = settingItem;
    }

}
