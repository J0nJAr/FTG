package jonjar.ftg.manager;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

public class DropsManager {
    private final static ArrayList<Inventory> guiList = new ArrayList<>();
    public final static String NAME = "Drops";





    public static void InventoryClickEvent(InventoryClickEvent event){
        String name = event.getInventory().getName().split(" ")[0];
        if(name.equalsIgnoreCase("Drops")){

        }

    }
}
