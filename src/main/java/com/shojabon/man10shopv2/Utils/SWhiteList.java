package com.shojabon.man10shopv2.Utils;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class SWhiteList implements @NotNull Listener {

    boolean enabled = false;

    String defaultKickMessage = "";

    Plugin plugin;

    String bypassPermission;

    ArrayList<UUID> allowedPlayers = new ArrayList<>();
    HashMap<UUID, String> kickMessages = new HashMap<>();

    public SWhiteList(Plugin plugin, String defaultKickMessage){
        this.plugin = plugin;
        this.defaultKickMessage = defaultKickMessage;
    }

    public void setKickMessages(UUID uuid, String message){
        kickMessages.put(uuid, message);
    }

    public void setBypassPermission(String permission){
        this.bypassPermission = permission;
    }

    public void addPlayer(UUID uuid){
        allowedPlayers.add(uuid);
    }

    public void removePlayer(UUID uuid){
        allowedPlayers.remove(uuid);
    }

    public void clearPlayers(){
        allowedPlayers.clear();
    }

    public void enable(){
        enabled = true;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void disable(){
        enabled = false;
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void playerJoinEvent(PlayerLoginEvent e){
        if(!enabled) return;

        if(allowedPlayers.contains(e.getPlayer().getUniqueId())) return;

        //has bypass permission
        if(bypassPermission != null && e.getPlayer().hasPermission(bypassPermission)) return;

        if(kickMessages.containsKey(e.getPlayer().getUniqueId())){
            //has kick message
            e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, kickMessages.get(e.getPlayer().getUniqueId()));
        }else{
            e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, defaultKickMessage);
        }
    }




}
