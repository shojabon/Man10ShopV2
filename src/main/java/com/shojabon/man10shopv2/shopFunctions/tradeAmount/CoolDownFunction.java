package com.shojabon.man10shopv2.shopFunctions.tradeAmount;

import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

@ShopFunctionDefinition(
        name = "取引クールダウン設定",
        explanation = {"取引を制限する", "設定秒に1回のみしか取引できなくなります", "0の場合はクールダウンなし"},
        enabledShopType = {},
        iconMaterial = Material.CLOCK,
        category = "取引量制限設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = false
)
public class CoolDownFunction extends ShopFunction {

    //variables
    public HashMap<UUID, Long> coolDownMap = new HashMap<>();
    public Man10ShopSetting<Integer> time = new Man10ShopSetting<>("shop.coolDown", 0);

    //init
    public CoolDownFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    public boolean checkCoolDown(Player p){
        int coolDown = time.get();
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
    
    @Override
    public boolean isFunctionEnabled() {
        return time.get() != 0;
    }
    
    @Override
    public boolean isAllowedToUseShop(Player p) {
        //if player is in coolDown
        if(checkCoolDown(p)){
            p.sendMessage(Man10ShopV2.prefix + "§c§l" + time.get() + "秒の取引クールダウン中です");
            return false;
        }
        return true;
    }

    @Override
    public boolean afterPerformAction(Player p, int amount) {
        if(!isFunctionEnabled()) return true;
        setCoolDown(p);
        return true;
    }

    @Override
    public String currentSettingString() {
        return time.get() + "秒";
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //number input menu
            NumericInputMenu menu = new NumericInputMenu(new SStringBuilder().green().text("取引クールダウン").build(), plugin);
            menu.setOnClose(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnCancel(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));
            menu.setOnConfirm(newValue -> {
                if(newValue < 0){
                    warn(player, "クールダウンタイムは正の数でなくてはならない");
                    return;
                }

                if(!time.set(newValue)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
            });
            menu.open(player);

        });

        return item;
    }
}
