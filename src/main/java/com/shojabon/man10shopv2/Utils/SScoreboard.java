package com.shojabon.man10shopv2.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;

public class SScoreboard {

    String objectiveName;

    public Scoreboard scoreboard;

    public ArrayList<String> displayText = new ArrayList<>();

    int maxLineIndex = 0;

    public SScoreboard(String objectiveName){
        this.objectiveName = objectiveName;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective(objectiveName, "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        String space = "";

        for (int i = 0; i < 15; i++) {
            displayText.add(space);
            space += " ";
        }
    }

    public void addPlayer(Player player){
        player.setScoreboard(scoreboard);
    }

    public void setTitle(String title){
        Objective obj = scoreboard.getObjective(objectiveName);
        if(obj == null) return;
        obj.setDisplayName(title);
    }

    public void addScore(String name, int amount) {
        Objective obj = scoreboard.getObjective(objectiveName);
        if(obj == null) return;
        Score score = obj.getScore(name);
        int point = score.getScore();
        score.setScore(point + amount);
    }

    public void setText(int lineNumber, String text){
        displayText.set(lineNumber, text);
        if(lineNumber > maxLineIndex) maxLineIndex = lineNumber;
    }

    public void renderText(){
        Objective obj = scoreboard.getObjective(objectiveName);
        if(obj == null) return;
        clearScoreboard();
        for(int i = 0; i < maxLineIndex+1; i++){
            Score score = obj.getScore(displayText.get(i));
            score.setScore((displayText.size() - i));
        }
    }

    public void clearScoreboard(){
        Objective obj = scoreboard.getObjective(objectiveName);
        if(obj == null) return;
        for(String text: scoreboard.getEntries()){
            scoreboard.resetScores(text);
        }
    }

    public void remove() {
        Objective obj = scoreboard.getObjective(objectiveName);
        if(obj == null) return;
        obj.unregister();
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
    }





}
