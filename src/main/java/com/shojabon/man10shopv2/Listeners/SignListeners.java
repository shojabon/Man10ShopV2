package com.shojabon.man10shopv2.Listeners;

import ToolMenu.ConfirmationMenu;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.Man10ShopSign;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Enums.Man10ShopType;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.AdminShopSelectorMenu;
import com.shojabon.man10shopv2.Menus.action.BarterActionMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Menus.EditableShopSelectorMenu;
import com.shojabon.man10shopv2.Menus.action.BuySellActionMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
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
        if(!Objects.requireNonNull(e.getLine(0)).equalsIgnoreCase("man10shop") && !Objects.requireNonNull(e.getLine(0)).equalsIgnoreCase("man10adminshop")) return;

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

        if(Objects.requireNonNull(e.getLine(0)).equalsIgnoreCase("man10shop")){
            EditableShopSelectorMenu menu = new EditableShopSelectorMenu(e.getPlayer(), "その他", plugin);

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
                            confirmationMenu.close(e.getPlayer());
                        }
                        Man10ShopV2.vault.withdraw(uuid, signPrice );
                        buySign(shop, e);
                    });

                    confirmationMenu.setOnCancel(ee -> confirmationMenu.close(e.getPlayer()));

                    confirmationMenu.open(e.getPlayer());
                    return;
                }
                buySign(shop, e);

            });
            menu.open(e.getPlayer());
        }else{

            //permission to use
            if(!e.getPlayer().hasPermission("man10shopv2.admin.sign.create")){
                e.getPlayer().sendMessage(Man10ShopV2.prefix + "§c§lあなたには権限がありません");
                return;
            }
            AdminShopSelectorMenu menu = new AdminShopSelectorMenu(e.getPlayer(), plugin);

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
                            confirmationMenu.close(e.getPlayer());
                        }
                        Man10ShopV2.vault.withdraw(uuid, signPrice );
                        buySign(shop, e);
                    });

                    confirmationMenu.setOnCancel(ee -> confirmationMenu.close(e.getPlayer()));

                    confirmationMenu.open(e.getPlayer());
                    return;
                }
                buySign(shop, e);

            });
            menu.open(e.getPlayer());
        }
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
        if(!shop.permission.hasPermissionAtLeast(e.getPlayer().getUniqueId(), Man10ShopPermission.MODERATOR) && !e.getPlayer().hasPermission("man10shopv2.sign.break.bypass")){
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

        Man10ShopV2.threadPool.execute(()->{
            Man10ShopSign sign = Man10ShopV2.api.getSign(e.getClickedBlock().getLocation());
            if(sign == null) return;
            Man10Shop shop = Man10ShopV2.api.getShop(sign.shopId);
            if(shop == null) return;
            if(!shop.allowedToUseShop(e.getPlayer())) return;

            shop.openActionMenu(e.getPlayer());
        });
    }

    @EventHandler
    public void onSignBreak(BlockPhysicsEvent e){
        if(e.isCancelled())return;
        Block source = e.getSourceBlock();
        Block block = e.getBlock();
        if(source.getType() != Material.AIR){
            return;
        }
        if(!(block.getState() instanceof Sign)){
            return;
        }
        if(block.getLocation().equals(e.getSourceBlock().getLocation())){
            return;
        }
        if(block.getState().getBlockData() instanceof org.bukkit.block.data.type.WallSign){
            org.bukkit.block.data.type.WallSign signData = (org.bukkit.block.data.type.WallSign) block.getState().getBlockData();
            if(!block.getRelative(signData.getFacing().getOppositeFace()).equals(source)) return;
        }else if(block.getState().getBlockData() instanceof org.bukkit.block.data.type.Sign){
            if(!block.getRelative(BlockFace.DOWN).equals(source)) return;
        }


        Man10ShopV2.threadPool.execute(()->{
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
            if(shop.shopType.getShopType() == Man10ShopType.BUY){
                sign.setLine(0, "§a§l販売ショップ");
            }else if(shop.shopType.getShopType() == Man10ShopType.SELL){
                sign.setLine(0, "§c§l買取ショップ");
            }else if(shop.shopType.getShopType() == Man10ShopType.BARTER){
                sign.setLine(0, "§b§lトレードショップ");
            }

            sign.update();
            sign.setLine(3, formatSignString(sign.getLine(2), shop));
            sign.setLine(2, formatSignString(sign.getLine(1), shop));
            if(shop.shopEnabled.getShopEnabled()){
                if(shop.shopType.getShopType() != Man10ShopType.BARTER){
                    if(shop.secretPrice.isFunctionEnabled()){
                        sign.setLine(1, "§b??????円");
                    }else{
                        sign.setLine(1, "§b" + BaseUtils.priceString(shop.price.getPrice()) + "円");
                    }
                }else{
                    sign.setLine(1, "");
                }
            }else{
                sign.setLine(1, "§c取引停止中");
            }

            sign.update(true);

            Location l = e.getBlock().getLocation();
            Man10ShopV2.threadPool.execute(() -> {
                Man10ShopV2.api.createSign(new Man10ShopSign(shop.shopId, l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ()));
            });
            e.getPlayer().sendMessage(Man10ShopV2.prefix + "§a§l看板を作成しました");
            e.getPlayer().closeInventory();
        });
    }

    public boolean containsPrice(String original){
        return original.contains("{price}");
    }

    public String formatSignString(String original, Man10Shop shop){
        return original.replace("{price}", String.valueOf(shop.price.getPrice()))
                .replace("{iName}", shop.targetItem.getTargetItem().getDisplayName())
                .replace("{sName}", shop.name.getName())
                .replace("&", "§");
    }



}
