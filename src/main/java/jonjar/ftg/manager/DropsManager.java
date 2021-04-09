package jonjar.ftg.manager;

import jonjar.ftg.util.MsgSender;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class DropsManager {
    private final static ArrayList<Inventory> guiList = new ArrayList<>();
    public final static String NAME = "Drops";
    public static LinkedHashMap<Integer,Drop> dropsMap = new LinkedHashMap<>();

    public static int Sum_modifier = 0;



    public static void openGUI(Player p){
        openGUI(p,0);
    }

    public static void openGUI(Player p,int index){
        while(index>guiList.size()){
            Inventory inv = Bukkit.createInventory(null,54,NAME+" MENU "+guiList.size());
            guiList.add(inv);

            for(int i = 0; i < 45 ; i++){

            }
            for(int i = 45; i < 54 ; i++){
                inv.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE));
            }
        }
        p.openInventory(guiList.get(index));
    }

    public static void InventoryClickEvent(InventoryClickEvent event){
        String[] name = event.getInventory().getName().split(" ");
        if(name[0].equalsIgnoreCase(NAME)){
            if(name[1] == "MENU"){
                event.setCancelled(true);
            }else{
                ;
            }
        }

    }

    public static Drop getRandomDrop(){
        Random random = new Random();

        int value = random.nextInt(Sum_modifier);
        for(Drop drop : dropsMap.values()){
            if(drop.modifier<=value){
                return drop;
            }else{
                value+=drop.modifier;
            }
        }

        return null;
    }


    public static class Drop{
        private static int count = 0;//gui 이름 구분을 위한 숫자
        private final int id; //고유 id

        private int modifier; //확률 관련 정수(경우의 수 느낌)
        public Inventory inventory;

        Drop(int modifier,Inventory inventory){
            this.modifier = modifier;
            Sum_modifier+=this.modifier;
            this.inventory = Bukkit.createInventory(null,27,NAME+" "+count);
            for (int i = 0 ; i < 27 ; i++){
                this.inventory.setItem(i,inventory.getItem(i));
            }
            id = count;
            dropsMap.put(id,this);
            count++;
        }

        public void remove(){
            dropsMap.remove(id);
            Sum_modifier -= this.modifier;
        }

        //TODO: 보급 아이콘, 확률과 내용물을 이름이나 lore에 적기.
        public ItemStack getIcon(){
            ItemStack icon = new ItemStack(Material.WHITE_SHULKER_BOX,1);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(id+"번 보급");

            ArrayList<String> lore = new ArrayList<>();
            lore.add(getChance()+"%");

            for(ItemStack item : inventory.getContents()){
                if(item == null) continue;
                StringBuilder i_str = new StringBuilder("ItemStack{").append(item.getType().name()).append(" x ").append(item.getAmount());
                if (item.hasItemMeta()) {
                    i_str.append(", ").append(item.getItemMeta());
                }
                lore.add(i_str.toString());
            }
            meta.setLore(lore);

            icon.setItemMeta(meta);

            return icon;
        }

        /*
        * 확률을 구하는 함수.
         */
        public double getChance(){
            return Math.round(((double)modifier/(double)Sum_modifier)*10000)/100.0;
        }


        public int getModifier() {
            return modifier;
        }

        public void setModifier(int modifier) {
            Sum_modifier -= this.modifier;
            this.modifier = modifier;
            Sum_modifier += this.modifier;
        }

    }
}
