package com.shojabon.man10shopv2.Commands;

import com.shojabon.man10shopv2.Commands.SubCommands.ShopsCommand;
import com.shojabon.man10shopv2.Commands.SubCommands.TestCommand;
import com.shojabon.man10shopv2.Man10ShopV2;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Man10ShopV2Command implements @Nullable CommandExecutor {

    Man10ShopV2 plugin;

    public Man10ShopV2Command(Man10ShopV2 plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1 && args[0].equalsIgnoreCase("shops")){
            new ShopsCommand(plugin).execute(sender, args);
        }
        if(args.length == 1 && args[0].equalsIgnoreCase("test")){
            new TestCommand(plugin).execute(sender, args);
        }
        return false;
    }
}
