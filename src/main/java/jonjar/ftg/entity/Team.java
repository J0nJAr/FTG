package jonjar.ftg.entity;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team {

    private static Scoreboard BOARD;

    public static void initScoreboardManager(Scoreboard sbm){
        BOARD = sbm;
    }


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

    public static void resetAll(){
        for(Team t : TeamList)
            t.unregister();
        TeamList.clear();
    }


    private final TeamColor tc;
    private final ItemStack[] armors;

    private org.bukkit.scoreboard.Team team;

    private List<Tile> tiles;

    public Team(TeamColor tc){
        this.tc = tc;

        armors = new ItemStack[4];
        armors[0] = color(Material.LEATHER_HELMET);
        armors[1] = color(Material.LEATHER_CHESTPLATE);
        armors[2] = color(Material.LEATHER_LEGGINGS);
        armors[3] = color(Material.LEATHER_BOOTS);

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
    }

    public void unregister(){
        team.unregister();
        team = null;
    }

    public org.bukkit.scoreboard.Team getTeam() {
        return team;
    }

    public void addPlayer(UUID uuid){
        if(!team.hasEntry(uuid.toString()))
            team.addEntry(uuid.toString());
    }

    public void removePlayer(UUID uuid){
        if(team.hasEntry(uuid.toString()))
            team.addEntry(uuid.toString());
    }

    public void addTile(Tile tile){
        if(!tiles.contains(tile))
            tiles.add(tile);
    }

    public void removeTile(Tile tile){
        tiles.remove(tile);
    }

    private ItemStack color(Material mat){
        ItemStack is = new ItemStack(mat);
        LeatherArmorMeta lam = (LeatherArmorMeta) is.getItemMeta();
        lam.setColor(tc.getColor());
        lam.setUnbreakable(true);
        is.setItemMeta(lam);
        return is;
    }

    public enum TeamColor {

        BLUE(ChatColor.AQUA, Color.BLUE ,11, "블루", 0, 12),
        RED(ChatColor.RED, Color.RED, 14, "레드", 0, 0),
        GREEN(ChatColor.GREEN, Color.GREEN, 5, "그린", -6, 6),
        YELLOW(ChatColor.YELLOW, Color.YELLOW, 4, "옐로우",-6, 0),
        ORANGE(ChatColor.GOLD, Color.ORANGE, 1, "오렌지", 6, 6),
        PINK(ChatColor.LIGHT_PURPLE, Color.PURPLE, 6, "핑크", 6, 0);

        private final ChatColor cc;
        private final Color color;
        private final int data;
        private final String korean;

        private final int baseX;
        private final int baseZ;
        TeamColor(ChatColor cc, Color color, int data, String korean, int x, int z){
            this.cc = cc;
            this.color = color;
            this.data = data;
            this.korean = korean;
            this.baseX = x;
            this.baseZ = z;
        }

        public int getBaseX() { return this.baseX; }
        public int getBaseZ() { return this.baseZ; }
        public Color getColor() { return this.color; }
        public ChatColor getChatColor() { return this.cc; }
        public short getData() { return (short) this.data; }

        public String getKoreanName() {
            return cc + korean;
        }

        public Tile getBaseTile(){
            return Tile.TILE_MAP.getTile(baseX, baseZ);
        }
    }

}
