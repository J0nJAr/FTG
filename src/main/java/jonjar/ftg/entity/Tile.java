package jonjar.ftg.entity;

import javafx.util.Pair;
import jonjar.ftg.FTG;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.material.Rails;

import java.util.*;

public class Tile {
    //final private Material outline = FTG.outline;
    static final private int radius = 6;
    final private static int RADIUS = 6;

    private Set<Location> locationSet;
    private Set<Block> blockSet;
    private ArrayList<Set<Tile>> nearTileList;

    public static TileMapC TILE_MAP= new TileMapC();
    public static Set<Tile> TILE_SET = new HashSet<>();



    private final Location center;
    public final int x_index, z_index;
    private ArmorStand centerDummy;

    Tile(Location center, int x_index, int y_index){
        this.center = center;
        this.x_index=x_index;
        this.z_index=y_index;

        this.registerLocations();
        nearTileList = new ArrayList<>();
    }

    @Override
    public String toString() {
        return this.x_index+","+this.z_index;
    }

    public Set<Block> getBlocks(){
        if(this.blockSet == null){//아직 블럭 set가 없으면 생성
            blockSet = new HashSet<Block>();
            this.getLocations().forEach(l -> blockSet.add(l.getBlock()));
        }
        return blockSet;
    }

    public void spawnDummy(){
        ArmorStand as = (ArmorStand) center.getWorld().spawnEntity(center.clone().add(0, -2, 0), EntityType.ARMOR_STAND);
        as.setGravity(false);
        as.setInvulnerable(true);
        as.setVisible(false);
        as.setCustomName(this.toString());
        as.setCustomNameVisible(false);
        centerDummy = as;
    }

    public void killDummy(){
        centerDummy.remove();
        centerDummy = null;
    }

    public void registerLocations(){
        locationSet = new HashSet<Location>();

        for (int z=0;z<=2*radius;z++) {
            locationSet.add(center.clone().add(0,0,z-radius));
        }

        for(int x=1;x<=radius;x++){
            locationSet.add(center.clone().add(x,0,0));
            locationSet.add(center.clone().subtract(x,0,0));

            for(int z=1;z<=Math.min(radius,radius*2+1-2*x);z++){
                locationSet.add(center.clone().add(x,0,z));
                locationSet.add(center.clone().add(-x,0,z));
                locationSet.add(center.clone().subtract(x,0,z));
                locationSet.add(center.clone().subtract(-x,0,z));
            }

        }
    }

    public boolean isAdjacent(int x_index,int z_index){

        if(z_index<0) return false;
        if(Math.abs(x_index)>RADIUS) return false;
        if((RADIUS*2-Math.abs(x_index))<z_index) return false;

        if(Math.abs(z_index-this.z_index)<=1 && Math.abs(x_index-this.x_index)<=1){
            return true;
        }

        return false;
    }

    public static void registerAllNearTileList(){
        for(Tile tile:TILE_SET) tile.registerNearTileList();
    }

    public void registerNearTileList() {
        int[] _z = {0,1};
        int[] _x = {-1,0,1};
        int distance = 0;

        Queue<Tile> q = new LinkedList<>();
        q.add(this);

        while (!q.isEmpty()){
            distance++;
            Tile tile = q.poll();

            for(int x : _x) for(int z : _z){
                if(isAdjacent(x,z)){
                    if(nearTileList.get(distance)==null) nearTileList.add(new HashSet<Tile>());
                    nearTileList.get(distance).add(TILE_MAP.getTile(x,z));
                    Bukkit.broadcastMessage("거리 : "+distance+"타일1 "+this.toString()+"타일2 "+tile.toString());
                    q.add(tile);
                }
            }
        }
    }


    public Set<Location> getLocations(){
        return this.locationSet;
    }

    public Location getCenter(){
        return center;
    }

    public ArmorStand getDummy(){
        return this.centerDummy;
    }

    public static void unregisterDummy(){
        for(Tile tile : TILE_SET){
            if(tile.getDummy() != null)
                tile.killDummy();
        }
    }

    public static void registerDummy(){
        for(Tile tile : TILE_SET){
            if(tile.getDummy() != null)
                tile.spawnDummy();
        }
    }

    public static void registerTiles(){
        final int distance = RADIUS*2-1;
        int amount = RADIUS*2+1;

       //final Location CENTER =
       Location upperF = new Location(FTG.world,153D,5D,-52D);
       Location lowerF = upperF.clone();

       TILE_MAP.addTiles(0,amount,upperF);

       for(int x=1;x<=RADIUS;x++){
           amount--;

           upperF.add(distance,0,RADIUS+1);
           TILE_MAP.addTiles(x,amount,upperF);

           lowerF.add(-distance,0,RADIUS+1);
           TILE_MAP.addTiles(-x,amount,lowerF);
       }
    }



    public static class TileMapC{
        private HashMap<Integer,ArrayList<Tile>> map;

        TileMapC(){
            map = new HashMap<>();
        }

        public void addTile(int x_index,Tile tile){
            if(map.get(x_index)==null) {
                map.put(x_index,new ArrayList<Tile>());
            }
            map.get(x_index).add(tile);
            tile.spawnDummy();
            TILE_SET.add(tile);
        }

        public void addTiles(int x_index,int amount,Location first){
            for (int i=0;i<amount;i++){
                addTile(x_index,new Tile(first.clone().add(0,0,i*(RADIUS*2+2)), x_index, i));
            }
        }

        public Tile getTile(int x_index,int z_index){
            return map.get(x_index).get(z_index);
        }
    }
}
