package com.shojabon.man10shopv2.shopFunctions.lootBox;

import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.LootBoxFunction;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.menus.settings.lootBoxSettings.LootBoxBigWinSelectorMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

@ShopFunctionDefinition(
        name = "通知・花火設定",
        explanation = {"このグループのアイテムが当たった際サーバー全体に通知する"},
        enabledShopType = {Man10ShopType.LOOT_BOX},
        iconMaterial = Material.FIREWORK_ROCKET,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = false
)
public class LootBoxBigWinFunction extends LootBoxFunction {

    //variables


    //init
    public LootBoxBigWinFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions

    //====================
    // settings
    //====================

    @Override
    public void afterLootBoxSpinFinished(Player player, ItemStack item, int groupId) {
        if(!shop.lootBoxFunction.lootBox.get().groupData.get(groupId).bigWin) return;
        Bukkit.broadcastMessage(Man10ShopV2.gachaPrefix + "§e§l" + player.getName() + "§a§lさんは" + shop.name.getName() + "で『" + new SItemStack(item).getDisplayName() + "§a§l』を当てました！");

        Bukkit.getScheduler().runTask(shop.plugin, ()->{
            Firework fw = (Firework) player.getLocation().getWorld().spawnEntity(player.getLocation().add(0, 2, 0), EntityType.FIREWORK);
            FireworkMeta fwm = fw.getFireworkMeta();

            fwm.setPower(0);
            fwm.addEffect(FireworkEffect.builder().withColor(Color.LIME).with(FireworkEffect.Type.STAR).flicker(true).build());
            fwm.addEffect(FireworkEffect.builder().withColor(Color.YELLOW).with(FireworkEffect.Type.BALL).flicker(true).build());
            fwm.addEffect(FireworkEffect.builder().withColor(Color.RED).with(FireworkEffect.Type.CREEPER).flicker(true).build());
            fwm.addEffect(FireworkEffect.builder().withColor(Color.BLUE).with(FireworkEffect.Type.BALL_LARGE).flicker(true).build());

            fw.setFireworkMeta(fwm);
            fw.detonate();
        });
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            new LootBoxBigWinSelectorMenu(player, shop, shop.lootBoxFunction.lootBox.get(), plugin).open(player);
        });


        return item;
    }

}
