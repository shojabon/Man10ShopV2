package com.shojabon.man10shopv2.DataClass.ShopFunctions;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.Man10ShopModerator;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Utils.MySQL.MySQLAPI;
import com.shojabon.man10shopv2.Utils.MySQL.MySQLCachedResultSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PermissionFunction extends ShopFunction {
    //variables
    private HashMap<UUID, Man10ShopModerator> moderators = new HashMap<>();

    public ArrayList<Man10ShopModerator> getModerators(){
        return new ArrayList<>(moderators.values());
    }

    //init

    public PermissionFunction(Man10Shop shop) {
        super(shop);
        loadPermissions();
    }

    public void loadPermissions(){
        ArrayList<MySQLCachedResultSet> results = Man10ShopV2.mysql.query("SELECT * FROM man10shop_permissions WHERE shop_id = '" + shop.getShopId() + "'");
        for(MySQLCachedResultSet rs: results) {
            UUID uuid = UUID.fromString(rs.getString("uuid"));
            Man10ShopModerator permission = new Man10ShopModerator(rs.getString("name"), uuid, Man10ShopPermission.valueOf(rs.getString("permission")), rs.getBoolean("notification"));
            moderators.put(uuid, permission);
        }
    }

    // mod user functions

    public boolean addModerator(Man10ShopModerator moderator){
        Man10ShopV2.mysql.execute("DELETE FROM man10shop_permissions WHERE shop_id ='" + shop.getShopId() + "' AND uuid = '" + moderator.uuid + "'");

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("name", moderator.name);
        payload.put("uuid", moderator.uuid.toString());
        payload.put("shop_id", shop.getShopId().toString());
        payload.put("permission", moderator.permission.name());
        payload.put("notification", moderator.notificationEnabled);
        if(!Man10ShopV2.mysql.execute(MySQLAPI.buildInsertQuery(payload, "man10shop_permissions"))) return false;
        moderators.put(moderator.uuid, moderator);
        Man10ShopV2API.userModeratingShopList.remove(moderator.uuid);
        return true;
    }

    public boolean setModerator(Man10ShopModerator moderator){
        Man10ShopV2.mysql.execute("DELETE FROM man10shop_permissions WHERE shop_id ='" + shop.getShopId() + "' AND uuid = '" + moderator.uuid + "'");

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("name", moderator.name);
        payload.put("uuid", moderator.uuid.toString());
        payload.put("shop_id", shop.getShopId().toString());
        payload.put("permission", moderator.permission.name());
        payload.put("notification", moderator.notificationEnabled);
        if(!Man10ShopV2.mysql.execute(MySQLAPI.buildInsertQuery(payload, "man10shop_permissions"))) return false;
        moderators.put(moderator.uuid, moderator);
        Man10ShopV2API.userModeratingShopList.remove(moderator.uuid);
        return true;
    }

    public boolean removeModerator(Man10ShopModerator moderator){
        boolean result = Man10ShopV2.mysql.execute("DELETE FROM man10shop_permissions WHERE shop_id ='" + shop.getShopId() + "' AND uuid = '" + moderator.uuid + "'");
        if(!result) return false;
        moderators.remove(moderator.uuid);
        Man10ShopV2API.userModeratingShopList.remove(moderator.uuid);
        return true;
    }

    public boolean isModerator(UUID uuid){
        return moderators.containsKey(uuid);
    }

    //permission calculation functions

    public Man10ShopPermission getPermission(UUID uuid){
        //admin mode admin
        if(shop.isAdminShop()){
            Player targetPlayer = Bukkit.getPlayer(uuid);
            if(targetPlayer != null && targetPlayer.isOnline()){
                if(targetPlayer.hasPermission("man10shopv2.admin")) return Man10ShopPermission.OWNER;
            }
        }

        if(!moderators.containsKey(uuid)) return null;
        return moderators.get(uuid).permission;
    }

    public boolean hasPermissionAtLeast(UUID uuid, Man10ShopPermission permission){
        Man10ShopPermission actualPerm = getPermission(uuid);
        if(actualPerm == null) return false;

        int userPermissionLevel = calculatePermissionLevel(actualPerm);

        int requiredPermissionLevel = calculatePermissionLevel(permission);
        return userPermissionLevel >= requiredPermissionLevel;
    }

    public boolean hasPermission(UUID uuid, Man10ShopPermission permission){
        Man10ShopPermission actualPerm = getPermission(uuid);
        if(actualPerm == null) return false;
        return actualPerm == permission;
    }

    private int calculatePermissionLevel(Man10ShopPermission permission){
        int permissionLevel = 0;
        switch (permission){
            case OWNER: permissionLevel = 10; break;
            case MODERATOR: permissionLevel = 9; break;
            case ACCOUNTANT: permissionLevel = 7; break;
            case STORAGE_ACCESS: permissionLevel = 7; break;
        }
        return permissionLevel;
    }

    //utils
    public int totalOwnerCount(){
        if(shop.isAdminShop()) return 1;
        int result = 0;
        for(Man10ShopModerator mod: moderators.values()){
            if(mod.permission == Man10ShopPermission.OWNER) result ++;
        }
        return result;
    }

    public String getPermissionString(Man10ShopPermission permission){
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

    //notification

    public boolean setEnableNotification(Man10ShopModerator mod, boolean enabled){
        if(!moderators.containsKey(mod.uuid)){
            return false;
        }
        if(mod.notificationEnabled == enabled) return true;
        mod.notificationEnabled = enabled;
        moderators.put(mod.uuid, mod);
        if(!Man10ShopV2.mysql.execute("UPDATE man10shop_permissions SET notification = '" + enabled + "' WHERE shop_id = '" + shop.getShopId() + "' AND uuid = '" + mod.uuid + "'")){
            return false;
        }
        return true;
    }

    public void notifyModerators(int amount){
        for(Man10ShopModerator mod: moderators.values()){
            if(!mod.notificationEnabled) continue;
            Player p = Bukkit.getServer().getPlayer(mod.uuid);
            if(p == null) continue;
            if(!p.isOnline()) continue;
            p.sendMessage(Man10ShopV2.prefix + "§a§l" + shop.getShopName() + "で" + amount + "個のアイテム取引がありました");
        }
    }

}
