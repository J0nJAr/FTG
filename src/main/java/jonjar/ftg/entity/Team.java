package jonjar.ftg.entity;

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

    public static Team getTeamByName(String name){
        for(Team t : TeamList){
            if(t.getName().equals(name.toLowerCase())) return t;
        }
        return null;
    }


    private final String name;
    private final Material symbol_mat;

    private org.bukkit.scoreboard.Team team;

    private List<Tile> tiles;

    public Team(String name){
        this.name = name;

        Material mat = Material.getMaterial(name.toUpperCase() + "_CONCRETE");
        symbol_mat = (mat != null ? mat : Material.BEDROCK);
    }

    public String getName() { return this.name; }
    public Material getSymbol(){ return this.symbol_mat; }
    public List<Tile> getTiles() { return this.tiles; }

    public void register(){
        team = BOARD.getTeam(name);
        if(team == null)
            team = BOARD.registerNewTeam(name);
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



}
