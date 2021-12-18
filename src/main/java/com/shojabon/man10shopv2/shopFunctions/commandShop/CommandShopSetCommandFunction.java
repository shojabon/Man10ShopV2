package com.shojabon.man10shopv2.shopFunctions.commandShop;

import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.man10shopv2.menus.settings.SettingsMainMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SLongTextInput;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@ShopFunctionDefinition(
        name = "実行コマンド設定",
        explanation = {"プレイヤー名:{name}", "プレイヤーUUID:{uuid}", "と設定", "スラッシュ不要"},
        enabledShopType = {Man10ShopType.COMMAND},
        iconMaterial = Material.COMMAND_BLOCK,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class CommandShopSetCommandFunction extends ShopFunction {

    //variables
    public Man10ShopSetting<String> command = new Man10ShopSetting<>("command.setCommand", "", true);
    //init
    public CommandShopSetCommandFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions

    //====================
    // settings
    //====================

    @Override
    public String currentSettingString() {
        return "/" + command.get();
    }

    @Override
    public boolean isAllowedToUseShop(Player p) {
        if(command.get().equalsIgnoreCase("")){
            warn(p, "コマンドがセットされていません");
            return false;
        }
        return true;
    }

    @Override
    public boolean performAction(Player p, int amount) {
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.get().replace("{name}", p.getName()).replace("{uuid}", p.getUniqueId().toString())));
        return true;
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setAsyncEvent(e -> {
            //text input
            SLongTextInput textInput = new SLongTextInput("§d§lカテゴリ名を入力してください 空白の場合はその他になります", plugin);
            textInput.setOnConfirm(categoryName -> {
                if(categoryName.length() == 0) categoryName = "";
                if(!command.set(categoryName)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
            });

            textInput.setOnCancel(ee -> {
                warn(player, "キャンセルしました");
                new SettingsMainMenu(player, shop, getDefinition().category(), plugin).open(player);
            });

            textInput.open(player);
            SInventory.closeNoEvent(player, plugin);
        });

        return item;
    }
}
