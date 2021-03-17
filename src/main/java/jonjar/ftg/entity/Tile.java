package jonjar.ftg.entity;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

public class Tile {
    final private Material outline = Material.WHITE_TERRACOTTA;
    final private int radius = 6;

    public static Set<Tile> TileSet = new HashSet<Tile>();

    Location center = null;

    Tile(Location centeer){
        this.center = center;
    }

    public static HashSet<Block> getBlocks(){
    radius = this.radius;
    Set<Block> result = new HashSet<Block>;
        for (int x = -radius, x++,x<=radius){
            for(int z = -radius, z++, z<=radius){
                if(Math.abs(x)>radius/2&&Math.abs(z)<1+x-(radius/2)*2){
                result.add(center.getBlock());
                }
            }
        }
    }
    public static void registerTiles(){

    }
}
