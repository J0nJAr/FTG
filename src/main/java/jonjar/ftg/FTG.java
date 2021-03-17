package jonjar.ftg;

import jonjar.ftg.entity.Tile;
import jonjar.ftg.manager.CommandManger;
import jonjar.ftg.manager.TabCompleteManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public final class FTG extends JavaPlugin {

    public static World world;

    Properties properties = new Properties();
    CommandManger cm;
    @Override
    public void onEnable() {
        String worldName = null;
        try {
            properties.load(new FileInputStream("server.properties"));
            worldName = properties.getProperty("server-name");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (worldName==null) worldName = "world";

        cm = new CommandManger();
        world = Bukkit.getWorld(worldName);



        getCommand("ftg").setExecutor(cm);
        //getCommand("ftg").tabComplete(new TabCompleteManager());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
