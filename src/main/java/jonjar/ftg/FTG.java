package jonjar.ftg;

import jonjar.ftg.entity.Tile;
import jonjar.ftg.manager.CommandManger;
import jonjar.ftg.manager.TabCompleteManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class FTG extends JavaPlugin {
    public static Material outline = Material.WHITE_TERRACOTTA;
    public static List<Material> tileMaterial= Arrays.asList(Material.GRAY_CONCRETE, Material.RED_CONCRETE, Material.BLUE_CONCRETE);
    public static World world;
    CommandManger cm;
    @Override
    public void onEnable() {
        // Plugin startup logic
        cm = new CommandManger();

        Tile.registerTiles();


        getCommand("ftg").setExecutor(cm);
        //getCommand("ftg").tabComplete(new TabCompleteManager());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
