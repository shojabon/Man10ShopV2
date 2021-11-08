package com.shojabon.man10shopv2.Commands;

import com.shojabon.man10shopv2.Commands.SubCommands.*;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.mcutils.Utils.SCommandRouter.SCommandArgument;
import com.shojabon.mcutils.Utils.SCommandRouter.SCommandArgumentType;
import com.shojabon.mcutils.Utils.SCommandRouter.SCommandObject;
import com.shojabon.mcutils.Utils.SCommandRouter.SCommandRouter;
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
        addCommand(
                new SCommandObject()
                        .addArgument(new SCommandArgument().addAllowedString("adminShops")).

                        addRequiredPermission("man10shopv2.admin.shops").addExplanation("管理者ショップ一覧").
                        setExecutor(new AdminShopsCommand(plugin))
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

        //create shop command

        addCommand(
                new SCommandObject()
                        .addArgument(new SCommandArgument().addAllowedString("create")).
                        addArgument(new SCommandArgument().addAlias("ショップ名")).

                        addRequiredPermission("man10shopv2.shop.create")
                        .addExplanation("ショップを作成")
                        .setExecutor(new CreateShopCommand(plugin))
        );

        addCommand(
                new SCommandObject()
                        .addArgument(new SCommandArgument().addAllowedString("createAdmin")).
                        addArgument(new SCommandArgument().addAlias("ショップ名")).

                        addRequiredPermission("man10shopv2.admin.shop.create")
                        .addExplanation("管理者ショップを作成")
                        .setExecutor(new CreateAdminShopCommand(plugin))
        );

        //reload command

        addCommand(
                new SCommandObject()
                        .addArgument(new SCommandArgument().addAllowedString("reload")).

                        addRequiredPermission("man10shopv2.reload")
                        .addExplanation("プラグインをリロードする")
                        .addExplanation("")
                        .addExplanation("設定を変更したときに使用する")
                        .addExplanation("コマンドを使用するとサーバー起動時状態に戻る")
                        .setExecutor(new ReloadConfigCommand(plugin))
        );

//        addCommand(
//                new SCommandObject()
//                        .addArgument(new SCommandArgument().addAllowedString("test"))
//                        .setExecutor(new TestCommand(plugin))
//        );

        //open shop for player
        addCommand(
                new SCommandObject()
                        .addArgument(new SCommandArgument().addAllowedString("open"))
                        .addArgument(new SCommandArgument().addAllowedType(SCommandArgumentType.ONLINE_PLAYER))
                        .addArgument(new SCommandArgument())
                        .setExecutor(new OpenShopCommand(plugin))
        );

    }

}
