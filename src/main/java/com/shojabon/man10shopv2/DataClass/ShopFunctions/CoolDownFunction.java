package com.shojabon.man10shopv2.DataClass.ShopFunctions;

import ToolMenu.NumericInputMenu;
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

import java.util.HashMap;
import java.util.UUID;

public class CoolDownFunction extends ShopFunction {

    //variables
    public HashMap<UUID, Long> coolDownMap = new HashMap<>();

    //init
    public CoolDownFunction(Man10Shop shop) {
        super(shop);
    }


    public boolean checkCoolDown(Player p){
        int coolDown = getCoolDownTime();
        if(coolDown == 0) return false;
        if(!coolDownMap.containsKey(p.getUniqueId())) coolDownMap.put(p.getUniqueId(), 0L);
        long currentTime = System.currentTimeMillis() / 1000L;

        return currentTime - coolDownMap.get(p.getUniqueId()) < coolDown;
    }

    public void setCoolDown(Player p){
        long currentTime = System.currentTimeMillis() / 1000L;
        coolDownMap.put(p.getUniqueId(), currentTime);
    }

    //====================
    // settings
    //====================

    public int getCoolDownTime(){
        String currentSetting = getSetting("shop.coolDown");
        if(!BaseUtils.isInt(currentSetting)) return 0;
        return Integer.parseInt(currentSetting);
    }

    public boolean setCoolDown(int time){
        if(getCoolDownTime() == time) return true;
        return setSetting("shop.coolDown", time);
    }

    @Override
    public boolean hasPermissionToEdit(UUID uuid) {
        return shop.permission.hasPermissionAtLeast(uuid, Man10ShopPermission.MODERATOR);
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        //if player is in coolDown
        if(checkCoolDown(p)){
            p.sendMessage(Man10ShopV2.prefix + "§c§l" + getCoolDownTime() + "秒の取引クールダウン中です");
            return false;
        }
        return true;
    }

    @Override
    public void performAction(Player p, int amount) {
        setCoolDown(p);
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.CLOCK).setDisplayName(new SStringBuilder().yellow().text("取引クールダウン").build());
        item.addLore(new SStringBuilder().lightPurple().text("現在の設定: ").yellow().text(getCoolDownTime()).text("秒").build());
        item.addLore("");
        item.addLore("§f取引を制限する");
        item.addLore("§f設定秒に1回のみしか取引できなくなります");
        item.addLore("§f0の場合はクールダウンなし");

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);
        inventoryItem.setEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.prefix + "§c§l権限が不足しています");
                return;
            }
            //number input menu
            NumericInputMenu menu = new NumericInputMenu(new SStringBuilder().green().text("取引クールダウン").build(), plugin);
            menu.setOnClose(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));
            menu.setOnCancel(ee -> menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin)));
            menu.setOnConfirm(newValue -> {
                if(newValue < 0){
                    player.sendMessage(Man10ShopV2.prefix + "§c§lクールダウンタイムは正の数でなくてはならない");
                    return;
                }

                if(shop.coolDown.setCoolDown(newValue)){
                    Man10ShopV2API.log(shop.shopId, "setCoolDownTime", newValue, player.getName(), player.getUniqueId()); //log
                }
                menu.moveToMenu(player, new SettingsMainMenu(player, shop, plugin));
            });
            sInventory.moveToMenu(player, menu);

        });

        return inventoryItem;
    }
}
