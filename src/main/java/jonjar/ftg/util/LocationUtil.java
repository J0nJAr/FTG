package jonjar.ftg.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

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

}
