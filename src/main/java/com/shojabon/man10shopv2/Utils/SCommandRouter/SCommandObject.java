package com.shojabon.man10shopv2.Utils.SCommandRouter;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.function.Consumer;

public class SCommandObject {

    public ArrayList<SCommandArgument> arguments = new ArrayList<>();
    public ArrayList<CommandExecutor> executors = new ArrayList<>();
    public ArrayList<Consumer<SCommandData>> executorConsumers = new ArrayList<>();

    public ArrayList<String> requiredPermissions = new ArrayList<>();

    ArrayList<String> explanation = new ArrayList<>();

    public SCommandObject addExplanation(String explanation){
        this.explanation.add(explanation);
        return this;
    }

    public SCommandObject addArgument(SCommandArgument arg){
        arguments.add(arg);
        return this;
    }

    public SCommandObject setExecutor(CommandExecutor event){
        executors.add(event);
        return this;
    }

    public SCommandObject setExecutor(Consumer<SCommandData> event){
        executorConsumers.add(event);
        return this;
    }

    public SCommandObject addRequiredPermission(String permission){
        requiredPermissions.add(permission);
        return this;
    }

    public boolean hasPermission(Player p ){
        for(String perm : requiredPermissions){
            if(!p.hasPermission(perm)) return false;
        }
        return true;
    }

    public boolean matches(String[] args){
        if(args.length != arguments.size()) return false;
        for(int i = 0; i < args.length; i++){
            if(!arguments.get(i).matches(args[i])){
                return false;
            }
        }
        return true;
    }

    public boolean validOption(String[] args){
        if(args.length > arguments.size()) return false;
        for(int i = 0; i < args.length-1; i++){
            if(!arguments.get(i).matches(args[i])){
                return false;
            }
        }
        return true;
    }

    public void execute(SCommandData obj){
        for(CommandExecutor executor : executors){
            executor.onCommand(obj.sender, obj.command, obj.label, obj.args);
        }
        for(Consumer<SCommandData> event: executorConsumers){
            event.accept(obj);
        }
    }

    public BaseComponent[] helpText(String baseCommand, String prefix){
        ComponentBuilder builder = new ComponentBuilder();

        StringBuilder commandExplanation = new StringBuilder();
        for(String exp : explanation){
            commandExplanation.append(exp).append("\n");
        }
        if(explanation.size() != 0) commandExplanation.append("\n");
        commandExplanation.append("§e==========必要権限==========\n");
        if(requiredPermissions.size() != 0){
            for(String perm : requiredPermissions){
                commandExplanation.append("§d").append(perm).append("\n");
            }
        }else{
            commandExplanation.append("§d").append("なし").append("\n");
        }

        builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(commandExplanation.toString())));
        builder.append(prefix + "/" + baseCommand + " ");


        for(SCommandArgument arg : arguments){
            if(arg.allowedStrings.size() == 1){
                builder.append(prefix + arg.allowedStrings.get(0));
            }else{
                builder.append(prefix + "<" + arg.alias.get(0) + ">");
            }
            //explanation
            StringBuilder explanation = new StringBuilder();
            for(String exp : arg.explanation){
                explanation.append("§d").append(exp).append("\n");
            }
            if(arg.explanation.size() != 0) explanation.append("\n");
            if(arg.allowedTypes.size() != 0){
                explanation.append("§e==========使用可能パラメータ種==========\n");
                for(SCommandArgumentType type: arg.allowedTypes){
                    explanation.append("§d").append(type.name()).append("\n");
                }
            }

            if(arg.allowedStrings.size() != 0 && arg.allowedStrings.size() != 1){
                explanation.append("§e==========使用可能パラメータ==========\n");
                for(String argument: arg.allowedStrings){
                    explanation.append("§d").append(argument).append("\n");
                }
            }
            builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(explanation.toString())));
            builder.append(" ");
            builder.event((HoverEvent) null);
        }

        return builder.create();
    }



}
