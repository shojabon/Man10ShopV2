package com.shojabon.man10shopv2.Menus.Shop.Permission;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.Man10ShopModerator;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.ConfirmationMenu;
import com.shojabon.man10shopv2.Utils.MySQL.MySQLAPI;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

public class PermissionSettingsMenu {

    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;
    Man10ShopModerator target;
    SInventory inventory;

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

        renderInventory();
    }

    public Consumer<InventoryClickEvent> generateClickChangePermissionEvent(){
        return e -> {
            for(int i = 0; i < slots.length; i++){
                if(e.getRawSlot() != slots[i]) continue;
                if(permissions[i] == target.permission) continue;
                if(target.permission == Man10ShopPermission.OWNER && shop.ownerCount() == 1){
                    player.sendMessage(Man10ShopV2.prefix + "§c§lオーナーは最低一人必要です");
                    return;
                }

                //permission change confirmation
                ConfirmationMenu menu = new ConfirmationMenu("確認", plugin);
                int finalI = i;
                menu.setOnConfirm(ee -> {
                    if(!shop.setModerator(target)){
                        player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                        return;
                    }
                    target.permission = permissions[finalI];
                    player.sendMessage(Man10ShopV2.prefix + "§a§l権限を設定しました");
                    menu.getInventory().moveToMenu(player, new PermissionSettingsMenu(player, shop, target, plugin).getInventory());
                });

                menu.setOnCancel(ee -> menu.getInventory().moveToMenu(player, new PermissionSettingsMenu(player, shop, target, plugin).getInventory()));
                menu.setOnClose(ee -> menu.getInventory().moveToMenu(player, new PermissionSettingsMenu(player, shop, target, plugin).getInventory()));

                //open confirmation
                inventory.moveToMenu(player, menu.getInventory());
                return;
            }
        };
    }

    public void renderSelector(){
        for(int i = 0; i < slots.length; i++){
            if(shop.hasPermissionAtLeast(player.getUniqueId(), permissions[i])){
                if(shop.hasPermission(target.uuid, permissions[i])){
                    //has permission and is current permission
                    SInventoryItem current = new SInventoryItem(new SItemStack(Material.LIME_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().green().bold().text("現在の権限").build()).build());
                    current.clickable(false);
                    inventory.setItem(slots[i], current);
                }else{
                    //has permission but not this permission
                    SInventoryItem background = new SInventoryItem(new SItemStack(Material.RED_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().red().bold().text("この権限に設定する").build()).build());
                    background.clickable(false);
                    background.setEvent(generateClickChangePermissionEvent());
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

    public void renderIcons(){

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



        //delete item
        SInventoryItem deleteUser = new SInventoryItem(new SItemStack(Material.LAVA_BUCKET).setDisplayName(new SStringBuilder().yellow().obfuscated().text("OO")
                .darkRed().bold().text("ユーザーを削除")
                .yellow().obfuscated().text("OO")
                .build()).build());
        deleteUser.setEvent(e -> {
            if(!shop.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
                player.sendMessage(Man10ShopV2.prefix + "§c§lあなたはこのユーザーを消去する権限を持っていません");
                return;
            }
            if(target.permission == Man10ShopPermission.OWNER && shop.ownerCount() == 1){
                player.sendMessage(Man10ShopV2.prefix + "§c§lオーナーは最低一人必要です");
                return;
            }


            //confirmation
            ConfirmationMenu menu = new ConfirmationMenu("確認", plugin);
            menu.setOnConfirm(ee -> {
                if(!shop.removeModerator(target)){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                    return;
                }
                menu.getInventory().moveToMenu(player, new PermissionSettingsMainMenu(player, shop, plugin).renderInventory());
                player.sendMessage(Man10ShopV2.prefix + "§c§a" + target.name + "を消去しました");
            });

            menu.setOnCancel(ee -> menu.getInventory().moveToMenu(player, new PermissionSettingsMenu(player, shop, target, plugin).getInventory()));
            menu.setOnClose(ee -> menu.getInventory().moveToMenu(player, new PermissionSettingsMenu(player, shop, target, plugin).getInventory()));
            inventory.moveToMenu(player, menu.getInventory());


        });

        deleteUser.clickable(false);
        inventory.setItem(deleteUserSlot, deleteUser);


    }

    public SInventory getInventory() {
        return inventory;
    }

    public void renderInventory(){
        inventory = new SInventory(new SStringBuilder().red().text(target.name).text("の権限設定").build(), 4, plugin);

        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        inventory.fillItem(background);

        renderSelector();
        renderIcons();

        inventory.setOnCloseEvent(e -> inventory.moveToMenu(player, new PermissionSettingsMainMenu(player, shop, plugin).renderInventory()));
    }

}
