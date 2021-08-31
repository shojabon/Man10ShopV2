package com.shojabon.man10shopv2.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SItemStack {

    private ItemStack item = null;

    public SItemStack(ItemStack item){
        this.item = item;
    }

    public SItemStack(Material type){
        this.item = new ItemStack(type);
    }

    public ItemStack build(){
        return this.item;
    }

    //identification methods

    public static SItemStack fromBase64(String base64){
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return new SItemStack(items[0]);
        } catch (Exception e) {
            return null;
        }
    }

    public String getBase64(ItemStack item){
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(1);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    public String getBase64(){
        return getBase64(this.item);
    }

    public String getMD5(ItemStack item){
        try {
            byte[] result =
                    MessageDigest.getInstance("MD5")
                            .digest(this.getBase64(item)
                                    .getBytes(StandardCharsets.UTF_8))
            ;
            return String.format("%020x", new BigInteger(1, result));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getMD5(){
        return this.getMD5(this.item);
    }

    //ItemStack identification functions

    public ItemStack getTypeItem(){
        ItemStack clone = this.item.clone();

        //set durability to 0
        if(clone.hasItemMeta()){
            ItemMeta itemMeta = clone.getItemMeta();
            ((Damageable) itemMeta).setDamage(0);
            clone.setItemMeta(itemMeta);
        }
        clone.setAmount(1);

        return clone;
    }

    public String getItemTypeBase64(){
        return getBase64(this.getTypeItem());
    }

    public String getItemTypeMD5(){
        return this.getMD5(this.getTypeItem());
    }

    //utils

    public SItemStack addFlag(ItemFlag itemFlag){
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.addItemFlags(itemFlag);
        item.setItemMeta(itemMeta);
        return this;
    }

    public SItemStack addEnchantment(Enchantment enchant, int level){
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.addEnchant(enchant, level, true);
        item.setItemMeta(itemMeta);
        return this;
    }

    public SItemStack setGlowingEffect(boolean enabled) {
        if(enabled){
            this.addFlag(ItemFlag.HIDE_ENCHANTS);
            this.addEnchantment(Enchantment.LURE, 1);
        }else{
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemMeta.removeEnchant(Enchantment.LURE);
            item.setItemMeta(itemMeta);
        }
        return this;
    }

    public Material getType(){
        return this.item.getType();
    }

    //==================== display name =======================

    public String getDisplayName(){
        if(!this.item.hasItemMeta()){
            return this.item.getI18NDisplayName();
        }
        if(!this.item.getItemMeta().hasDisplayName()){
            return this.item.getI18NDisplayName();
        }
        return this.item.getItemMeta().getDisplayName();
    }

    public SItemStack setDisplayName(String name){
        ItemMeta itemMeta = this.item.getItemMeta();
        itemMeta.setDisplayName(name);
        this.item.setItemMeta(itemMeta);
        return this;
    }

    public SItemStack addDisplayName(String name){
        String currentName = getDisplayName();
        return setDisplayName(currentName + name);
    }

    //========================================================

    public SItemStack setLore(List<String> lore){
        ItemMeta itemMeta = this.item.getItemMeta();
        itemMeta.setLore(lore);
        this.item.setItemMeta(itemMeta);
        return this;
    }

    public SItemStack addLore(String lore){
        List<String> lores = getLore();
        lores.add(lore);
        return setLore(lores);
    }

    public List<String> getLore(){
        ItemMeta itemMeta = this.item.getItemMeta();
        if(!itemMeta.hasLore()){
            return new ArrayList<>();
        }
        return itemMeta.getLore();
    }

    public SItemStack setAmount(int amount){
        this.item.setAmount(amount);
        return this;
    }

    public boolean hasDamage(){
        ItemMeta itemMeta = this.item.getItemMeta();
        return ((Damageable) itemMeta).hasDamage();
    }

    public int getMaxStackSize(){
        return this.item.getMaxStackSize();
    }

    public int getDamage(){
        ItemMeta itemMeta = this.item.getItemMeta();
        return ((Damageable) itemMeta).getDamage();
    }

    public SItemStack setDamage(int damage){
        ItemMeta itemMeta = this.item.getItemMeta();
        ((Damageable) itemMeta).setDamage(damage);
        this.item.setItemMeta(itemMeta);
        return this;
    }

    public int getAmount(){
        return this.item.getAmount();
    }

    public SItemStack setHeadOwner(UUID uuid){
        if(getType() != Material.PLAYER_HEAD) return this;
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        SkullMeta skullMeta = (SkullMeta) this.item.getItemMeta();
        skullMeta.setOwningPlayer(player);
        this.item.setItemMeta(skullMeta);
        return this;
    }

}
