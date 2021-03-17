package jonjar.ftg.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class LocationUtil {

    public static Entity getClosestEntityType(Location center, double radius, EntityType entityType){

        Entity closestEntity = null;
        double closestDistance = 0.0;

        for(Entity entity : center.getWorld().getEntities()){
            double distance = entity.getLocation().distanceSquared(center);
            if(closestEntity == null || (distance < closestDistance && entity.getType().equals(entityType))){
                closestDistance = distance;
                closestEntity = entity;
            }
        }
        return closestEntity;
    }

}
