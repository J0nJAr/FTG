package jonjar.ftg.entity;

import org.bukkit.ChatColor;
import org.bukkit.Material;
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

    private org.bukkit.scoreboard.Team team;

    private List<Tile> tiles;

    public Team(TeamColor tc){
        this.tc = tc;
        TeamList.add(this);
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
        team.setColor(tc.getColor());
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



    public enum TeamColor {

        BLUE(ChatColor.AQUA, 11, "블루"),
        RED(ChatColor.RED, 14, "레드"),
        GREEN(ChatColor.GREEN, 5, "그린"),
        YELLOW(ChatColor.YELLOW, 4, "옐로우"),
        ORANGE(ChatColor.GOLD, 1, "오렌지"),
        PINK(ChatColor.LIGHT_PURPLE, 6, "핑크");

        private final ChatColor cc;
        private final int data;
        private final String korean;
        TeamColor(ChatColor cc, int data, String korean){
            this.cc = cc;
            this.data = data;
            this.korean = korean;
        }

        public ChatColor getColor() { return this.cc; }
        public short getData() { return (short) this.data; }

        public String getKoreanName() {
            return cc + korean;
        }
    }

}
