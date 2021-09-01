package com.shojabon.man10shopv2.Commands.SubCommands;

import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SLongTextInput;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TestCommand implements CommandExecutor {
    Man10ShopV2 plugin;

    public TestCommand(Man10ShopV2 plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        SLongTextInput inputPrompt = new SLongTextInput("ショップ名を入力してください", plugin);
        inputPrompt.setOnConfirm(e -> {
            sender.sendMessage(e);
        });
        inputPrompt.open((Player) sender);
        return true;
    }
}
