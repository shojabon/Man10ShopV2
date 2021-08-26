package com.shojabon.man10shopv2.Utils;

import java.awt.*;
import java.util.Random;

public class SStringBuilder {

    public StringBuilder stringBuilder;

    public SStringBuilder(String string){
        stringBuilder = new StringBuilder(string);
    }

    public SStringBuilder(){
        stringBuilder = new StringBuilder();
    }


    //============= default ===============

    public SStringBuilder bold(){
        stringBuilder.append("§l");
        return this;
    }

    public SStringBuilder obfuscated(){
        stringBuilder.append("§k");
        return this;
    }

    public SStringBuilder strike(){
        stringBuilder.append("§m");
        return this;
    }

    public SStringBuilder underline(){
        stringBuilder.append("§n");
        return this;
    }

    public SStringBuilder italic(){
        stringBuilder.append("§o");
        return this;
    }

    public SStringBuilder reset(){
        stringBuilder.append("§r");
        return this;
    }

    //============= color  =================

    public SStringBuilder black(){
        stringBuilder.append("§0");
        return this;
    }

    public SStringBuilder darkBlue(){
        stringBuilder.append("§1");
        return this;
    }

    public SStringBuilder darkGreen(){
        stringBuilder.append("§2");
        return this;
    }

    public SStringBuilder darkAqua(){
        stringBuilder.append("§3");
        return this;
    }

    public SStringBuilder darkRed(){
        stringBuilder.append("§4");
        return this;
    }

    public SStringBuilder darkPurple(){
        stringBuilder.append("§5");
        return this;
    }

    public SStringBuilder gold(){
        stringBuilder.append("§6");
        return this;
    }

    public SStringBuilder gray(){
        stringBuilder.append("§7");
        return this;
    }

    public SStringBuilder darkGray(){
        stringBuilder.append("§8");
        return this;
    }

    public SStringBuilder blue(){
        stringBuilder.append("§9");
        return this;
    }

    public SStringBuilder green(){
        stringBuilder.append("§a");
        return this;
    }

    public SStringBuilder aqua(){
        stringBuilder.append("§b");
        return this;
    }

    public SStringBuilder red(){
        stringBuilder.append("§c");
        return this;
    }

    public SStringBuilder lightPurple(){
        stringBuilder.append("§d");
        return this;
    }

    public SStringBuilder yellow(){
        stringBuilder.append("§e");
        return this;
    }

    public SStringBuilder white(){
        stringBuilder.append("§f");
        return this;
    }

    //======================================

    public SStringBuilder text(String string){
        stringBuilder.append(string);
        return this;
    }

    public SStringBuilder text(int number){
        stringBuilder.append(number);
        return this;
    }

    public SStringBuilder hex(String hex){
        stringBuilder.append(hexColor(hex));
        return this;
    }

    public SStringBuilder randomized(String hex, int redRange, int blueRange, int greenRange){
        Color color = hex2Rgb(hex);
        redRange++;
        blueRange++;
        greenRange++;
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < stringBuilder.length(); i++){
            Random rand = new Random();
            int red = color.getRed() + rand.nextInt(redRange) - redRange/2;
            if (red < 0 ) red = 0;
            if(red > 255) red = 255;
            int blue = color.getBlue() + rand.nextInt(blueRange) - blueRange/2;
            if (blue < 0 ) blue = 0;
            if(blue > 255) blue = 255;
            int green = color.getGreen() + rand.nextInt(greenRange) - redRange/2;
            if (green < 0 ) green = 0;
            if(green > 255) green = 255;

            System.out.println(red + " " + blue + " " + green);
            builder.append(hexColor(String.format("#%02X%02X%02X", red,
                    blue,
                    green))).append(stringBuilder.charAt(i));
        }
        stringBuilder = builder;
        return this;
    }

    public SStringBuilder gradient(String hex1, String hex2){
        Color color1 = hex2Rgb(hex1);
        Color color2 = hex2Rgb(hex2);
        Color[] steps = calculateGradient(color1, color2, stringBuilder.length());
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < steps.length; i++){
            Color c = steps[i];
            String hex = String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
            builder.append(hexColor(hex)).append(stringBuilder.charAt(i));
        }
        stringBuilder = builder;
        return this;
    }

    private Color[] calculateGradient(Color color1, Color color2, int steps){
        Color[] colors = new Color[steps];
        for(int i = 0; i < steps; i++){
            float p = i/((float)steps);
            colors[i] = new Color(((int)(color1.getRed()*(1-p) + color2.getRed()*p)),
                    ((int)(color1.getBlue()*(1-p) + color2.getBlue()*p)),
                    ((int)(color1.getGreen()*(1-p) + color2.getGreen()*p)));
        }
        return colors;
    }

    private static Color hex2Rgb(String colorStr) {
        return new Color(
                Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
    }

    public static String hexColor(String hexColor) throws NumberFormatException {
        if (hexColor.startsWith("#")) {
            hexColor = hexColor.substring(1); //fuck you im reassigning this.
        }
        if (hexColor.length() != 6) {
            return null;
        }
        Color.decode("#" + hexColor);
        StringBuilder assembledColorCode = new StringBuilder();
        assembledColorCode.append("\u00a7x");
        for (char curChar : hexColor.toCharArray()) {
            assembledColorCode.append("\u00a7").append(curChar);
        }
        return assembledColorCode.toString();
    }

    public String build(){
        return stringBuilder.toString();
    }
}
