package com.shojabon.man10shopv2.Listeners;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.Man10ShopSign;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.ToolMenu.ConfirmationMenu;
import com.shojabon.man10shopv2.Menus.EditableShopSelectorMenu;
import com.shojabon.man10shopv2.Menus.ShopActionMenu;
import com.shojabon.man10shopv2.Utils.BaseUtils;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class SignListeners implements @NotNull Listener {

    Man10ShopV2 plugin;

    public SignListeners(Man10ShopV2 plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignUpdate(SignChangeEvent e){
        if(e.getLine(0) == null) return;
        if(!Objects.requireNonNull(e.getLine(0)).equalsIgnoreCase("man10shop")) return;

        //permission to use
        if(!e.getPlayer().hasPermission("man10shopv2.sign.create")){
            e.getPlayer().sendMessage(Man10ShopV2.prefix + "§c§lあなたには権限がありません");
            return;
        }

        //allowed world
        if(!Man10ShopV2.config.getStringList("enabledWorlds").contains(e.getBlock().getWorld().getName())) return;

        //if plugin disabled
        if(!Man10ShopV2.config.getBoolean("pluginEnabled")){
            e.getPlayer().sendMessage(Man10ShopV2.prefix + "§c§l現在このプラグインは停止中です");
            return;
        }

        EditableShopSelectorMenu menu = new EditableShopSelectorMenu(e.getPlayer(), plugin);

        int signPrice = Man10ShopV2.config.getInt("sign.price");
        UUID uuid = e.getPlayer().getUniqueId();

        menu.setOnClick(shop -> {

            if(shop.signs.size() + 1 > Man10ShopV2.config.getInt("sign.maxSignsPerShop")){
                e.getPlayer().sendMessage(Man10ShopV2.prefix + "§a§l設置できる看板の量を越しました");
                return;
            }

            if(Man10ShopV2.config.getInt("sign.price") != 0){
                ConfirmationMenu confirmationMenu = new ConfirmationMenu(BaseUtils.priceString(signPrice)+ "円支払いますか？", plugin);

                //confirm purchase
                confirmationMenu.setOnConfirm(ee -> {
                    if(Man10ShopV2.vault.getBalance(uuid) < signPrice){
                        e.getPlayer().sendMessage(Man10ShopV2.prefix + "§c§l現金が不足しています");
                        e.getPlayer().closeInventory();
                    }
                    Man10ShopV2.vault.withdraw(uuid, signPrice );
                    buySign(shop, e);
                });

                confirmationMenu.open(e.getPlayer());
                return;
            }
            buySign(shop, e);

        });
        menu.open(e.getPlayer());
    }

    @EventHandler
    public void onSignDestroy(BlockBreakEvent e){
        if(!(e.getBlock().getState() instanceof Sign)){
            return;
        }
        Man10ShopSign sign = plugin.api.getSign(e.getBlock().getLocation());
        if(sign == null) return;
        Man10Shop shop = plugin.api.getShop(sign.shopId);
        if(shop == null) {
            plugin.api.deleteSign(sign);
            return;
        }
        if(!shop.hasPermissionAtLeast(e.getPlayer().getUniqueId(), Man10ShopPermission.MODERATOR) && !e.getPlayer().hasPermission("man10shopv2.sign.break.bypass")){
            e.getPlayer().sendMessage(Man10ShopV2.prefix + "§c§l看板を破壊する権限を持っていません");
            e.setCancelled(true);
            return;
        }
        SInventory.threadPool.execute(()-> {
            plugin.api.deleteSign(sign);
            e.getPlayer().sendMessage(Man10ShopV2.prefix + "§a§l看板を破壊しました");
        });
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent e){
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(e.getClickedBlock() == null) return;
        if(!(e.getClickedBlock().getState() instanceof Sign)) return;

        Man10ShopSign sign = plugin.api.getSign(e.getClickedBlock().getLocation());
        if(sign == null) return;
        Man10Shop shop = plugin.api.getShop(sign.shopId);
        if(shop == null) return;

        //permission to use
        if(!e.getPlayer().hasPermission("man10shopv2.use")){
            e.getPlayer().sendMessage(Man10ShopV2.prefix + "§c§lあなたには権限がありません");
            return;
        }

        //allowed world
        if(!Man10ShopV2.config.getStringList("enabledWorlds").contains(e.getClickedBlock().getWorld().getName())) return;

        //if plugin disabled
        if(!Man10ShopV2.config.getBoolean("pluginEnabled")){
            e.getPlayer().sendMessage(Man10ShopV2.prefix + "§c§l現在このプラグインは停止中です");
            return;
        }

        ShopActionMenu menu = new ShopActionMenu(e.getPlayer(), shop, plugin);
        if(!shop.settings.getShopEnabled()){
            e.getPlayer().sendMessage(Man10ShopV2.prefix + "§c§l現在このショップは停止しています");
            return;
        }
        menu.open(e.getPlayer());
    }

    @EventHandler
    public void onSignBreak(BlockPhysicsEvent e){
        if(!(e.getBlock().getState() instanceof Sign)){
            return;
        }
        SInventory.threadPool.execute(()->{
            Man10ShopSign sign = plugin.api.getSign(e.getBlock().getLocation());
            if(sign == null) return;
            Man10Shop shop = plugin.api.getShop(sign.shopId);
            if(shop == null) {
                plugin.api.deleteSign(sign);
                return;
            }
            plugin.api.deleteSign(sign);
        });
    }

    public void buySign(Man10Shop shop, SignChangeEvent e){
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Sign sign = ((Sign) e.getBlock().getState());

            if(shop.shopType == Man10ShopType.BUY){
                sign.setLine(0, "§a§l販売ショップ");
            }else{
                sign.setLine(0, "§c§l買取ショップ");
            }
            sign.setLine(3, formatSignString(sign.getLine(2), shop));
            sign.setLine(2, formatSignString(sign.getLine(1), shop));
            if(shop.settings.getShopEnabled()){
                sign.setLine(1, "§b" + BaseUtils.priceString(shop.price) + "円");
            }else{
                sign.setLine(1, "§c取引停止中");
            }

            sign.update(true);

            Location l = e.getBlock().getLocation();
            SInventory.threadPool.execute(() -> {
                plugin.api.createSign(new Man10ShopSign(shop.shopId, l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ()));
            });
            e.getPlayer().sendMessage(Man10ShopV2.prefix + "§a§l看板を作成しました");
            e.getPlayer().closeInventory();
        });
    }

    public boolean containsPrice(String original){
        return original.contains("{price}");
    }

    public String formatSignString(String original, Man10Shop shop){
        return original.replace("{price}", String.valueOf(shop.price))
                .replace("{iName}", shop.targetItem.getDisplayName())
                .replace("{sName}", shop.name)
                .replace("&", "§");
    }



}
