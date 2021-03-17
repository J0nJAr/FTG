package jonjar.ftg.entity;

import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.UUID;

public class Team {

    private static Scoreboard BOARD;

    public static void initScoreboardManager(Scoreboard sbm){
        BOARD = sbm;
    }



    private final String name;

    private org.bukkit.scoreboard.Team team;

    public Team(String name){
        this.name = name;
    }

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



}
