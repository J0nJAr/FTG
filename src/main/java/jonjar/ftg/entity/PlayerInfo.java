package jonjar.ftg.entity;

import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerInfo {

    private static HashMap<String, PlayerInfo> PlayerInfoList = new HashMap<>();

    public static PlayerInfo getPlayerInfo(Player p){
        return PlayerInfoList.get(p.getName());
    }


    private final String name;
    private final UUID uuid;

    private boolean observe = false;
    private Team team;


    public PlayerInfo(String name, UUID uuid){
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() { return this.name; }
    public UUID getUUID() { return this.uuid; }
    public Team getTeam() { return this.team; }
    public boolean isObserver() { return this.observe; }

    public void setObserve(boolean observe){
        this.observe = observe;
    }

    public void leaveTeam(){
        if(team != null){
            team.removePlayer(uuid);
            team = null;
        }
    }

    public void joinTeam(Team team){
        if(team != null)
            leaveTeam();
        team.addPlayer(uuid);
        this.team = team;
    }

}
