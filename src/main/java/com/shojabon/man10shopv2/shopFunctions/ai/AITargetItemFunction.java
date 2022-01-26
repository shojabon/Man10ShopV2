package com.shojabon.man10shopv2.shopFunctions.ai;

import ToolMenu.NumericInputMenu;
import ToolMenu.SingleItemStackSelectorMenu;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

@ShopFunctionDefinition(
        name = "対象アイテム設定",
        explanation = {},
        enabledShopType = {Man10ShopType.AI},
        iconMaterial = Material.DIAMOND,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class AITargetItemFunction extends ShopFunction{

    public Man10ShopSetting<ItemStack> item = new Man10ShopSetting<>("shop.ai.targetitem", null, true);

    public AITargetItemFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        if(item.get() == null){
            if(Man10ShopV2.vault.getBalance(p.getUniqueId()) < shop.aiPriceUnitFunction.price.get()){
                warn(p, "残高が不足しています");
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean performAction(Player p, int amount) {
        if(item.get() == null){
            int totalPrice = shop.aiPriceUnitFunction.price.get();
            if(!Man10ShopV2.vault.withdraw(p.getUniqueId(), totalPrice)){
                warn(p, "内部エラーが発生しました");
                return false;
            }
            shop.money.addMoney(totalPrice);
        }else{
            ItemStack requiredItem = item.get();
            if(requiredItem == null) {
                warn(p, "内部エラーが発生しました");
                return false;
            }
            if(!p.getInventory().containsAtLeast(requiredItem, shop.aiPriceUnitFunction.price.get())){
                p.sendMessage(Man10ShopV2.prefix + "§c§lトレードのためのアイテムが不足しています");
                return false;
            }

            int paymentLeft = shop.aiPriceUnitFunction.price.get();
            ItemStack[] items = p.getInventory().getContents();
            for(int i = 0; i < items.length; i++){
                if(paymentLeft == 0) break;
                if(items[i] == null) continue;
                if(!new SItemStack(items[i]).getItemTypeMD5(true).equalsIgnoreCase(new SItemStack(requiredItem).getItemTypeMD5(true))) continue;
                int removing = requiredItem.getMaxStackSize();
                if(removing > items[i].getAmount()) removing = items[i].getAmount();
                if(paymentLeft < removing) removing = paymentLeft;

                //delete process
                if(removing == requiredItem.getMaxStackSize()){
                    items[i] = null;
                }else{
                    items[i] = new SItemStack(items[i].clone()).setAmount(items[i].clone().getAmount() - removing).build();
                }
                paymentLeft -= removing;
            }

            p.getInventory().setContents(items);

        }
        return true;
    }

    @Override
    public String currentSettingString() {
        if(item.get() == null) return "現金払い";
        return new SItemStack(item.get()).getDisplayName();
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setEvent(e -> {
            //confirmation menu
            SingleItemStackSelectorMenu menu = new SingleItemStackSelectorMenu("アイテムを設定してください", this.item.get(), plugin);
            menu.allowNullItem(true);
            menu.setOnConfirm(finalItem -> {
                if(finalItem == null){
                    if(!this.item.delete()){
                        warn(player, "内部エラーが発生しました");
                        return;
                    }
                }else{
                    if(!this.item.set(finalItem)){
                        warn(player, "内部エラーが発生しました");
                        return;
                    }
                }
                success(player, "アイテムを設定しました");
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
            });
            menu.setOnCloseEvent(ee -> new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player));

            menu.open(player);
        });
        return item;
    }

}
