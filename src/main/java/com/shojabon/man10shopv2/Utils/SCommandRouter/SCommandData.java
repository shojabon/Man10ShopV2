package com.shojabon.man10shopv2.Utils.SCommandRouter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SCommandData {

    public CommandSender sender;
    public Command command;
    public String label;
    public String[] args;

    public SCommandData(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        this.sender = sender;
        this.command = command;
        this.label = label;
        this.args = args;
    }
}
