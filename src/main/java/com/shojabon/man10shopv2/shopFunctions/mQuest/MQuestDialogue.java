package com.shojabon.man10shopv2.shopFunctions.mQuest;

import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.annotations.ShopFunctionDefinition;
import com.shojabon.man10shopv2.dataClass.Man10Shop;
import com.shojabon.man10shopv2.dataClass.Man10ShopSetting;
import com.shojabon.man10shopv2.dataClass.ShopFunction;
import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SLongTextInput;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@ShopFunctionDefinition(
        name = "クエストストーリー設定",
        explanation = {"§fクエストで表示する際表示するストーリーの設定"},
        enabledShopType = {},
        iconMaterial = Material.BOOK,
        category = "一般設定",
        allowedPermission = Man10ShopPermission.MODERATOR,
        isAdminSetting = true
)
public class MQuestDialogue extends ShopFunction {

    //variables
    public Man10ShopSetting<String> dialogue = new Man10ShopSetting<>("mquest.dialogue", "");
    //init
    public MQuestDialogue(Man10Shop shop, Man10ShopV2 plugin) {
        super(shop, plugin);
    }


    //functions

    //====================
    // settings
    //====================

    @Override
    public String currentSettingString() {
        if(dialogue.get() == null){
            return "なし";
        }
        return dialogue.get().replace("|", "\n");
    }

    @Override
    public SInventoryItem getSettingItem(Player player, SInventoryItem item) {
        item.setAsyncEvent(e -> {
            //text input
            SLongTextInput textInput = new SLongTextInput("§d§lメッセージを入力してください 空白の場合はなしになります 改行は|", plugin);
            textInput.setOnConfirm(newMessage -> {
                if(newMessage.length() == 0){
                    dialogue.delete();
                    success(player, "メッセージを変更しました");
                    return;
                }
                if(!dialogue.set(newMessage)){
                    warn(player, "内部エラーが発生しました");
                    return;
                }
                success(player, "メッセージを変更しました");
            });

            textInput.setOnCancel(ee -> warn(player, "キャンセルしました"));


            textInput.open(player);
            SInventory.closeNoEvent(player, plugin);
        });

        return item;
    }
}
