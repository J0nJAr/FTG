package jonjar.ftg.manager;

import jonjar.ftg.FTG;
import jonjar.ftg.entity.PlayerInfo;
import jonjar.ftg.entity.Team;
import jonjar.ftg.entity.Tile;
import jonjar.ftg.util.ContainerUtil;
import jonjar.ftg.util.LocationUtil;
import jonjar.ftg.util.MsgSender;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class CommandManager extends MsgSender implements CommandExecutor {

    private final TeamManager tm;
    private final FTG main;

    public CommandManager(FTG plugin){
        tm = new TeamManager();
        main = plugin;
    }
    public static final List<String> CMD_MAIN = Arrays.asList("start","stop","pause","team","observer","debug","drops","stats");
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
        else if(args[0].equalsIgnoreCase("drops"))
            drops(p, args);
        else if(args[0].equalsIgnoreCase("team"))
            team(p, args);
        else if(args[0].equalsIgnoreCase("observer"))
            observer(p, args);
        else if(args[0].equalsIgnoreCase("stats"))
            stats(p, args);
        else if(args[0].equalsIgnoreCase("debug"))
            debug(p,args);
        else
            help(p);
        
        return true;
    }



    private void drops(Player p, String[] args) {
        if(args.length == 1)
            DropsManager.openGUI(p);
        else if(args[1].equalsIgnoreCase("save"))
            DropsManager.save();
        else if(args[1].equalsIgnoreCase("load"))
            DropsManager.load();
        DropsManager.openGUI(p);
    }

    private void start(Player p){
        main.getGameManager().start(p);
    }

    private void stop(Player p) {
        main.getGameManager().stop(p);
    }

    private void pause(Player p) {
        main.getGameManager().pause(p);
    }

    public final static List<String>
            CMD_TEAM = Arrays.asList("random", "setting", "join", "list", "leave", "reset");

    private void team(Player p, String[] args) {

        if(args.length == 1)
            tm.help(p);
        else if(args[1].equalsIgnoreCase("random"))
            tm.random(p, args);
        else if(args[1].equalsIgnoreCase("setting"))
            tm.setting(p, args);
        else if(args[1].equalsIgnoreCase("join"))
            tm.join(p, args);
        else if(args[1].equalsIgnoreCase("list"))
            tm.list(p);
        else if(args[1].equalsIgnoreCase("leave"))
            tm.leave(p, args);
        else if(args[1].equalsIgnoreCase("reset"))
            tm.reset(p);
        else
            tm.help(p);

    }
    public static HashMap<String,File> fileMap = new HashMap<>();
    private void stats(Player p, String[] args) {
        getStatsList();
        if(args.length == 1){
            fileMap.keySet().forEach(s -> msg_cmt(p,getCmt_Click(ChatColor.BOLD +"["+s+"]", new String[]{"stats", s})));
        }
        else {
            File file = fileMap.get(args[1]);
            if(file == null){
                error(p,"유효하지 않은 이름입니다.");
                error(p, "올바른 명령어 : /ftg stats [파일이름]");
            }
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            for(String team: yaml.getKeys(false)){
                msg(p,team);
                msg(p, yaml.getString(team+".place")+"위");
                try{
                ConfigurationSection team_yml = yaml.getConfigurationSection(team+".Player");
                for(String player: team_yml.getKeys(false)){
                    msg(p,player);
                    msg(p, "킬" +team_yml.getInt(player+".kill"));
                    msg(p, "죽음" +team_yml.getInt(player+".death"));
                    msg(p, "점령"+team_yml.getInt(player+".file_assistance"));
                }
                }
                catch (NullPointerException ne){
                    error(p,"플레이어 목록을 조회하는 중에 오류가 발생했습니다.");
                }
            }
        }
    }
    private static void getStatsList() {
         for(File file:new File("plugins/FTG/GAMEDATA/").listFiles()){
             fileMap.put(file.getName(),file);
         }
    }
    private void observer(Player p, String[] args) {
        if(args.length == 1)
            error(p, "올바른 명령어 : /ftg observer [PLAYER]");
        else {
            PlayerInfo pi = PlayerInfo.getPlayerInfo(args[1]);
            if(pi == null){
                error(p, args[1] + " 은(는) 없는 플레이어입니다.");
            } else {
                pi.setObserve(!pi.isObserver());
                msg(p, args[1] + " 님을 " + (pi.isObserver() ? "관전자" : "게임 참가자") + "로 설정했습니다.");
            }
        }
    }

    public final static List<String> CMD_DEBUG = Arrays.asList("setTile","register","testDistance","getTile","getChest", "fastStart","armor");

    private void debug(Player p, String[] args) {
        if(args.length==1){
            msg_cmt(p, new TextComponent(new ComponentBuilder("=======").
                            append("Debug").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/FTG debug")).color(net.md_5.bungee.api.ChatColor.YELLOW).
                            append("=======").color(net.md_5.bungee.api.ChatColor.WHITE).create()));
            msg_cmt(p, getCmt_Click(ChatColor.GRAY +"setTile", new String[]{"debug", "setTile"}));
            msg_cmt(p, getCmt_Click(ChatColor.GRAY +"register", new String[]{"debug", "register"}));
            msg_cmt(p, getCmt_Click(ChatColor.GRAY +"testDistance", new String[]{"debug", "testDistance","1"}));
            msg_cmt(p, getCmt_Click(ChatColor.GRAY +"getTile", new String[]{"debug", "getTile"}));
            msg_cmt(p, getCmt_Click(ChatColor.GRAY +"방어구 재지급", new String[]{"debug", "armor"}));
            msg_cmt(p, getCmt_Click(ChatColor.GRAY + "fastStart 2", new String[]{"debug", "fastStart", "2"}));
            msg_cmt(p, getCmt_Click(ChatColor.GRAY + "fastStart 3", new String[]{"debug", "fastStart", "3"}));
            msg_cmt(p, getCmt_Click(ChatColor.GRAY + "drop", new String[]{"debug", "drop"}));
            msg_cmt(p, getCmt_Click(ChatColor.GRAY + "test", new String[]{"debug", "test"}));
            msg_cmt(p, getCmt_Click("RELOAD", ChatColor.DARK_RED+""+ChatColor.BOLD+"FORCE RELOAD" ,new String[]{"confirm"}));
        }
        else {
            switch (args[1]){
                case "getInfos":
                    Tile t = PlayerInfo.getPlayerInfo(p).getNowLocation();
                    t.printAll();
                    break;
                case "fastStart":
                    p.chat("/ftg team setting " + args[2]);
                    p.chat("/ftg team random");
                    p.chat("/ftg start");
                    break;
                case "setTile":
                    for (Tile tile: Tile.TILE_SET) {
                        for(Block bl:tile.getBlocks()){
                            bl.setType(Material.CONCRETE);
                        }
                    }
                    break;
                case "register":
                    Tile.registerTiles();
                    msg(p, "타일 등록함");
                    Tile.registerAllNearTileList();
                    msg(p, "거리 등록함");
                    break;
                case "armor":
                    p.getInventory().addItem(Team.getTeamByColor(Team.TeamColor.valueOf(args[2])).getArmors());
                    break;
                case "testDistance":
                    int d;
                        try {
                            d=Integer.parseInt(args[2]);
                        }catch (Exception e){
                            d = 1;
                       }
                    for (int i = 0; i<=d;i++){
                        msg(p, "=============="+i+"============");
                        LocationUtil.getClosestTile(p.getLocation()).getNearTileList().get(i).forEach(s ->msg(p,s.toString()));
                    }
                    break;
                case "getTile" :
                    msg(p,LocationUtil.getClosestTile(p.getLocation()).toString());
                    break;
                case "drop" :
                    p.getInventory().addItem(DropsManager.getRandomDrop().getIcon(true));
                    break;
                case "test" :
                    p.openInventory(ContainerUtil.getFromShulker(DropsManager.getRandomDrop().getIcon()));
                    break;
            }
        }
    }




    private void help(Player p){
        msg(p, "§a/ftg start §f게임을 시작합니다.");
        msg(p, "§a/ftg stop §f게임을 종료합니다.");
        msg(p, "§a/ftg pause §f게임을 일시 정지합니다.");
        msg(p, "§a/ftg team §f팀 관련 명령어를 확인합니다.");
        msg(p, "§a/ftg observer [PLAYER] §f해당 플레이어의 관전자 여부를 설정합니다.");
    }

    
}
