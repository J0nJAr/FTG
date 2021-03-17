package jonjar.ftg.entity;

import jonjar.ftg.FTG;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

public class Tile {
    //final private Material outline = FTG.outline;
    final private int radius = 6;
    final private static int RADIUS = 6;

    public static Set<Tile> TileSet = new HashSet<Tile>();

    private Location center = null;

    Tile(Location center){
        this.center = center;
    }

    public Set<Block> getBlocks(){
    Set<Block> result = new HashSet<Block>();
        for (int x = -radius; x<=radius;x++){
            for(int z = -radius; z<=radius;z++ ){
                if(Math.abs(x)>radius/2&&Math.abs(z)<1+x-(radius/2)*2){

                    result.add(center.getBlock().getLocation().add((double) x,0,(double) z).getBlock());
                }
            }
        }
        return result;
    }

    public Location getCenter(){
        return center;
    }


    public static void registerTiles(){
       Location basis = new Location(FTG.world,153D,5D,32D);
       int temp =0;
       for (int x = -RADIUS;x<=RADIUS;x++){
           for (int z = -RADIUS/2-temp;z<=temp+RADIUS/2;z++){
               if(x<0) temp++;
               else temp--;
               TileSet.add(new Tile(basis.clone().add(x*14,0,z*14)));
           }
        }
       //while (FTG.tileMaterial.contains(current.getBlock().getType()==Material.GRAY_CONCRETE));

    }
}
