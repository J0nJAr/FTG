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

    static final int CHECK_SIZE = 1;
    public static Tile getClosestTile(Location player){
        Location location = player.clone().subtract(Tile.LOCATION_0);
        Location playerLoc = player.clone();
        playerLoc.setY(5);

        int x = (int)Math.rint(location.getX()/12);
        int z = (int)Math.rint((location.getZ()-Math.abs(x*7))/14);

        Tile tile = Tile.TILE_MAP.getTile(x, z);
/*
        Bukkit.broadcastMessage("========================================");
        Bukkit.broadcastMessage(x+", "+z);
        Bukkit.broadcastMessage(Tile.LOCATION_0.getBlockX()+", "+Tile.LOCATION_0.getBlockZ());
        Bukkit.broadcastMessage(playerLoc.getBlockX()+", "+playerLoc.getBlockZ());

        Bukkit.broadcastMessage("__"+tile);*/

        if (tile.getBlocks().contains(playerLoc.getBlock())) {
            //ukkit.broadcastMessage("@@"+tile);
            return tile;
        } else {
            for (int i = 1; i <= CHECK_SIZE; i++) {
                Set<Tile> s = tile.getNearTileList().get(i);
                for (Tile t : s) {
                  //  Bukkit.broadcastMessage("____" + t);
                    if (t.getBlocks().contains(playerLoc.getBlock())) {
                      //  Bukkit.broadcastMessage("@@@@" + i);
                        return t;
                    }
                }
            }
        }
        return null;
    }
}
