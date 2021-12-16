package com.shojabon.man10shopv2.shopFunctions.allowedToUse;

import ToolMenu.AutoScaledMenu;
import ToolMenu.NumericInputMenu;
import ToolMenu.TimeSelectorMenu;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

@ShopFunctionDefinition(
        name = "範囲内プレイヤー有効化設定",
        explanation = {"範囲内にプレイヤーが設定人数人いるときのみ", "shopを利用可能に", "※範囲はプレイヤー基準"},
        enabledShopType = {},
        iconMaterial = Material.WOLF_SPAWN_EGG,
        category = "使用可条件設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class ProximityFriendshipAllowFunction extends ShopFunction{

    //variables


    //init
    public ProximityFriendshipAllowFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions



    //====================
    // settings
    //====================

    public Man10ShopSetting<Integer> numberOfPlayers = new Man10ShopSetting<>("proximity.friendship.allow.count", 0);
    public Man10ShopSetting<Integer> range = new Man10ShopSetting<>("proximity.friendship.allow.range", 0);


    @Override
    public boolean isAllowedToUseShop(Player p) {
        int count = 0;
        for(Player playerInWorld: p.getWorld().getPlayers()){
            if(p.getUniqueId() == playerInWorld.getUniqueId()) continue;

            if(playerInWorld.getLocation().distance(p.getLocation()) <= range.get()){
                count += 1;
            }
        }
        boolean result = count >= numberOfPlayers.get();
        if(!result) warn(p, "自分から" + range.get() + "ブロック以内にプレイヤーが" + numberOfPlayers.get() + "人いないと使用できません");
        return result;
    }

    @Override
    public String currentSettingString() {
        return range.get() + "の距離に" + numberOfPlayers.get() + "人いないと使用不可";
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //confirmation menu
            getInnerSettingMenu(player, plugin).open(player);
        });
        return item;
    }

    public AutoScaledMenu getInnerSettingMenu(Player player, Man10ShopV2 plugin){
        AutoScaledMenu autoScaledMenu = new AutoScaledMenu("分間毎ごとの倉庫補充設定", plugin);

        SInventoryItem rangeSetting = new SInventoryItem(new SItemStack(Material.COMPASS).setDisplayName("§2§l距離").build());
        rangeSetting.clickable(false);
        rangeSetting.setEvent(e -> {

            NumericInputMenu menu = new NumericInputMenu("距離を入力してください 0はoff", plugin);
            menu.setOnConfirm(number -> {
                if(!range.set(number)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "範囲を設定しました");
                getInnerSettingMenu(player, plugin).open(player);
            });
            menu.setOnCancel(ee -> getInnerSettingMenu(player, plugin).open(player));
            menu.setOnClose(ee -> getInnerSettingMenu(player, plugin).open(player));

            menu.open(player);
        });

        SInventoryItem playerCountSetting = new SInventoryItem(new SItemStack(Material.PLAYER_HEAD).setDisplayName("§2§l人数").build());
        playerCountSetting.clickable(false);
        playerCountSetting.setEvent(e -> {

            NumericInputMenu menu = new NumericInputMenu("人数を入力してください 0はoff", plugin);
            menu.setOnConfirm(number -> {
                if(!numberOfPlayers.set(number)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "範囲を設定しました");
                getInnerSettingMenu(player, plugin).open(player);
            });
            menu.setOnCancel(ee -> getInnerSettingMenu(player, plugin).open(player));
            menu.setOnClose(ee -> getInnerSettingMenu(player, plugin).open(player));

            menu.open(player);
        });


        autoScaledMenu.addItem(rangeSetting);
        autoScaledMenu.addItem(playerCountSetting);

        return autoScaledMenu;
    }

}
