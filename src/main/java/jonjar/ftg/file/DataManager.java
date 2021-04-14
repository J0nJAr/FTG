package jonjar.ftg.file;

import jonjar.ftg.FTG;
import jonjar.ftg.entity.PlayerInfo;
import jonjar.ftg.entity.Team;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.EnumMap;
import java.util.logging.Level;

public class DataManager extends YamlManager{
    public static void loadInstance(){
    }

    public DataManager(String name) {
        super("GAMEDATA", name, true);
    }
    public DataManager(File file) {
        super(file);
    }

    @Override
    protected void setDefault(YamlConfiguration yaml) {
        saveData();
    }

    public void saveData(){
        YamlConfiguration data = getYaml();
        for (String t : Team.TeamNames) {
            Team team = Team.getTeamByName(t);
            team.stats.saveData(data);
            for(PlayerInfo pi : team.playerInfos){
                pi.stats.saveData(data);
            }
        }
    }
    public void resetData(){
        for (String t : Team.TeamNames) {
            Team team = Team.getTeamByName(t);
            team.stats.reset();
            for(PlayerInfo pi : team.playerInfos){
                pi.stats.reset();
            }
        }
    }

    private static abstract class AbstractStats {

        protected AbstractStats(){
        }

        public abstract void reset();
        public abstract void saveData(YamlConfiguration data);
    }
    public static class TeamStats extends AbstractStats{
        private final Team team;
        public EnumMap<Stats,Integer> stat_map;


        public TeamStats(Team team){
            this.team = team;
            stat_map= new EnumMap<>(Stats.class);
            reset();
        }
        @Override
        public void reset(){
            for(Stats stats: Stats.values()){ ;
                stat_map.put(stats,0);
            }
        }

        @Override
        public void saveData(YamlConfiguration data){
            for(Stats stats :Stats.values()){
                data.set(team.toString()+stats, stat_map.get(stats));
            }
        }

        public void add(Stats stat, int i){
            stat_map.put(stat,(int)stat_map.get(stat)+i);
        }
        public enum Stats {
            place, tiles;
        }
    }
    public static class PlayerStats extends AbstractStats{
        private PlayerInfo playerInfo;
        public EnumMap<Stats,Integer> stat_map;


        public PlayerStats(PlayerInfo pi){
            playerInfo=pi;
            stat_map = new EnumMap<>(Stats.class);
            reset();
        }
        @Override
        public void reset() {
            for(Stats stats: Stats.values()){
                stat_map.put(stats,0);
            }
        }

        public void add(Stats stats){
            add(stats,1);
        }
        public void add(Stats stats,int i){
            stat_map.put(stats,(int)stat_map.get(stats)+i);
        }

        @Override
        public void saveData(YamlConfiguration data){
            data.set(playerInfo.getTeam()+".Player",playerInfo.getName());
            for(Stats stats : Stats.values()){
            data.set(playerInfo.getTeam()+".Player."+playerInfo.getName()+"."+stats.name(),stat_map.get(stats));
            }
        }

       public enum Stats {
            kill,death,tile_assist;
        }
    }
}
