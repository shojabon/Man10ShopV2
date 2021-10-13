package com.shojabon.man10shopv2.Menus.Settings;

import ToolMenu.CategoricalSInventoryMenu;
import ToolMenu.ConfirmationMenu;
import ToolMenu.LargeSInventoryMenu;
import ToolMenu.NumericInputMenu;
import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.ShopFunction;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.ShopMainMenu;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SLongTextInput;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class SettingsMainMenu extends CategoricalSInventoryMenu {
    Man10Shop shop;
    Man10ShopV2 plugin;
    Player player;


    public SettingsMainMenu(Player p, Man10Shop shop, String startingCategory, Man10ShopV2 plugin) {
        super(new SStringBuilder().darkGray().text("ショップ設定").build(), startingCategory, plugin);
        this.player = p;
        this.shop = shop;
        this.plugin = plugin;
    }

    public ItemStack createNoPermissionItem(ItemStack item){
        SItemStack result = new SItemStack(item);
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§c§l§n権限がありません");
        result.setLore(lore);
        return result.build();
    }

    public void renderMenu(){
        //ArrayList<SInventoryItem> items = new ArrayList<>();

        //set items from function
        for(ShopFunction func: shop.functions){
            SInventoryItem item = func.getSettingItem(player, this, plugin);
            if(item == null)continue;
            if(func.enabledShopTypes().length != 0 && !ArrayUtils.contains(func.enabledShopTypes(), shop.shopType.getShopType())) continue;
            if(!func.hasPermissionToEdit(player.getUniqueId())){
                //if no permission to edit
                item = new SInventoryItem(createNoPermissionItem(item.getItemStack()));
                item.clickable(false);
            }
            addItem(func.settingCategory(), item);
            //items.add(item);
        }

        //admin items
        for(ShopFunction func: shop.functions){
            if(!shop.isAdminShop()) continue;
            SInventoryItem item = func.getAdminSettingItem(player, this, plugin);
            if(item == null)continue;
            if(func.enabledShopTypes().length != 0 && !ArrayUtils.contains(func.enabledShopTypes(), shop.shopType.getShopType())) continue;
            if(!func.hasPermissionToEdit(player.getUniqueId())){
                //if no permission to edit
                item = new SInventoryItem(createNoPermissionItem(item.getItemStack()));
                item.clickable(false);
            }
            addItem(func.settingCategory(), item);
        }

        setOnCloseEvent(e -> moveToMenu(player, new ShopMainMenu(player, shop, plugin)));
    }
}
