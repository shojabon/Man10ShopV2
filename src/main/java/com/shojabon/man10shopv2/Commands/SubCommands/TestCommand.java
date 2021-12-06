package com.shojabon.man10shopv2.Commands.SubCommands;

import ToolMenu.AutoScaledMenu;
import ToolMenu.CategoricalSInventoryMenu;
import ToolMenu.TimeSelectorMenu;
import com.shojabon.man10shopv2.DataClass.lootBox.LootBox;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Man10ShopV2API;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.BarterSettingMenu;
import com.shojabon.man10shopv2.Menus.Settings.InnerSettings.WeekdayShopToggleMenu;
import com.shojabon.mcutils.Utils.*;
import com.shojabon.mcutils.Utils.SInventory.SInventory;
import com.shojabon.mcutils.Utils.SInventory.SInventoryItem;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.UUID;

public class TestCommand implements CommandExecutor {
    Man10ShopV2 plugin;

    public TestCommand(Man10ShopV2 plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        LootBox box = Man10ShopV2.api.getShop(UUID.fromString("319ccf60-c356-478b-b317-f2b609e5e8d6")).lootBoxItemGroupFunction.getLootBox();
        for(int i = 0; i < 1000; i ++){
            ((Player)sender).getInventory().addItem(box.pickRandomItem());
        }
        return true;
    }
}
