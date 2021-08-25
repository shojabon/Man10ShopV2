package com.shojabon.man10shopv2.Menus.Shop;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.Man10ShopModerator;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PermissionSettingsMenu {

    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;
    Man10ShopModerator target;

    Man10ShopPermission[] permissions = new Man10ShopPermission[]{Man10ShopPermission.OWNER,
            Man10ShopPermission.MODERATOR,
            Man10ShopPermission.ACCOUNTANT,
            Man10ShopPermission.STORAGE_ACCESS};
    int[] slots = new int[]{20, 21, 22, 23};

    int deleteUserSlot = 25;

    public PermissionSettingsMenu(Player p, Man10Shop shop, Man10ShopModerator target, Man10ShopV2 plugin){
        this.player = p;
        this.shop = shop;
        this.plugin = plugin;
        this.target = target;
    }

    public void renderSelector(SInventory inventory){
        for(int i = 0; i < slots.length; i++){
            if(shop.hasPermissionAtLeast(target.uuid, permissions[i])){
               if(shop.hasPermission(target.uuid, permissions[i])){
                   //has permission and is current permission
                   SInventoryItem current = new SInventoryItem(new SItemStack(Material.LIME_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().green().bold().text("現在の権限").build()).build());
                   current.clickable(false);
                   inventory.setItem(slots[i], current);
               }else{
                   //has permission but not this permission
                   SInventoryItem background = new SInventoryItem(new SItemStack(Material.RED_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().red().bold().text("この権限に設定する").build()).build());
                   background.clickable(false);
                   inventory.setItem(slots[i], background);
               }
            }else{
                // no permission
                SInventoryItem notAllowed = new SInventoryItem(new SItemStack(Material.BARRIER).setDisplayName(new SStringBuilder().gray().bold().text("この権限に設定することはできません").build()).build());
                notAllowed.clickable(false);
                inventory.setItem(slots[i], notAllowed);
            }
        }

    }

    public void renderIcons(SInventory inventory){

        SInventoryItem owner = new SInventoryItem(new SItemStack(Material.DIAMOND_BLOCK).setDisplayName(new SStringBuilder().aqua().bold().text("オーナー権限").build()).build());
        owner.clickable(false);
        inventory.setItem(slots[0]-9, owner);

        SInventoryItem moderator = new SInventoryItem(new SItemStack(Material.GOLD_BLOCK).setDisplayName(new SStringBuilder().gold().bold().text("管理者権限").build()).build());
        moderator.clickable(false);
        inventory.setItem(slots[1]-9, moderator);

        SInventoryItem accountant = new SInventoryItem(new SItemStack(Material.EMERALD_BLOCK).setDisplayName(new SStringBuilder().green().bold().text("会計権限").build()).build());
        accountant.clickable(false);
        inventory.setItem(slots[2]-9, accountant);

        SInventoryItem storageAccess = new SInventoryItem(new SItemStack(Material.CHEST).setDisplayName(new SStringBuilder().yellow().bold().text("倉庫編集権限").build()).build());
        storageAccess.clickable(false);
        inventory.setItem(slots[3]-9, storageAccess);



        SInventoryItem deleteUser = new SInventoryItem(new SItemStack(Material.LAVA_BUCKET).setDisplayName(new SStringBuilder().yellow().obfuscated().text("OO")
                .darkRed().bold().text("ユーザーを削除")
                .yellow().obfuscated().text("OO")
                .build()).build());

        deleteUser.clickable(false);
        inventory.setItem(deleteUserSlot, deleteUser);


    }

    public SInventory renderInventory(){
        SInventory inventory = new SInventory(new SStringBuilder().red().text(target.name).text("の権限設定").build(), 4, plugin);

        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        inventory.fillItem(background);

        renderSelector(inventory);
        renderIcons(inventory);
        return inventory;
    }

}
