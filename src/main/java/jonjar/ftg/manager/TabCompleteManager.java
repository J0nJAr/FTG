package jonjar.ftg.manager;




import jonjar.ftg.FTG;
import jonjar.ftg.entity.Team;
import jonjar.ftg.manager.CommandManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class TabCompleteManager implements TabCompleter {

    public TabCompleteManager(FTG ftg) {
        super();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {


        if (command.getName().equalsIgnoreCase("ftg")) {
            switch (args.length){
                case 1:
                    return getSortedArgs(args[0],CommandManager.CMD_MAIN);
                case 2:
                    if(args[0].equalsIgnoreCase("team")) return getSortedArgs(args[1],CommandManager.CMD_TEAM);
                    if(args[0].equalsIgnoreCase("debug")) return getSortedArgs(args[1],CommandManager.CMD_DEBUG);
                case 3:
                    if(args[0].equalsIgnoreCase("team")){
                        if(args[1].equalsIgnoreCase("join")) {
                            if(Team.TeamNames==null) return Arrays.asList("아직 팀이 설정되지 않았습니다.");
                            return getSortedArgs(args[2], Team.TeamNames);
                        }
                        if(args[1].equalsIgnoreCase("setting")) return getSortedArgs(args[2], "2","3","4","6");
                    }
            }
        }
        return null;
    }
    private List<String> getSortedArgs(String head, String... args){
        return getSortedArgs(head, Arrays.asList(args));
    }

    private List<String> getSortedArgs(String head, List<String> args){
        List<String> result = new ArrayList<String>();
        for (String a : args){
            if (a.toLowerCase().startsWith(head.toLowerCase())){
                result.add(a);
            }
        }
        return result;
    }
}