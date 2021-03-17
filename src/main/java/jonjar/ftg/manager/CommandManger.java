package jonjar.ftg.manager;

import jonjar.ftg.FTG;
import jonjar.ftg.entity.Tile;
import jonjar.ftg.util.MsgSender;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandManger extends MsgSender implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!(sender instanceof Player)){
            error(sender, "플레이어만 사용 가능한 명령어입니다.");
            return true;
        }

        Player p = (Player) sender;

        if(!p.isOp()){
            error(sender, "§c권한이 없습니다.");
            return true;
        }



        if(args.length == 0)
            help(p);
        else if(args[0].equalsIgnoreCase("start"))
            start(p);
        else if(args[0].equalsIgnoreCase("stop"))
            stop(p);
        else if(args[0].equalsIgnoreCase("pause"))
            pause(p);
        else if(args[0].equalsIgnoreCase("team"))
            team(p);
        else if(args[0].equalsIgnoreCase("observer"))
            observer(p);
        else if(args[0].equalsIgnoreCase("debug"))
            debug(p,args);
        else
            help(p);
        
        return true;
    }


    private void start(Player p){

    }
    private void stop(Player p) {
    }

    private void pause(Player p) {
    }

    private void team(Player p) {
    }

    private void observer(Player p) {
    }

    private void debug(Player p, String[] args) {

        if(args.length>0){
            switch (args[1]){
                case "setTile":
                    for (Tile tile: Tile.TileSet) {
                        for(Block bl:tile.getBlocks()){
                            bl.setType(Material.WHITE_CONCRETE);
                        }
                    }
                case "register":
                    Tile.registerTiles();
                    msg(p, "등록함");
            }
        }
    }




    private void help(Player p){
        msg(p, "§a/ftg start §f게임을 시작합니다.");
        msg(p, "§a/ftg stop §f게임을 종료합니다.");
        msg(p, "§a/ftg pause §f게임을 일시 정지합니다.");
        msg(p, "§a/ftg team §f팀 관련 명령어를 확인합니다.");
        msg(p, "§a/ftg observer §f관전자 관련 명령어를 확인합니다.");
    }

    
}
