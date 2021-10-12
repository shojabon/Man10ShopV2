package com.shojabon.man10shopv2.DataClass.ShopFunctions.allowedToUse;

import ToolMenu.BooleanInputMenu;
import ToolMenu.TimeSelectorMenu;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.Settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.sql.Time;
import java.util.UUID;

public class EnabledFromFunction extends ShopFunction {

    //variables

    //init
    public EnabledFromFunction(Man10Shop shop) {
        super(shop);
    }


    //functions


    //====================
    // settings
    //====================

    public long getEnabledTime(){
        String currentSetting = getSetting("shop.enabledFrom");
        if(!BaseUtils.isLong(currentSetting)) return 0;
        return Long.parseLong(currentSetting);
    }

    public boolean setEnabledTime(long enabled){
        if(getEnabledTime() == enabled) return true;
        if(!setSetting("shop.enabledFrom", enabled)) return false;
        Man10ShopV2API.closeInventoryGroup(shop.getShopId());
        return true;
    }

    @Override
    public boolean isFunctionEnabled() {
        return getEnabledTime() != 0;
    }

    @Override
    public String settingCategory() {
        return "使用可条件設定";
    }

    @Override
    public boolean hasPermissionToEdit(UUID uuid) {
        return shop.permission.hasPermissionAtLeast(uuid, Man10ShopPermission.MODERATOR);
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        if(System.currentTimeMillis()/1000L < getEnabledTime()) {
            p.sendMessage(Man10ShopV2.prefix + "§c§l現在このショップは停止しています、開始は " + BaseUtils.unixTimeToString(getEnabledTime()));
            return false;
        }
        return true;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.EMERALD_BLOCK).setDisplayName(new SStringBuilder().gray().text("有効化開始時間設定").build());

        String currentSetting = BaseUtils.unixTimeToString(getEnabledTime());
        if(!isFunctionEnabled())currentSetting = "なし";

        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(currentSetting).build());
        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);

        inventoryItem.setEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }

            TimeSelectorMenu menu = new TimeSelectorMenu(getEnabledTime(), "有効化開始時間を設定してください", plugin);
            menu.setOnCloseEvent(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin)));
            menu.setOnConfirm(time -> {
                if(time == -1L){
                    deleteSetting("shop.enabledFrom");
                }else{
                    if(setEnabledTime(time)){
                        Man10ShopV2API.log(shop.shopId, "enabledFrom", time, player.getName(), player.getUniqueId()); //log
                    }
                }
                player.sendMessage(Man10ShopV2.prefix + "§a§l時間を設定しました");
                menu.moveToMenu(player, new SettingsMainMenu(player, shop, settingCategory(), plugin));
            });

            sInventory.moveToMenu(player, menu);

        });


        return inventoryItem;
    }

}
