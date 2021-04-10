package jonjar.ftg.manager;

import jonjar.ftg.util.MsgSender;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
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

    public static void openGUI(Player p,int page){
        updateGUIs();
        p.openInventory(getGUI(page));
    }

    private static Inventory getGUI(int page){
        while(page>=guiList.size()){
            Inventory inv = Bukkit.createInventory(null,54,NAME+" Menu "+guiList.size());
            guiList.add(inv);

            ItemStack is = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
            ItemStack make = new ItemStack(Material.CHEST);
            ItemStack pick = new ItemStack(Material.EMERALD);

            ItemStack is2 = is.clone();

            ItemMeta im = is.getItemMeta();

            im.setDisplayName("이전");
            is.setItemMeta(im);

            im.setDisplayName("다음");
            is2.setItemMeta(im);

            im.setDisplayName("뽑기");
            pick.setItemMeta(im);

            ItemMeta make_is = is.getItemMeta();
            make_is.setDisplayName(ChatColor.WHITE+"새 보급 만들기");
            make.setItemMeta(make_is);

            inv.setItem(45,is);
            inv.setItem(49,make);
            inv.setItem(50,pick);
            inv.setItem(53,is2);
        }
        return guiList.get(page);
    }

    private static void updateGUIs(){
        Iterator iterator = dropsMap.keySet().iterator();
        int idx=0;
        int pg=0;
        while(iterator.hasNext()){
            getGUI(pg).setItem(idx,dropsMap.get(iterator.next()).getIcon());
            if(45<=++idx){
                idx=0;
                pg++;
            }
        }

    }

    public static void InventoryClickEvent(InventoryClickEvent event) {
        String[] name = event.getInventory().getName().split(" ");
        if (name[0].equalsIgnoreCase(NAME)) {
            if (name[1].equalsIgnoreCase("Menu")) {
                ItemStack itemStack = event.getCurrentItem();
                event.setCancelled(true);
                if (itemStack == null) {
                    return;
                }
                if (itemStack.getType() == Material.WHITE_SHULKER_BOX) {
                    Drop drop = dropsMap.get(Integer.parseInt(event.getCurrentItem().getItemMeta().getDisplayName().split("번")[0]));
                    drop.inventory.setItem(27, drop.getIcon());
                    event.getWhoClicked().openInventory(drop.inventory);
                } else if (event.getRawSlot() == 44) {
                    event.getWhoClicked().openInventory(getGUI(Math.max(0,Integer.parseInt(name[2]) - 1)));
                } else if (event.getRawSlot() == 49) {
                    event.getWhoClicked().openInventory(new Drop().inventory);
                } else if (event.getRawSlot() == 50) {
                    event.getWhoClicked().openInventory(getRandomDrop().inventory);

                } else if (event.getRawSlot() == 53) {
                    event.getWhoClicked().openInventory(getGUI(Integer.parseInt(name[2]) + 1));
                }

            } else {
                if (event.getRawSlot() == 27) {
                    int add = 1;
                    Drop drop = dropsMap.get(Integer.parseInt(name[1]));
                    if(event.isRightClick()) add*=-1;
                    if(event.isShiftClick()) add*=10;
                    drop.addModifier(add);
                    drop.inventory.setItem(27,drop.getIcon());
                    event.setCancelled(true);
                }
                if (event.getRawSlot() > 27 && event.getRawSlot() < 36) {//인벤토리가 아닌부분은 관상용
                    updateGUIs();
                    event.getWhoClicked().openInventory(getGUI(0));
                    event.setCancelled(true);
                }
            }
        }
    }


    public static Drop getRandomDrop(){
        Random random = new Random();
        if(Sum_modifier == 0 ) {
            MsgSender.getMsgSender().broadcast("설정된 보급이 없어 비어있는 보급을 생성했습니다.");
            return new Drop();
        }
        int value = 1+random.nextInt(Sum_modifier);//

        for(Drop drop : dropsMap.values()){
            if(drop.modifier>=value){
                return drop;
            }else{
                value-=drop.modifier;
            }
        }

        return null;
    }


    public static class Drop{
        private static int count = 0;//gui 이름 구분을 위한 숫자
        private final int id; //고유 id

        private int modifier; //확률 관련 정수(경우의 수 느낌)
        public Inventory inventory;

        Drop(){
            this(1);
        }

        Drop(int modifier){
            this.modifier = modifier;
            Sum_modifier+=this.modifier;
            this.inventory = Bukkit.createInventory(null,36,NAME+" "+count);

            id = count;
            dropsMap.put(id,this);
            count++;


            this.inventory.setItem(27,getIcon());

            ItemStack is = new ItemStack(Material.BARRIER);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(" ");
            is.setItemMeta(im);

            for (int i = 28 ; i < 36 ; i++){
                this.inventory.setItem(i,is);
            }

        }

        public void remove(){
            dropsMap.remove(id);
            Sum_modifier -= this.modifier;
        }

        public void setDropInventory(Inventory inventory){
            for (int i = 0 ; i < 27 ; i++){
                this.inventory.setItem(i,inventory.getItem(i));
            }
        }

        public ItemStack getIcon(){
            ItemStack icon = new ItemStack(Material.WHITE_SHULKER_BOX,1);

            ItemMeta meta = icon.getItemMeta();
            BlockStateMeta bsm = (BlockStateMeta) meta;

            ShulkerBox sb = (ShulkerBox) bsm.getBlockState();
            sb.getInventory().setContents(Arrays.copyOfRange(inventory.getContents(),0,26));

            meta.setDisplayName(this.toString());

            ArrayList<String> lore = new ArrayList<>();
            lore.add(getChance()+"%");
            lore.add(ChatColor.GRAY+"가중치 : "+modifier+"/"+Sum_modifier);
            /*
            for(ItemStack item : inventory.getContents()){
                if(item == null) continue;
                StringBuilder i_str = new StringBuilder(item.getType().name()).append(" x ").append(item.getAmount());
                if (item.hasItemMeta()) {
                    i_str.append(", ").append(item.getItemMeta());
                }
                lore.add(i_str.toString());
            }*/
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

        public void addModifier(int add) {
            if((this.modifier + add)<0) {
                setModifier(0);
            }else{
                Sum_modifier += add;
                this.modifier += add;
            }
        }

        @Override
        public String toString(){
            return id+"번 보급";
        }
    }
}
