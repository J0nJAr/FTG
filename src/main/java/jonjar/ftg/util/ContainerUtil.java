package jonjar.ftg.util;


import jonjar.ftg.FTG;
import jonjar.ftg.entity.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

public class ContainerUtil {

    Location LOCATION_CONTAINER;
    static ContainerUtil c;

    private ContainerUtil(){
        LOCATION_CONTAINER = new Location(FTG.world,-15,5,2);
    }

    public static ContainerUtil getInstance(){
        if (c==null) c= new ContainerUtil();
        return c;
    }

    public void registerAllInventory(){
        Location location = LOCATION_CONTAINER.clone();
        location.add(0,-1,0);
        for(Team.TeamColor tc : Team.TeamColor.values()) {
            location.add(0,0,2);
            if(tc.getData()==location.getBlock().getData()){
                tc.setInventory(((Chest)location.getBlock().getState()).getInventory());
            }
        }
    }

    public void setInventory(Player p, Chest c){
        // if(!(b instanceof Chest)) return;
        setInventory(p,c.getInventory());
    }

    public void setInventory(Player p, Inventory inv){
       // if(!(b instanceof Chest)) return;
        PlayerInventory pi = p.getInventory();
        for(int i =0; i<=40 ; i++) {
            Bukkit.broadcastMessage(i+"");
            if(inv.getItem(i)!=null) Bukkit.broadcastMessage(inv.getItem(i).getType().toString());
            pi.setItem(i,inv.getItem(i));
        }
    }

}
