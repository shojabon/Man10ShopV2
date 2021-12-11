package com.shojabon.man10shopv2.commands.subCommands;

import ToolMenu.AutoScaledMenu;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TestCommand implements CommandExecutor {
    Man10ShopV2 plugin;

    public TestCommand(Man10ShopV2 plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
//        Man10Shop shop = Man10ShopV2.api.getShop(UUID.fromString("319ccf60-c356-478b-b317-f2b609e5e8d6"));
//        shop.mQuestFunction.refreshQuests(2);

        AutoScaledMenu s = new AutoScaledMenu("test1", plugin);
        AutoScaledMenu s1 = new AutoScaledMenu("test2", plugin);
        AutoScaledMenu s2 = new AutoScaledMenu("test3", plugin);

        s.addItem(new SInventoryItem(new ItemStack(Material.DIAMOND)).clickable(false).setEvent(e -> {
            s1.open(((Player)sender));
        }));

        s1.addItem(new SInventoryItem(new ItemStack(Material.GOLD_BLOCK)).clickable(false).setEvent(e -> {
            s2.open(((Player) sender));
        }));
        s1.setOnCloseEvent(e -> {
            s.open(((Player)sender));
        });

        s2.addItem(new SInventoryItem(new ItemStack(Material.COPPER_BLOCK)).clickable(false).setEvent(e -> {
            s.open(((Player)sender));
        }));
        s2.setOnCloseEvent(e -> {
            s1.open(((Player)sender));
        });

        s.open(((Player)sender));

        return true;
    }
}
