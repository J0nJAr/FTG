package jonjar.ftg.util;

import jonjar.ftg.entity.Tile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Set;

public class LocationUtil {

    public static Entity getClosestEntityType(Location center, double radius, EntityType entityType){

        Entity closestEntity = null;
        double closestDistance = 0.0;

        for(Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)){
            double distance = entity.getLocation().distanceSquared(center);
            if(closestEntity == null || (entity.getType().equals(entityType) && distance < closestDistance)){
                closestDistance = distance;
                closestEntity = entity;
            }
        }

        return closestEntity;
    }

    public static Tile getClosestTile(Location player){
        Location location = player.clone().subtract(Tile.LOCATION_0).add(7*10,0,+39);
        Location loc = player.clone();
        loc.setY(5);

        Tile tile = Tile.TILE_MAP.getTile((int)Math.round(location.getX()/(147/13)), (int)Math.round(location.getZ()/(183/13)));

        Bukkit.broadcastMessage(""+location.getBlockX()+","+location.getBlockZ()+"  || "+Math.round(location.getX()/(147/13))+","+Math.round(location.getZ()/(183/13))+"\n"+loc.toString());

        Bukkit.broadcastMessage(tile.toString()+"dsdjaskds");
        if (tile.getLocations().contains(loc)) return tile;
        else {
            Set<Tile> s = tile.getNearTileList().get(1);
            for(Tile t:s){
                if(t.getLocations().contains(loc.getBlock().getType() == Material.CONCRETE)) return t;
            }
        }

        return tile;
    }
}
