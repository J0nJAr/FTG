package jonjar.ftg.manager;

import jonjar.ftg.FTG;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class YamlManager {
    private File yamlFile;
    private YamlConfiguration yaml;
    private final String name;

    public YamlManager(String name){
        this.name = name;
        loadYaml();
    }

    private void loadYaml() {

        yamlFile = new File("plugins/FTG",name+".yml");
        yaml= YamlConfiguration.loadConfiguration(yamlFile);

        try {
            if (!yamlFile.exists()) {
                if(name=="config") yaml.set("time",320);
                yaml.save(yamlFile);
            }
            yaml.load(yamlFile);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public FileConfiguration getYaml(){
        return yaml;
    }

    public void saveYaml(){
        try {
            yaml.save(yamlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
