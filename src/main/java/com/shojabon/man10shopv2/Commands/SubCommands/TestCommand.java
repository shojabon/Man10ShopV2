package com.shojabon.man10shopv2.Commands.SubCommands;

import ToolMenu.CategoricalSInventoryMenu;
import ToolMenu.TimeSelectorMenu;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.WeekdayShopToggleMenu;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import com.shojabon.mcutils.Utils.SItemStack;
import com.shojabon.mcutils.Utils.SLongTextInput;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

public class TestCommand implements CommandExecutor {
    Man10ShopV2 plugin;

    public TestCommand(Man10ShopV2 plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        CategoricalSInventoryMenu a = new CategoricalSInventoryMenu("test", "test2", plugin);
        for(int i = 0; i < 100; i++){
            //a.addItem("test1", new SInventoryItem(new SItemStack(Material.DIAMOND_BLOCK).setDisplayName(String.valueOf(i)).build()));
        }
        a.setCurrentCategory("test2");
        a.open(((Player)sender));
        return true;
    }
}
