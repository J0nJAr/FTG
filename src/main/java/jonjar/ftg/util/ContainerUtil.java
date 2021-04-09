package jonjar.ftg.util;


import jonjar.ftg.FTG;
import jonjar.ftg.entity.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

public class ContainerUtil {

    private final static Location LOCATION_CONTAINER = new Location(FTG.world,-15,5,2);


    public static void registerAllInventory(){
        Location _color =LOCATION_CONTAINER.clone();
        Location _chest = LOCATION_CONTAINER.clone();
        _color.add(0,-1,0);
        for(int i=0;i<Team.TeamColor.values().length;i++){
        for(Team.TeamColor tc : Team.TeamColor.values()) {
            if(tc.getData()==_color.getBlock().getData()){
                Chest chest = (Chest)(_chest).getBlock().getState();
                tc.setInventory(chest.getInventory());
                }
            }

        _chest.add(0,0,2);
        _color.add(0,0,2);
        }
    }


    public static void setInventory(Player p, Chest c){
        // if(!(b instanceof Chest)) return;
        setInventory(p,c.getInventory());
    }

    public static void setInventory(Player p, Inventory inv){
       // if(!(b instanceof Chest)) return;
        PlayerInventory pi = p.getInventory();
        for(int i =0; i<=40 ; i++) {
            pi.setItem(i,inv.getItem(i));
        }
    }

}
