package jonjar.ftg.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class MsgSender {

    public void msg(CommandSender sender, String msg){
        sender.sendMessage("§e[FTG] §f" + msg);
    }

    public void error(CommandSender sender, String msg){
        sender.sendMessage("§c[FTG] " + msg);
    }

    public void broadcast(CommandSender sender, String msg){
        sender.sendMessage("§e[FTG] ");

    }
}