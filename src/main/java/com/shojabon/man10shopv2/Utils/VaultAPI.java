package com.shojabon.man10shopv2.Utils;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

/**
 * Created by sho on 2017/07/21.
 */
public class VaultAPI {
    public static Economy economy = null;

    public VaultAPI(){
        setupEconomy();
    }

    private boolean setupEconomy() {
        Bukkit.getLogger().info("setupEconomy");
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            Bukkit.getLogger().warning("Vault plugin is not installed");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            Bukkit.getLogger().warning("Can't get vault service");
            return false;
        }
        economy = rsp.getProvider();
        Bukkit.getLogger().info("Economy setup");
        return economy != null;
    }

    /////////////////////////////////////
    //      残高確認
    /////////////////////////////////////
    public double  getBalance(UUID uuid){
        return economy.getBalance(Bukkit.getOfflinePlayer(uuid).getPlayer());
    }

    /////////////////////////////////////
    //      残高確認
    /////////////////////////////////////
    public void showBalance(UUID uuid){
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid).getPlayer();
        double money = getBalance(uuid);
        p.getPlayer().sendMessage(ChatColor.YELLOW + "あなたの所持金は$" + money);
    }
    /////////////////////////////////////
    //      引き出し
    /////////////////////////////////////
    public Boolean  withdraw(UUID uuid, double money){
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        if(p == null){
            Bukkit.getLogger().info(uuid.toString()+"は見つからない");
            return false;
        }
        EconomyResponse resp = economy.withdrawPlayer(p,money);
        if(resp.transactionSuccess()){
            if(p.isOnline()) {
                p.getPlayer().sendMessage(ChatColor.YELLOW + "$" + money + "支払いました");
            }
            return true;
        }
        return  false;
    }
    /////////////////////////////////////
    //      お金を入れる
    /////////////////////////////////////
    public Boolean  deposit(UUID uuid,double money){
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        if(p == null){
            Bukkit.getLogger().info(uuid.toString()+"は見つからない");

            return false;
        }
        EconomyResponse resp = economy.depositPlayer(p,money);
        if(resp.transactionSuccess()){
            if(p.isOnline()){
                p.getPlayer().sendMessage(ChatColor.YELLOW + "$"+money+"受取りました");
            }
            return true;
        }

        return  false;
    }

    /////////////////////////////////////
    //      引き出し
    /////////////////////////////////////
    public Boolean  silentWithdraw(UUID uuid, double money){
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        if(p == null){
            Bukkit.getLogger().info(uuid.toString()+"は見つからない");
            return false;
        }
        EconomyResponse resp = economy.withdrawPlayer(p,money);
        if(resp.transactionSuccess()){
            return true;
        }
        return  false;
    }
    /////////////////////////////////////
    //      お金を入れる
    /////////////////////////////////////
    public Boolean  silentDeposit(UUID uuid,double money){
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        if(p == null){
            Bukkit.getLogger().info(uuid.toString()+"は見つからない");

            return false;
        }
        EconomyResponse resp = economy.depositPlayer(p,money);
        if(resp.transactionSuccess()){
            return true;
        }
        return  false;
    }


    public String complexJpyBalForm(Long val){
        if(val < 10000){
            return String.valueOf(val);
        }
        if(val < 100000000){
            long man = val/10000;
            String left = String.valueOf(val).substring(String.valueOf(val).length() - 4);
            if(Long.parseLong(left) == 0){
                return man + "万";
            }
            return man + "万" + Long.parseLong(left);
        }
        if(val < 100000000000L){
            long oku = val/100000000;
            String man = String.valueOf(val).substring(String.valueOf(val).length() - 8);
            String te = man.substring(0, 4);
            String left = String.valueOf(val).substring(String.valueOf(val).length() - 4);
            if(Long.parseLong(te)  == 0){
                if( Long.parseLong(left) == 0){
                    return oku + "億";
                }else{
                    return oku + "億"+ Long.parseLong(left);
                }
            }else{
                if( Long.parseLong(left) == 0){
                    return oku + "億" + Long.parseLong(te) + "万";
                }
            }
            return oku + "億" + Long.parseLong(te) + "万" + Long.parseLong(left);
        }
        return "Null";
    }
}