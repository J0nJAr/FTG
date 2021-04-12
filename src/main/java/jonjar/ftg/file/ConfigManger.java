package jonjar.ftg.file;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManger extends YamlManager{

    final int id;

    public ConfigManger(){
        super("config");
        this.id = 0;
    }
    public ConfigManger(int id){
        super("config "+id);
        this.id = id;
    }

    @Override
    protected void setDefault(YamlConfiguration yaml) {
        yaml.set("time",320);
    }
}
