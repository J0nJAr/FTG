package jonjar.ftg.manager;




import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class TabCompleteManager implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("ftg")) {
            switch (args.length) {

            }
        }
        return null;
    }

}