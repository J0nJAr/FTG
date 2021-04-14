package jonjar.ftg.entity;

import jonjar.ftg.file.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class Team {

    private static Scoreboard BOARD;

    public static void initScoreboardManager(Scoreboard sbm){
        BOARD = sbm;
    }
    public Set<PlayerInfo> playerInfos;

    private static List<Team> TeamList = new ArrayList<Team>();

    public static Team getTeamByColor(TeamColor tc){
        for(Team t : TeamList){
            if(t.getColor() == tc) return t;
        }
        return null;
    }

    public static Team getTeamByName(String name){
        for(Team t : TeamList){
            if(t.getColor().name().equalsIgnoreCase(name)) return t;
        }
        return null;
    }

    public static List<Team> getTeams(){
        return TeamList;
    }
    public static List<String> TeamNames;

    public static void resetAll(){

        Scoreboard sc = Bukkit.getScoreboardManager().getMainScoreboard();
        sc.getObjective("tile").unregister();
        Objective obj = sc.registerNewObjective("tile", "dummy");
        obj.setDisplayName("§l점령한 타일");

        TeamList.clear();

    }


    private final TeamColor tc;
    private final ItemStack[] armors;


    private org.bukkit.scoreboard.Team team;

    private final List<Tile> tiles;


    public boolean cantRespawn;
    public boolean isSurvived;
    public int survivedPlayerNum;
    public DataManager.TeamStats stats;

    public Team(TeamColor tc){
        this.tc = tc;
        this.tiles = new ArrayList<>();

        stats = new DataManager.TeamStats(this);
        playerInfos = new HashSet<>();


        isSurvived = true;
        cantRespawn =false;
        armors = new ItemStack[4];
        armors[3] = color(Material.LEATHER_HELMET);
        armors[2] = color(Material.LEATHER_CHESTPLATE);
        armors[1] = color(Material.LEATHER_LEGGINGS);
        armors[0] = color(Material.LEATHER_BOOTS);
        TeamNames.add(this.getColor().name());
        TeamList.add(this);
    }

    public ItemStack[] getArmors() {
        ItemStack[] clone = new ItemStack[4];
        for(int i=0;i<4;i++)
            clone[i] = armors[i].clone();
        return clone;
    }
    public TeamColor getColor() { return this.tc; }
    public List<Tile> getTiles() { return this.tiles; }

    public void register(){
        team = BOARD.getTeam(tc.name());
        if(team == null)
            team = BOARD.registerNewTeam(tc.name());

        team.setAllowFriendlyFire(false);
        team.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
        team.setCanSeeFriendlyInvisibles(true);
        team.setColor(tc.getChatColor());

        Scoreboard sc = Bukkit.getScoreboardManager().getMainScoreboard();
        sc.getObjective("tile").getScore(team.getColor() + team.getName()).setScore(0);
    }

    public void unregister(){
        for(String uuid : team.getEntries()){
            PlayerInfo pi = PlayerInfo.getPlayerInfoByUUID(uuid);
            if(pi != null)
                pi.leaveTeam();
        }
    }

    public org.bukkit.scoreboard.Team getTeam() {
        return team;
    }

    public void addPlayer(String name){
        if(!team.hasEntry(name))
            team.addEntry(name);
    }

    public void removePlayer(String name){
        if(team.hasEntry(name))
            team.addEntry(name);
    }

    public void addTile(Tile tile){
        if(!tiles.contains(tile))
            tiles.add(tile);
        Tile.TILE_MAP.empty_tiles_set.remove(tile);
        tile.colorAll(this);
        BOARD.getObjective("tile").getScore(team.getColor() + team.getName()).setScore(tiles.size());
        stats.add(DataManager.TeamStats.Stats.tiles,1);
    }

    public void removeTile(Tile tile){
        tiles.remove(tile);
        tile.colorAll(null);
        BOARD.getObjective("tile").getScore(team.getColor() + team.getName()).setScore(tiles.size());
        stats.add(DataManager.TeamStats.Stats.tiles,-1);
    }

    private ItemStack color(Material mat){
        ItemStack is = new ItemStack(mat);
        LeatherArmorMeta lam = (LeatherArmorMeta) is.getItemMeta();
        lam.setColor(tc.getColor());
        lam.setUnbreakable(true);
        is.setItemMeta(lam);
        return is;
    }

    @Override
    public String toString(){
        return this.getColor().name();
    }
    public enum TeamColor {

        BLUE(ChatColor.AQUA, BarColor.BLUE, Color.BLUE ,11, "블루", 0, 12),
        RED(ChatColor.RED, BarColor.RED, Color.RED, 14, "레드", 0, 0),
        GREEN(ChatColor.GREEN, BarColor.GREEN, Color.GREEN, 5, "그린", -6, 6),
        YELLOW(ChatColor.YELLOW, BarColor.YELLOW, Color.YELLOW, 4, "옐로우",-6, 0),
        WHITE(ChatColor.WHITE, BarColor.WHITE, Color.WHITE, 0, "화이트", 6, 6),
        PINK(ChatColor.LIGHT_PURPLE, BarColor.PINK, Color.PURPLE, 6, "핑크", 6, 0);

        private final ChatColor cc;
        private final BarColor bc;
        private final Color color;
        private final int data;
        private final String korean;

        private final int baseX;
        private final int baseZ;

        private Inventory inv;

        TeamColor(ChatColor cc, BarColor bc, Color color, int data, String korean, int x, int z){
            this.cc = cc;
            this.bc = bc;
            this.color = color;
            this.data = data;
            this.korean = korean;
            this.baseX = x;
            this.baseZ = z;
        }
        public void setInventory(Inventory inv){
            this.inv = inv;
        }

        public Inventory getInventory(){
            return inv;
        }

        public int getBaseX() { return this.baseX; }
        public int getBaseZ() { return this.baseZ; }
        public Color getColor() { return this.color; }
        public ChatColor getChatColor() { return this.cc; }
        public BarColor getBarColor() { return this.bc; }
        public short getData() { return (short) this.data; }

        public String getKoreanName() {
            return cc + korean;
        }
        public String getKoreanName(String head) {
            return head + korean;
        }

        public Tile getBaseTile(){
            return Tile.TILE_MAP.getTile(baseX,baseZ);
        }
    }

}
