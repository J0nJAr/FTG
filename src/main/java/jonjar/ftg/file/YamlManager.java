package jonjar.ftg.file;

import jonjar.ftg.FTG;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public abstract class YamlManager {
    private File yamlFile;
    private YamlConfiguration yaml;
    private final String name;
    private final boolean fromDefault;
    private final String DEFAULT_PATH = "plugins/FTG/";

    public YamlManager(String name){
        this(null,name,true);
    }
    public YamlManager(String path, String name,boolean fromDefault){
        this.name = name;
        this.fromDefault = fromDefault;
        yamlFile = new File(fromDefault ? DEFAULT_PATH+path:path,name+".yml");
        yaml= YamlConfiguration.loadConfiguration(yamlFile);
    }

    protected void loadYaml() {
        yamlFile = new File(DEFAULT_PATH,name+".yml");
        yaml= YamlConfiguration.loadConfiguration(yamlFile);
        try {
            if (!yamlFile.exists()) {
                setDefault(yaml);
                yaml.save(yamlFile);
            }
            yaml.load(yamlFile);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }
    protected abstract void setDefault(YamlConfiguration yaml);

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
