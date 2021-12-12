package com.shojabon.man10shopv2.dataClass.shopFunctions.general;

import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

@ShopFunctionDefinition(
        name = "サブ垢制限",
        explanation = {"設定個までアカウント数を制限", "0の場合は制限なし"},
        enabledShopType = {},
        iconMaterial = Material.PLAYER_HEAD,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class IpLimitFunction extends ShopFunction{

    //variables

    HashMap<String, ArrayList<UUID>> ipToAccounts = new HashMap<>();

    //init
    public IpLimitFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions



    //====================
    // settings
    //====================

    public Man10ShopSetting<Integer> accountCount = new Man10ShopSetting<>("ipLimit.count", 0);

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
        if(allowedAccounts.size() <= accountCount.get()) return true;

        allowedAccounts = new ArrayList<>(allowedAccounts.subList(0, accountCount.get()));
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
    public boolean isFunctionEnabled() {
        return accountCount.get() != 0;
    }

    @Override
    public String currentSettingString() {
        return accountCount.get() + "アカウント";
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //number input menu
            NumericInputMenu menu = new NumericInputMenu("§aアカウント数", plugin);
            menu.setOnCloseEvent(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnCancel(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnConfirm(newValue -> {
                if(!accountCount.set(newValue)){
                    warn(player, "内部エラーが発生しました");
                }
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
            });
            menu.open(player);

        });
        return item;
    }

}
