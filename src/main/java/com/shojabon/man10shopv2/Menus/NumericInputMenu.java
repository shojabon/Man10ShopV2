package com.shojabon.man10shopv2.Menus;

import com.shojabon.man10shopv2.Utils.BannerDictionary;
import com.shojabon.man10shopv2.Utils.SInventory.SInventory;
import com.shojabon.man10shopv2.Utils.SInventory.SInventoryItem;
import com.shojabon.man10shopv2.Utils.SItemStack;
import com.shojabon.man10shopv2.Utils.SStringBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.naming.InterruptedNamingException;
import java.util.function.Consumer;

public class NumericInputMenu extends SInventory{

    Consumer<Integer> onConfirm;
    Consumer<InventoryClickEvent> onCancel;
    Consumer<InventoryCloseEvent> onClose;

    BannerDictionary dictionary = new BannerDictionary();
    ItemStack information;

    int currentValue = 0;
    int maxValue = -1;
    int maxDigits = 9;
    boolean allowZero = true;

    int[] numberDisplay = new int[]{8,7,6,5,4,3,2,1,0};
    int[] numberPad = new int[]{46, 37, 38, 39, 28, 29, 30, 19, 20, 21};

    public NumericInputMenu(String title, JavaPlugin plugin){
        super(title, 6, plugin);
    }


    public void setInformation(ItemStack item){
        information = item;
    }

    public void setDefaultValue(int value){
        currentValue = value;
    }

    public void setMaxValue(int value){
        maxValue = value;
    }

    public void setMaxDigits(int value){
        if(value > 9) value = 9;
        if(value < 1) value = 1;
        maxDigits = value;
    }

    public void setAllowZero(boolean value){
        allowZero = value;
    }


    public void setOnConfirm(Consumer<Integer> event){
        this.onConfirm = event;
    }

    public void setOnCancel(Consumer<InventoryClickEvent> event){
        this.onCancel = event;
    }

    public void setOnClose(Consumer<InventoryCloseEvent> event){
        this.onClose = event;
    }


    public void renderNumberDisplay(){
        for(int i = 0; i < maxDigits; i++){
            setItem(numberDisplay[i], new ItemStack(Material.AIR));
        }

        int lengthOfCurrentValue = String.valueOf(currentValue).length();
        for(int i = 0; i < lengthOfCurrentValue; i++){
            int nextCharacter = Integer.parseInt(String.valueOf(String.valueOf(currentValue).charAt(i)));
            SItemStack itemStack = new SItemStack(dictionary.getItem(nextCharacter));
            itemStack.setDisplayName(new SStringBuilder().aqua().bold().text(currentValue).build());
            setItem(numberDisplay[lengthOfCurrentValue-1-i], itemStack.build());
        }
        renderConfirmButton();
        renderInventory();
    }

    public void renderNumberPad(){
        for(int i = 0; i < 10; i++){
            SItemStack numberItem = new SItemStack(dictionary.getItem(i)).setDisplayName(new SStringBuilder().aqua().bold().text(i).build());
            SInventoryItem item = new SInventoryItem(numberItem.build());
            item.clickable(false);
            int nextNumber = i;

            item.setEvent(e -> {
                String currentString = String.valueOf(currentValue);


                //if starting with 0 change value
                if(currentValue == 0){
                    //if starting with 0 and next is also 0
                    if(nextNumber == 0){
                        return;
                    }
                    currentString = String.valueOf(nextNumber);
                }else{
                    currentString += nextNumber;
                }

                //if next value's digit is bigger than max
                if(currentString.length() > maxDigits) {
                    StringBuilder builder = new StringBuilder();
                    for(int ii = 0; ii < maxDigits; ii++){
                        builder.append("9");
                    }
                    currentValue = Integer.parseInt(builder.toString());
                    renderNumberDisplay();
                    return;
                }

                //if next value is bigger than max value
                if(Integer.parseInt(currentString) > maxValue && maxValue != -1){
                    currentValue = maxValue;
                    renderNumberDisplay();
                    return;
                }

                //finish
                currentValue = Integer.parseInt(currentString);
                renderNumberDisplay();
            });

            setItem(numberPad[i], item);
        }

        SItemStack deleteItem = new SItemStack(Material.TNT).setDisplayName(new SStringBuilder().red().bold().text("クリア").build());
        SInventoryItem deleteInventoryItem = new SInventoryItem(deleteItem.build());
        deleteInventoryItem.clickable(false);
        deleteInventoryItem.setEvent(e -> {
            currentValue = 0;
            renderNumberDisplay();
        });
        setItem(48, deleteInventoryItem);
    }

    public void renderConfirmButton(){
        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        if(!allowZero){
            if(currentValue == 0){
                setItem(41, background);
                return;
            }
        }
        SInventoryItem confirm = new SInventoryItem(new SItemStack(Material.LIME_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().green().bold().text("確認").build()).build());
        confirm.clickable(false);
        if(onConfirm != null) confirm.setEvent(e -> {onConfirm.accept(currentValue);});
        setItem(41, confirm);
    }

    public void renderMenu(){
        SInventoryItem background = new SInventoryItem(new SItemStack(Material.BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        background.clickable(false);
        fillItem(background);

        SInventoryItem cancel = new SInventoryItem(new SItemStack(Material.RED_STAINED_GLASS_PANE).setDisplayName(new SStringBuilder().red().bold().text("キャンセル").build()).build());
        cancel.clickable(false);
        if(onCancel != null) cancel.setEvent(onCancel);
        setItem(43, cancel);

        if(onClose != null) setOnCloseEvent(onClose);

        renderNumberDisplay();
        renderNumberPad();

        if(information != null){
            SInventoryItem invItem = new SInventoryItem(information);
            invItem.clickable(false);
            setItem(24, invItem);
        }



    }
}
