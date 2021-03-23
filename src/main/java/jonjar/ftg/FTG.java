package jonjar.ftg;

import jonjar.ftg.entity.PlayerInfo;
import jonjar.ftg.entity.Team;
import jonjar.ftg.entity.Tile;
import jonjar.ftg.manager.CommandManager;
import jonjar.ftg.manager.EventManager;
import jonjar.ftg.manager.GameManager;
import jonjar.ftg.manager.TabCompleteManager;
import org.bukkit.Bukkit;
import org.bukkit.World;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public final class FTG extends JavaPlugin {

    public static FTG INSTANCE = null;

    private GameManager gm;
    private EventManager em;
    private TabCompleteManager tcm;

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

        INSTANCE = this;

        world = Bukkit.getWorlds().get(0);
        Team.initScoreboardManager(Bukkit.getScoreboardManager().getMainScoreboard());
        cm = new CommandManager(this);
        gm = new GameManager(this);
        em = new EventManager(this);
        tcm = new TabCompleteManager(this);

        Tile.registerTiles();
        Tile.registerAllNearTileList();
        for(ArmorStand e : world.getEntitiesByClass(ArmorStand.class)){
            if(e.getCustomName() != null)
                e.remove();
        }
        Tile.registerDummy();

        Bukkit.getPluginManager().registerEvents(em, this);

        getCommand("ftg").setExecutor(cm);
        getCommand("ftg").setTabCompleter(tcm);
        //getCommand("ftg").tabComplete(new TabCompleteManager());

        Scoreboard sc = Bukkit.getScoreboardManager().getMainScoreboard();
        if(sc.getObjective(DisplaySlot.SIDEBAR) != null)
            sc.getObjective(DisplaySlot.SIDEBAR).unregister();

        for(Player ap : Bukkit.getOnlinePlayers()){
            new PlayerInfo(ap.getName(), ap.getUniqueId());
        }

        if(sc.getObjective("tile") == null){
            Objective obj = sc.registerNewObjective("tile", "dummy");
            obj.setDisplayName("§l점령한 타일");
        }

    }

    @Override
    public void onDisable() {
        Tile.unregisterDummy();
    }



    public GameManager getGameManager(){
        return this.gm;
    }
    public EventManager getEventManager() { return this.em; }
}
