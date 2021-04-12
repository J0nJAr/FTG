package jonjar.ftg.entity;

import jonjar.ftg.FTG;
import jonjar.ftg.manager.YamlManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Calendar;
import java.util.EnumMap;
import java.util.HashMap;

public class PlayerStats{
    private PlayerInfo playerInfo;
    public EnumMap<Stats,Object> stat_map= new EnumMap<>(Stats.class);


    PlayerStats(PlayerInfo pi){
        playerInfo=pi;
        reset();
    }

    public void reset() {
        for(Stats stats:Stats.values()){
            stat_map.put(stats,0);
        }
    }

    public void add(Stats stats){
        stat_map.put(stats,stat_map.get(stats));
    }

    public void saveData(){
        FileConfiguration data =  FTG.INSTANCE.gm.getYaml();
        data.set(playerInfo.getTeam()+".Player",playerInfo.getName());
        for(Stats stats : Stats.values()){
        data.set(playerInfo.getTeam()+".Player."+playerInfo.getName()+"."+stats.name(),stat_map.get(stats));
        }
    }

    enum Stats {
        kill,death,tile_assist;
    }
}