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
        for(SETTINGS setting :SETTINGS.values()){
            yaml.set(setting.path,setting.default_value);
        }
    }

    public Integer getSetting(SETTINGS setting){
        return getYaml().getInt(setting.path);
    }

    public enum SETTINGS{
        time("time",320),
        fever("fever",1);


        String path;
        int default_value;

        SETTINGS(String path,int default_value){
            this.path=path;
            this.default_value = default_value;
        }
    }
}
