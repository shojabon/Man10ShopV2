package com.shojabon.man10shopv2.DataClass;

import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Utils.MySQL.MySQLAPI;
import com.shojabon.man10shopv2.Utils.MySQL.MySQLCachedResultSet;
import com.shojabon.man10shopv2.Utils.SItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Man10Shop {

    public String name;
    public UUID shopId;
    public int storageSize;
    public int storageCap;
    public int itemCount;
    public int price;

    public int money;

    public SItemStack targetItem;
    public int targetItemCount;
    public ItemStack icon;
    public Man10ShopType shopType;

    public HashMap<UUID, Man10ShopModerator> moderators = new HashMap<>();
    public ArrayList<Man10ShopSign> signs = new ArrayList<>();

    public Man10Shop(UUID shopId,
                     String name,
                     int storageSize,
                     int storageCap,
                     int itemCount,
                     int price,
                     int money,
                     SItemStack targetItem,
                     int targetItemCount,
                     Man10ShopType shopType){

        if(targetItem == null){
            targetItem = new SItemStack(Material.DIAMOND);
        }
        this.money = money;
        this.price = price;
        this.shopId = shopId;
        this.name = name;
        this.itemCount = itemCount;
        this.storageCap = storageCap;
        this.storageSize = storageSize;
        this.targetItem = targetItem;
        this.targetItemCount = targetItemCount;
        this.icon = new ItemStack(targetItem.getType());
        this.shopType = shopType;

        loadPermissions();
    }


    //storage

    public boolean removeItemCount(int count){
        boolean result = Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET item_count = item_count - " + count + " WHERE shop_id = '" + shopId + "'");
        if(!result) return false;
        itemCount = itemCount - count;
        //log here
        return true;
    }

    public boolean addItemCount(int count){
        boolean result = Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET item_count = item_count + " + count + " WHERE shop_id = '" + shopId + "'");
        if(!result) return false;
        itemCount = itemCount + count;
        //log here
        return true;
    }

    //money storage

    public boolean addMoney(int value){
        boolean result = Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET money = money + " + value + " WHERE shop_id = '" + shopId + "'");
        if(!result) return false;
        money = money + value;
        //log here
        return true;
    }

    public boolean removeMoney(int value){
        boolean result = Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET money = money - " + value + " WHERE shop_id = '" + shopId + "'");
        if(!result) return false;
        money = money - value;
        //log here
        return true;
    }

    //itemstack setting

    public boolean setTargetItem(Player p, ItemStack item){
        if(!hasPermissionAtLeast(p.getUniqueId(), Man10ShopPermission.MODERATOR)){
            p.sendMessage(Man10ShopV2.prefix + "§c§lあなたにはこの項目を設定する権限がありません");
            return false;
        }
        SItemStack sItem = new SItemStack(item);
        if(!sItem.getItemTypeMD5().equals(targetItem.getItemTypeMD5()) && itemCount != 0){
            p.sendMessage(Man10ShopV2.prefix + "§c§lショップ在庫があるときは取引アイテムを変更することはできません");
            return false;
        }
        return setTargetItem(item);
    }

    public boolean setTargetItem(ItemStack item){
        SItemStack sItem = new SItemStack(item);
        boolean result = Man10ShopV2.mysql.execute("UPDATE man10shop_shops SET target_item = '" + sItem.getItemTypeBase64() + "', target_item_hash ='" + sItem.getItemTypeMD5() + "' WHERE shop_id = '" + shopId + "'");
        if(!result) return false;
        targetItem = sItem;
        //log here
        return true;
    }

    //permissions settings

    public Man10ShopPermission getPermission(UUID uuid){
        if(!moderators.containsKey(uuid)) return null;
        return moderators.get(uuid).permission;
    }

    public boolean hasPermissionAtLeast(UUID uuid, Man10ShopPermission permission){
        if(!moderators.containsKey(uuid)){
            return false;
        }
        Man10ShopPermission actualPerm = moderators.get(uuid).permission;
        int userPermissionLevel = calculatePermissionLevel(actualPerm);
        int requiredPermissionLevel = calculatePermissionLevel(permission);
        return userPermissionLevel >= requiredPermissionLevel;
    }

    public boolean hasPermission(UUID uuid, Man10ShopPermission permission){
        if(!moderators.containsKey(uuid)){
            return false;
        }
        Man10ShopPermission actualPerm = moderators.get(uuid).permission;
        return actualPerm == permission;
    }

    private int calculatePermissionLevel(Man10ShopPermission permission){
        int permissionLevel = 0;
        switch (permission){
            case OWNER: permissionLevel = 10; break;
            case MODERATOR: permissionLevel = 9; break;
            case ACCOUNTANT: permissionLevel = 8; break;
            case STORAGE_ACCESS: permissionLevel = 7; break;
        }
        return permissionLevel;
    }

    public boolean addModerator(Player p, Man10ShopPermission permissionType){
        Man10ShopModerator permission = new Man10ShopModerator(p.getName(), p.getUniqueId(), permissionType);
        moderators.put(p.getUniqueId(), permission);
        Man10ShopV2.mysql.execute("DELETE FROM man10shop_permissions WHERE shop_id ='" + shopId + "' AND uuid = '" + p.getUniqueId() + "'");

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("name", p.getName());
        payload.put("uuid", p.getUniqueId().toString());
        payload.put("shop_id", shopId);
        payload.put("permission", Man10ShopPermission.OWNER.name());
        return Man10ShopV2.mysql.execute(MySQLAPI.buildInsertQuery(payload, "man10shop_permissions"));
    }

    public boolean removeModerator(Player p){
        boolean result = Man10ShopV2.mysql.execute("DELETE FROM man10shop_permissions WHERE shop_id ='" + shopId + "' AND uuid = '" + p.getUniqueId() + "'");
        if(!result) return false;
        moderators.remove(p.getUniqueId());
        return true;
    }

    public void loadPermissions(){
        ArrayList<MySQLCachedResultSet> results = Man10ShopV2.mysql.query("SELECT * FROM man10shop_permissions WHERE shop_id = '" + shopId + "'");
        for(MySQLCachedResultSet rs: results) {
            UUID uuid = UUID.fromString(rs.getString("uuid"));
            Man10ShopModerator permission = new Man10ShopModerator(rs.getString("name"), uuid, Man10ShopPermission.valueOf(rs.getString("permission")));
            moderators.put(uuid, permission);
        }
    }

    //actions

    public void performAction(Player p, int amount){
        if(shopType == Man10ShopType.BUY){
            if(itemCount <= 0){
                p.sendMessage(Man10ShopV2.prefix + "§c§l在庫がありません");
                return;
            }
            if(p.getInventory().firstEmpty() == -1){
                p.sendMessage(Man10ShopV2.prefix + "§c§lインベントリに空きがありません");
                return;
            }
            if(amount > itemCount){
                amount = itemCount;
            }
            int totalPrice = price*amount;
            if(Man10ShopV2.vault.getBalance(p.getUniqueId()) < totalPrice){
              p.sendMessage(Man10ShopV2.prefix + "§c§l残高が不足しています");
              return;
            }
            if(!Man10ShopV2.vault.withdraw(p.getUniqueId(), totalPrice)){
                p.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return;
            }
            addMoney(totalPrice);
            //remove items from shop storage

            SItemStack item = new SItemStack(targetItem.build().clone());
            boolean removeItemResult =  removeItemCount(amount*item.getAmount());
            if(!removeItemResult){
                p.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return;
            }

            for(int i = 0; i < amount; i++){
                p.getInventory().addItem(item.build());
            }

            p.sendMessage(Man10ShopV2.prefix + "§a§l" + item.getDisplayName() + "を" + amount*item.getAmount() + "個購入しました");

        }else if(shopType == Man10ShopType.SELL){
            if(itemCount > storageCap){
                p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップは現在買取を行っていません");
                return;
            }
            SItemStack item = new SItemStack(targetItem.build().clone());
            if(!p.getInventory().containsAtLeast(targetItem.build(), amount*item.getAmount())){
                p.sendMessage(Man10ShopV2.prefix + "§c§l買い取るためのアイテムを持っていません");
                return;
            }
            int totalPrice = price*amount;
            if(totalPrice > money){
                p.sendMessage(Man10ShopV2.prefix + "§c§lこのショップの現金が不足しています");
                return;
            }
            if(!removeMoney(totalPrice)){
                p.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return;
            }
            Man10ShopV2.vault.deposit(p.getUniqueId(), totalPrice);
            //remove items from shop storage
            boolean addItemResult =  addItemCount(amount);
            if(!addItemResult){
                p.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                return;
            }

            //perform transaction
            for( int i =0; i < amount; i++){
                p.getInventory().removeItemAnySlot(item.build());
            }

            p.sendMessage(Man10ShopV2.prefix + "§a§l" + item.getDisplayName() + "を" + amount*item.getAmount() + "個売却しました");
        }else if(shopType == Man10ShopType.STOPPED){
            p.sendMessage(Man10ShopV2.prefix + "§a§lこのショップは現在取引を停止しています");
        }
    }


}
