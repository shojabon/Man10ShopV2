package com.shojabon.man10shopv2.Commands.SubCommands;

import com.shojabon.man10shopv2.DataClass.Man10Shop;
import com.shojabon.man10shopv2.DataClass.Man10ShopModerator;
import com.shojabon.man10shopv2.Enums.Man10ShopPermission;
import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.Menus.NumericInputMenu;
import com.shojabon.man10shopv2.Utils.MySQL.MySQLAPI;
import com.shojabon.man10shopv2.Utils.SItemStack;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TestCommand {
    Man10ShopV2 plugin;

    public TestCommand(Man10ShopV2 plugin){
        this.plugin = plugin;
    }

    public void execute(CommandSender sender, String[] args){
        if(! (sender instanceof Player)){
            sender.sendMessage(Man10ShopV2.prefix + "§c§lこのコマンドはプレイヤーのみが実行可能です");
            return;
        }
        Player p = ((Player)sender);

        new Thread(()->{
            plugin.getServer().getScheduler().runTaskLater(plugin, ()-> {
                p.sendMessage("a");
                Man10ShopV2.mysql.execute("SELECT SLEEP(5);");
                p.sendMessage("b");
            }, 0);
        }).start();



    }

}
