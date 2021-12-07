package com.shojabon.man10shopv2.DataClass.ShopFunctions.general;

import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.Settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class IpLimitFunction extends ShopFunction {

    //variables

    HashMap<String, ArrayList<UUID>> ipToAccounts = new HashMap<>();

    //init
    public IpLimitFunction(Man10Shop shop) {
        super(shop);
    }


    //functions



    //====================
    // settings
    //====================

    public int getAccountCount(){
        String currentSetting = getSetting("ipLimit.count");
        if(!BaseUtils.isInt(currentSetting)) return 0;
        return Integer.parseInt(currentSetting);
    }

    public boolean setAccountCount(int count){
        if(getAccountCount() == count) return true;
        return setSetting("ipLimit.count", count);
    }

    @Override
    public boolean afterPerformAction(Player p, int amount) {
        if(p.getAddress() == null) return false;
        String address = p.getAddress().getHostString();
        if(!ipToAccounts.containsKey(address)) ipToAccounts.put(address, new ArrayList<>());
        if(ipToAccounts.get(address).contains(p.getUniqueId())) return false;
        ipToAccounts.get(address).add(p.getUniqueId());
        return true;
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        if(!isFunctionEnabled()) return true;
        if(p.getAddress() == null) {
            warn(p, "IP内部エラーが発生しました");
            return false;
        }

        String address = p.getAddress().getHostString();
        if(!ipToAccounts.containsKey(address)) return true;

        ArrayList<UUID> allowedAccounts = ipToAccounts.get(address);
        if(!allowedAccounts.contains(p.getUniqueId())) allowedAccounts.add(p.getUniqueId());
        if(allowedAccounts.size() <= getAccountCount()) return true;

        allowedAccounts = new ArrayList<>(allowedAccounts.subList(0, getAccountCount()));
        if(allowedAccounts.contains(p.getUniqueId())) return true;

        StringBuilder accounts = new StringBuilder();
        for(UUID uuid: allowedAccounts){
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            accounts.append(offlinePlayer.getName()).append(" ");
        }
        warn(p, "このショップを使えるアカウントは");
        warn(p, accounts.substring(0, accounts.length()-1) + "です");

        return false;
    }

    @Override
    public String settingCategory() {
        return "一般設定";
    }

    @Override
    public boolean hasPermissionToEdit(UUID uuid) {
        return shop.permission.hasPermissionAtLeast(uuid, Man10ShopPermission.MODERATOR);
    }

    @Override
    public boolean isFunctionEnabled() {
        return getAccountCount() != 0;
    }

    @Override
    public SInventoryItem getAdminSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.PLAYER_HEAD).setDisplayName(new SStringBuilder().green().text("サブ垢制限").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(getAccountCount()).build());
        item.addLore("");
        item.addLore("§f設定個までアカウント数を制限");
        item.addLore("§f0の場合制限話");

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //number input menu
            NumericInputMenu menu = new NumericInputMenu(new SStringBuilder().green().text("アカウント数").build(), plugin);
            if(!shop.admin) menu.setMaxValue(shop.storage.storageSize);
            menu.setOnClose(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin)));
            menu.setOnCancel(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin)));
            menu.setOnConfirm(newValue -> {
                if(setAccountCount(newValue)){
                    Man10ShopV2API.log(shop.shopId, "setIpLimit", newValue, player.getName(), player.getUniqueId()); //log
                }
                menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin));
            });
            sInventory.moveToMenu(player, menu);

        });

        return inventoryItem;
    }
}
