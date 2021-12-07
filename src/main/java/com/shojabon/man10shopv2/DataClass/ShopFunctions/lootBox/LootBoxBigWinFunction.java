package com.shojabon.man10shopv2.DataClass.ShopFunctions.lootBox;

import com.shojabon.man10shopv2.DataClass.LootBoxFunction;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.DataClass.lootBox.LootBox;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.Settings.lootBoxSettings.LootBoxBigWinSelectorMenu;
import com.shojabon.man10shopv2.Menus.Settings.lootBoxSettings.LootBoxGroupSelectorMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.UUID;

public class LootBoxBigWinFunction extends LootBoxFunction {

    //variables


    //init
    public LootBoxBigWinFunction(Man10Shop shop) {
        super(shop);
    }


    //functions

    //====================
    // settings
    //====================

    @Override
    public void afterLootBoxSpinFinished(Player player, ItemStack item, int groupId) {
        if(!shop.lootBoxFunction.getLootBox().groupData.get(groupId).bigWin) return;
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
    public Man10ShopType[] enabledShopTypes() {
        return new Man10ShopType[]{Man10ShopType.LOOT_BOX};
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
    public SInventoryItem getAdminSettingItem(Player player, SInventory sInventory, Man10ShopV2 plugin) {
        SItemStack item = new SItemStack(Material.FIREWORK_ROCKET).setDisplayName("§e§l通知・花火設定");

        item.addLore("");
        item.addLore("§f最上位グループの設定");
        item.addLore("§fグループ数が0の場合は使用不可");

        SInventoryItem inventoryItem = new SInventoryItem(item.build());
        inventoryItem.clickable(false);

        inventoryItem.setEvent(e -> {
            if(!hasPermissionToEdit(player.getUniqueId())){
                player.sendMessage(Man10ShopV2.gachaPrefix + "§c§l権限が不足しています");
                return;
            }
            //confirmation menu
            sInventory.moveToMenu(player, new LootBoxBigWinSelectorMenu(player, shop, shop.lootBoxFunction.getLootBox(), plugin));

        });


        return inventoryItem;
    }

}
