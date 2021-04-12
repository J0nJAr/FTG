package jonjar.ftg.file;

import jonjar.ftg.entity.PlayerInfo;
import jonjar.ftg.entity.Team;
import org.bukkit.configuration.file.YamlConfiguration;

public class DataManager extends YamlManager{
    public DataManager(String name) {
        super("GAMEDATA", name, true);
    }

    @Override
    protected void setDefault(YamlConfiguration yaml) {
        for (String t : Team.TeamNames) {
            this.getYaml().set(t + ".place", "등수를 이곳에 입력");
        }
        PlayerInfo.PlayerInfoList.values().forEach(pi -> pi.stats.saveData());
    }

}
