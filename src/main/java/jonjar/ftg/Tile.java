package jonjar.ftg;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;

public class Tile {
    HashSet<Tile> TileSet = new HashSet<Tile>();
    Location center = null;

    Tile(Location center){
        this.center = center;
    };

    public static void registerTiles(){

    }
}
