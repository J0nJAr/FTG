package jonjar.ftg;

import jonjar.ftg.manager.CommandManager;
import jonjar.ftg.manager.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class FTG extends JavaPlugin {

    private GameManager gm;

    public static World world;

    CommandManager cm;
    @Override
    public void onEnable() {

        /*
        String worldName = null;
        try {
            properties.load(new FileInputStream("server.properties"));
            worldName = properties.getProperty("server-name");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (worldName==null) worldName = "world";
        */

        world = Bukkit.getWorlds().get(0);
        cm = new CommandManager(this);
        gm = new GameManager(this);

        getCommand("ftg").setExecutor(cm);
        //getCommand("ftg").tabComplete(new TabCompleteManager());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public GameManager getGameManager(){
        return this.gm;
    }
}
