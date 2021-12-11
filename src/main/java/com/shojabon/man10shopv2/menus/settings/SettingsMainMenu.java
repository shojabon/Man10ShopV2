package com.shojabon.man10shopv2.menus.settings;

import ToolMenu.CategoricalSInventoryMenu;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.menus.ShopMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SStringBuilder;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.entity.Player;
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
            if(!func.getClass().isAnnotationPresent(ShopFunctionDefinition.class)) continue;
            ShopFunctionDefinition shopFunctionDefinition = func.getClass().getAnnotation(ShopFunctionDefinition.class);
            if(!shop.isAdminShop() && shopFunctionDefinition.isAdminSetting())  continue; //if admin function

            SInventoryItem item = func.getSettingItem(player, func.getSettingBaseItem()); //get setting item icon
            if(item == null)continue;

            if(shopFunctionDefinition.enabledShopType().length != 0 && !ArrayUtils.contains(shopFunctionDefinition.enabledShopType(), shop.shopType.getShopType())) continue; //shop type check

            if(!shop.permission.hasPermissionAtLeast(player.getUniqueId(), shopFunctionDefinition.allowedPermission())){
                //if no permission to edit
                item = new SInventoryItem(createNoPermissionItem(item.getItemStack()));
                item.clickable(false);
            }
            addItem(shopFunctionDefinition.category(), item);
            //items.add(item);
        }

        setOnCloseEvent(e -> moveToMenu(player, new ShopMainMenu(player, shop, plugin)));
    }
}
