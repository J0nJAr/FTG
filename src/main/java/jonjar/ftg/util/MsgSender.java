package jonjar.ftg.util;

import jonjar.ftg.FTG;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import java.util.Arrays;

public class MsgSender {
    private static MsgSender msgSender;

    public static MsgSender getMsgSender() {
        if(msgSender==null) msgSender = new MsgSender();
        return msgSender;
    }

    public void msg(CommandSender sender, String msg){
        sender.sendMessage("§e[FTG] §f" + msg);
    }
    public void msg_cmt(CommandSender sender, BaseComponent Component){
        sender.spigot().sendMessage(new TextComponent("§e[FTG] §f"), Component);
    }

    public void error(CommandSender sender, String msg){
        sender.sendMessage("§c[FTG] " + msg);
    }

    public void broadcast(String msg){
        Bukkit.broadcastMessage("§e[FTG] §f" + msg);
    }


    public TextComponent getCmt_Click(String command, String text, String[] args){
        TextComponent result = new TextComponent(TextComponent.fromLegacyText(text));
        result.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/"+command+ Arrays.toString(args).
                        replace("["," ").
                        replace("]","").
                        replace(","," ")
        ));
        return result;
    }
    public TextComponent getCmt_Click(String text, String[] args){
        return getCmt_Click("FTG",text ,args);
    }

}