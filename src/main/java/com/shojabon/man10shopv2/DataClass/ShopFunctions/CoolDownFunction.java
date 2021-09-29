package com.shojabon.man10shopv2.DataClass.ShopFunctions;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Utils.BaseUtils;
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
    public boolean isAllowedToUseShop(Player p) {
        //if player is in coolDown
        if(checkCoolDown(p)){
            p.sendMessage(Man10ShopV2.prefix + "§c§l" + getCoolDownTime() + "秒の取引クールダウン中です");
            return false;
        }
        return true;
    }
}
