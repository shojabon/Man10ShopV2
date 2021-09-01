package com.shojabon.man10shopv2.Commands;

import com.shojabon.man10shopv2.Commands.SubCommands.ShopsCommand;
import com.shojabon.man10shopv2.Commands.SubCommands.TogglePluginCommand;
import com.shojabon.man10shopv2.Commands.SubCommands.ToggleWorldCommand;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Utils.SCommandRouter.SCommandArgument;
import com.shojabon.man10shopv2.Utils.SCommandRouter.SCommandArgumentType;
import com.shojabon.man10shopv2.Utils.SCommandRouter.SCommandObject;
import com.shojabon.man10shopv2.Utils.SCommandRouter.SCommandRouter;
import org.bukkit.World;


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
                new SCommandObject()
                        .addArgument(new SCommandArgument().addAllowedString("shops")).
                        
                        addRequiredPermission("man10shopv2.shops").addExplanation("自分が管理できるショップ一覧").
                        setExecutor(new ShopsCommand(plugin))
        );

        //toggled worlds command

        addCommand(
                new SCommandObject()
                        .addArgument(new SCommandArgument().addAllowedString("toggleWorld")).

                        addRequiredPermission("man10shopv2.toggleWorld")
                        .addExplanation("看板が機能するワールド一覧を表示する")
                        .setExecutor(new ToggleWorldCommand(plugin))
        );

        addCommand(
                new SCommandObject()
                        .addArgument(new SCommandArgument().addAllowedString("toggleWorld")).
                        addArgument(new SCommandArgument().addAllowedType(SCommandArgumentType.WORLD).addAlias("ワールド名")).

                        addRequiredPermission("man10shopv2.toggleWorld")
                        .addExplanation("看板が機能するワールドの有効/無効を設定")
                        .setExecutor(new ToggleWorldCommand(plugin))
        );

        //toggle plugin command

        addCommand(
                new SCommandObject()
                        .addArgument(new SCommandArgument().addAllowedString("togglePlugin")).

                        addRequiredPermission("man10shopv2.togglePlugin")
                        .addExplanation("プラグインが有効かどうかを表示する")
                        .setExecutor(new TogglePluginCommand(plugin))
        );

        addCommand(
                new SCommandObject()
                        .addArgument(new SCommandArgument().addAllowedString("togglePlugin")).
                        addArgument(new SCommandArgument().addAlias("有効/無効").addAllowedType(SCommandArgumentType.BOOLEAN)).

                        addRequiredPermission("man10shopv2.togglePlugin")
                        .addExplanation("プラグインの有効/無効を設定")
                        .setExecutor(new TogglePluginCommand(plugin))
        );

    }

}
