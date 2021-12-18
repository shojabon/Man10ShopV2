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
import org.bukkit.Material;
import org.bukkit.entity.Player;

@ShopFunctionDefinition(
        name = "コマンド説明設定",
        explanation = {"購入画面で簡単なコマンドの説明", "|で改行"},
        enabledShopType = {Man10ShopType.COMMAND},
        iconMaterial = Material.SPRUCE_SIGN,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class CommandShopExplanationFunction extends ShopFunction {

    //variables
    public Man10ShopSetting<String> explanation = new Man10ShopSetting<>("command.explanation", "");
    //init
    public CommandShopExplanationFunction(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions

    //====================
    // settings
    //====================

    @Override
    public String currentSettingString() {
        return explanation.get().replace("|", "\n");
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setAsyncEvent(e -> {
            //text input
            SLongTextInput textInput = new SLongTextInput("§d§l説明を入力してください 改行は\\n 空白の場合はその他になります", plugin);
            textInput.setOnConfirm(categoryName -> {
                if(categoryName.length() == 0) categoryName = "";
                if(!explanation.set(categoryName)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "説明を変更しました");
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
