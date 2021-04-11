package jonjar.ftg.util;


import jonjar.ftg.FTG;
import jonjar.ftg.entity.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;

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
                tc.setInventory(getFromChest(_chest));
                }
            }
        _chest.add(0,0,2);
        _color.add(0,0,2);
        }
    }

    public static Inventory getFromChest(Location loc){
        Chest chest = (Chest)(loc).getBlock().getState();
        return chest.getInventory();
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

    public static Inventory getFromShulker(ItemStack shulkerBoxItemStack){
        ItemStack item = shulkerBoxItemStack;
        if(item.getItemMeta() instanceof BlockStateMeta){
            BlockStateMeta im = (BlockStateMeta)item.getItemMeta();
            if(im.getBlockState() instanceof ShulkerBox){
                ShulkerBox shulker = (ShulkerBox) im.getBlockState();
                Inventory inv = Bukkit.createInventory(null, 27, item.getItemMeta().getDisplayName());
                inv.setContents(shulker.getInventory().getContents());
                return inv;
            }
        }
        return null;
    }

    public static ItemStack toShuklerBox(Material shulkerBox, Inventory inventory){
        ItemStack itemStack = new ItemStack(shulkerBox);
        BlockStateMeta bsm = (BlockStateMeta) itemStack.getItemMeta();
        ShulkerBox box = (ShulkerBox) bsm.getBlockState();
        for(int i = 0 ; i <27; i++) {
            box.getInventory().setItem(i, inventory.getItem(i));
        }
        bsm.setBlockState(box);
        box.update();
        itemStack.setItemMeta(bsm);
        return itemStack;
    }

}
