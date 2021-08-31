package com.shojabon.man10shopv2.Commands;

import com.shojabon.man10shopv2.Commands.SubCommands.ShopsCommand;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Utils.SCommandRouter.SCommandArgument;
import com.shojabon.man10shopv2.Utils.SCommandRouter.SCommandArgumentType;
import com.shojabon.man10shopv2.Utils.SCommandRouter.SCommandObject;
import com.shojabon.man10shopv2.Utils.SCommandRouter.SCommandRouter;


public class Man10ShopV2Command extends SCommandRouter {

    Man10ShopV2 plugin;

    public Man10ShopV2Command(Man10ShopV2 plugin){
        this.plugin = plugin;
        registerCommands();
        registerEvents();
        pluginPrefix = Man10ShopV2.prefix;
    }

    public void registerEvents(){
        setNoPermissionEvent(e -> e.sender.sendMessage(Man10ShopV2.prefix + "§c§lあなたは権限がありません"));
        setOnNoCommandFoundEvent(e -> e.sender.sendMessage(Man10ShopV2.prefix + "§c§lコマンドが存在しません"));
    }

    public void registerCommands(){
        //shops command
        addCommand(
                new SCommandObject().addArgument(new SCommandArgument().addAllowedString("shops")).
                addRequiredPermission("man10shopv2.shops").addExplanation("自分が管理できるショップ一覧").
                setExecutor(new ShopsCommand(plugin))
        );
    }

}
