package com.shojabon.man10shopv2.Utils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

public class SLongTextInput implements Listener {

    JavaPlugin plugin;

    Consumer<String> onConfirm = null;
    Consumer<Player> onCancel = null;

    ArrayList<UUID> playerInMenu = new ArrayList<>();

    String title;


    public SLongTextInput(String title, JavaPlugin plugin){
        this.plugin = plugin;
        this.title = title;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    public void open(Player p){
        for(int i = 0; i < 15; i++){
            p.sendMessage("");
        }
        p.sendMessage("§bキャンセルするには/cancelと入力するかキャンセルをクリックしてください");
        p.sendMessage("");
        TextComponent cancelButton = new TextComponent("§c§l[キャンセル]");
        p.sendMessage("");
        p.sendMessage("§a入力例: /<入力パラメータ> \n§b最初にスラッシュを入れてください");

        cancelButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cancel"));

        p.sendMessage("");
        p.sendMessage(title);
        p.sendMessage("");
        p.sendMessage(cancelButton);

        playerInMenu.add(p.getUniqueId());
    }

    public void setOnConfirm(Consumer<String> event){
        this.onConfirm = event;
    }

    public void setOnCancel(Consumer<Player> event){
        this.onCancel = event;
    }

    @EventHandler
    private void onPlayerExit(PlayerQuitEvent e){
        if(!playerInMenu.contains(e.getPlayer().getUniqueId())){
            return;
        }
        if(onCancel != null) onCancel.accept(e.getPlayer());
        playerInMenu.remove(e.getPlayer().getUniqueId());
        if(playerInMenu.size() == 0) HandlerList.unregisterAll(this);
    }

    @EventHandler
    private void onCommand(PlayerCommandPreprocessEvent e){
        if(!playerInMenu.contains(e.getPlayer().getUniqueId())){
            return;
        }
        e.setCancelled(true);
        String c = e.getMessage().substring(1);
        if(e.getMessage().equalsIgnoreCase("/cancel")) {
            for(int i = 0; i < 15; i++){
                e.getPlayer().sendMessage("");
            }
            if(onCancel != null) onCancel.accept(e.getPlayer());
            playerInMenu.remove(e.getPlayer().getUniqueId());
            if(playerInMenu.size() == 0) HandlerList.unregisterAll(this);
            return;
        }
        for(int i = 0; i < 15; i++){
            e.getPlayer().sendMessage("");
        }
        if(onConfirm != null) onConfirm.accept(c);
        playerInMenu.remove(e.getPlayer().getUniqueId());
        if(playerInMenu.size() == 0) HandlerList.unregisterAll(this);
    }



}
