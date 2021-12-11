package com.shojabon.man10shopv2.menus.permission;

import ToolMenu.BooleanInputMenu;
import ToolMenu.ConfirmationMenu;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopModerator;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;


import java.util.function.Consumer;

public class PermissionSettingsMenu extends SInventory{

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
        super(new SStringBuilder().red().text(target.name).text("の権限設定").build(), 4, plugin);
        this.player = p;
        this.target = target;
        this.shop = shop;
        this.plugin = plugin;
    }

    public Consumer<InventoryClickEvent> generateClickChangePermissionEvent(){
        return e -> {
            for(int i = 0; i < slots.length; i++){
                if(e.getRawSlot() != slots[i]) continue;
                if(permissions[i] == target.permission) continue;

                //permission change confirmation
                ConfirmationMenu menu = new ConfirmationMenu("確認", plugin);
                int finalI = i;
                menu.setOnConfirm(ee -> {
                    if(target.permission == Man10ShopPermission.OWNER && shop.permission.totalOwnerCount() == 1){
                        player.sendMessage(Man10ShopV2.prefix + "§c§lオーナーは最低一人必要です");
                        return;
                    }
                    target.permission = permissions[finalI];

                    if(!shop.permission.setModerator(target)){
                        player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                        return;
                    }

                    Man10ShopV2API.log(shop.shopId, "permissionChange." + target.uuid, permissions[finalI].name(), player.getName(), player.getUniqueId()); //log
                    player.sendMessage(Man10ShopV2.prefix + "§a§l権限を設定しました");
                    menu.moveToMenu(player, new PermissionSettingsMenu(player, shop, target, plugin));
                });

                menu.setOnCancel(ee -> menu.moveToMenu(player, new PermissionSettingsMenu(player, shop, target, plugin)));
                menu.setOnClose(ee -> menu.moveToMenu(player, new PermissionSettingsMenu(player, shop, target, plugin)));

                //open confirmation
                moveToMenu(player, menu);
                return;
            }
        };
    }

    public void renderSelector(){
        for(int i = 0; i < slots.length; i++){
            if(shop.permission.hasPermissionAtLeast(player.getUniqueId(), permissions[i])){
                if(shop.permission.hasPermission(target.uuid, permissions[i])){
                    //has permission and is current permission
                    SInventoryItem current = new SInventoryItem(new SItemStack(Material.LIME_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().green().bold().text("現在の権限").build()).build());
                    current.clickable(false);
                    setItem(slots[i], current);
                }else{
                    //has permission but not this permission
                    SInventoryItem background = new SInventoryItem(new SItemStack(Material.RED_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().red().bold().text("この権限に設定する").build()).build());
                    background.clickable(false);
                    background.setEvent(generateClickChangePermissionEvent());
                    setItem(slots[i], background);
                }
            }else{
                // no permission
                SInventoryItem notAllowed = new SInventoryItem(new SItemStack(Material.BARRIER).setDisplayName(new SStringBuilder().gray().bold().text("この権限に設定することはできません").build()).build());
                notAllowed.clickable(false);
                setItem(slots[i], notAllowed);
            }
        }

    }

    public void renderIcons(){

        SInventoryItem owner = new SInventoryItem(new SItemStack(Material.DIAMOND_BLOCK).setDisplayName(new SStringBuilder().aqua().bold().text("オーナー権限").build()).build());
        owner.clickable(false);
        setItem(slots[0]-9, owner);

        SInventoryItem moderator = new SInventoryItem(new SItemStack(Material.GOLD_BLOCK).setDisplayName(new SStringBuilder().gold().bold().text("管理者権限").build()).build());
        moderator.clickable(false);
        setItem(slots[1]-9, moderator);

        SInventoryItem accountant = new SInventoryItem(new SItemStack(Material.EMERALD_BLOCK).setDisplayName(new SStringBuilder().green().bold().text("会計権限").build()).build());
        accountant.clickable(false);
        setItem(slots[2]-9, accountant);

        SInventoryItem storageAccess = new SInventoryItem(new SItemStack(Material.CHEST).setDisplayName(new SStringBuilder().yellow().bold().text("倉庫編集権限").build()).build());
        storageAccess.clickable(false);
        setItem(slots[3]-9, storageAccess);



        //delete item
        SInventoryItem deleteUser = new SInventoryItem(new SItemStack(Material.LAVA_BUCKET).setDisplayName(new SStringBuilder().yellow().obfuscated().text("OO")
                .darkRed().bold().text("ユーザーを削除")
                .yellow().obfuscated().text("OO")
                .build()).build());
        deleteUser.setAsyncEvent(e -> {
            if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), Man10ShopPermission.MODERATOR)){
                player.sendMessage(Man10ShopV2.prefix + "§c§lあなたはこのユーザーを消去する権限を持っていません");
                return;
            }
            if(target.permission == Man10ShopPermission.OWNER && shop.permission.totalOwnerCount() == 1){
                player.sendMessage(Man10ShopV2.prefix + "§c§lオーナーは最低一人必要です");
                return;
            }


            //confirmation
            ConfirmationMenu menu = new ConfirmationMenu("確認", plugin);
            menu.setOnConfirm(ee -> {
                if(!shop.permission.removeModerator(target)){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                    return;
                }
                Man10ShopV2API.log(shop.shopId, "permissionDelete", target.uuid, player.getName(), player.getUniqueId()); //log
                menu.moveToMenu(player, new PermissionSettingsMainMenu(player, shop, plugin).renderInventory());
                player.sendMessage(Man10ShopV2.prefix + "§c§a" + target.name + "を消去しました");
            });

            menu.setOnCancel(ee -> menu.moveToMenu(player, new PermissionSettingsMenu(player, shop, target, plugin)));
            menu.setOnClose(ee -> menu.moveToMenu(player, new PermissionSettingsMenu(player, shop, target, plugin)));
            moveToMenu(player, menu);


        });

        deleteUser.clickable(false);
        setItem(deleteUserSlot, deleteUser);


    }

    public void renderNotification(){
        SInventoryItem notification = new SInventoryItem(new SItemStack(Material.BELL).setDisplayName(new SStringBuilder().gold().bold().text("通知設定").build()).build());
        notification.clickable(false);
        setItem(10, notification);

        SInventoryItem enabled = new SInventoryItem(new SItemStack(Material.LIME_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().green().bold().text("有効").build()).build());
        enabled.clickable(false);
        enabled.setAsyncEvent(renderNotificationEvent());

        SInventoryItem disabled = new SInventoryItem(new SItemStack(Material.RED_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().red().bold().text("無効").build()).build());
        disabled.clickable(false);
        disabled.setAsyncEvent(renderNotificationEvent());

        if(target.notificationEnabled){
            setItem(19, enabled);
        }else{
            setItem(19, disabled);
        }

    }

    public Consumer<InventoryClickEvent> renderNotificationEvent(){
        return e -> {
            if(!target.uuid.equals(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.prefix + "§c§lこの設定は本人のみ編集可能です");
                return;
            }


            BooleanInputMenu boolMenu = new BooleanInputMenu(target.notificationEnabled, "設定を変更しますか？", plugin);
            boolMenu.setOnConfirm(bool -> {
                if(!shop.permission.setEnableNotification(target, bool)){
                    player.sendMessage(Man10ShopV2.prefix + "§c§l内部エラーが発生しました");
                    return;
                }
                player.sendMessage(Man10ShopV2.prefix + "§a§l通知設定を設定しました");
                boolMenu.moveToMenu(player, new PermissionSettingsMenu(player, shop, target, plugin));
            });
            boolMenu.setOnCancel(ee -> boolMenu.moveToMenu(player, new PermissionSettingsMenu(player, shop, target, plugin)));
            boolMenu.setOnClose(ee -> boolMenu.moveToMenu(player, new PermissionSettingsMenu(player, shop, target, plugin)));

            moveToMenu(player, boolMenu);

        };
    }

    public void renderMenu(){

        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        fillItem(background);

        renderSelector();
        renderIcons();
        renderNotification();

        setOnCloseEvent(e -> moveToMenu(player, new PermissionSettingsMainMenu(player, shop, plugin).renderInventory()));
    }

}
