package jonjar.ftg.manager;

import jonjar.ftg.FTG;
import jonjar.ftg.entity.Tile;
import jonjar.ftg.util.ContainerUtil;
import jonjar.ftg.util.MsgSender;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventException;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class DropsManager {
    private final static ArrayList<Inventory> guiList = new ArrayList<>();
    public final static String NAME = "Drop";
    public static LinkedHashMap<Integer,Drop> dropsMap = new LinkedHashMap<>();

    private final static Inventory CONFIRM_GUI;
    static {
        CONFIRM_GUI = Bukkit.createInventory(null,9,"정말로 삭제모드로 바꾸겠습니까?");
        ItemStack is = new ItemStack(Material.TNT);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(ChatColor.RED+""+ChatColor.BOLD+"예");
        is.setItemMeta(im);
        CONFIRM_GUI.setItem(4,is);
    }
    private static boolean delete = false;

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
            ItemStack delete = new ItemStack(Material.TNT);

            ItemStack is2 = is.clone();

            ItemMeta im = is.getItemMeta();

            im.setDisplayName(ChatColor.WHITE+"이전");
            is.setItemMeta(im);

            im.setDisplayName(ChatColor.WHITE+"다음");
            is2.setItemMeta(im);

            im.setDisplayName(ChatColor.WHITE+"뽑기");
            pick.setItemMeta(im);

            im.setDisplayName(ChatColor.RED+""+ChatColor.BOLD+"삭제하기");
            delete.setItemMeta(im);

            ItemMeta make_is = is.getItemMeta();
            make_is.setDisplayName(ChatColor.WHITE+"새 보급 만들기");
            make.setItemMeta(make_is);

            inv.setItem(45,is);
            inv.setItem(48,delete);
            inv.setItem(49,make);
            inv.setItem(50,pick);
            inv.setItem(53,is2);
        }
        return guiList.get(page);
    }

    private static void updateGUIs(){

        for(int i=0;i<Drop.count;i++){
            Drop drop = dropsMap.get(i);
            if(drop == null) {
                getGUI(i / 45).setItem(i % 45,null);
                continue;
            }
            ItemStack is = drop.getIcon();
            if(delete) is.setType(Material.RED_SHULKER_BOX);
            getGUI(i / 45).setItem(i % 45,is);
        }

        /*
        Iterator iterator = dropsMap.keySet().iterator();
        int idx=0;
        int pg=0;
        while(iterator.hasNext()){
            ItemStack is = dropsMap.get(iterator.next()).getIcon();
            if(delete) is.setType(Material.RED_SHULKER_BOX);
            getGUI(pg).setItem(idx,is);
            if(45<=++idx){
                idx=0;
                pg++;
            }
        }
         */

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
                int num = 1;
                if(event.isShiftClick()) num = 5;

                if (itemStack.getType() == Material.ORANGE_SHULKER_BOX || itemStack.getType() == Material.WHITE_SHULKER_BOX) {
                    Drop drop = dropsMap.get(Integer.parseInt(event.getCurrentItem().getItemMeta().getDisplayName().split("번")[0].replaceFirst(ChatColor.WHITE.toString(),"")));
                    drop.inventory.setItem(27, drop.getIcon());
                    event.getWhoClicked().openInventory(drop.inventory);
                } if (itemStack.getType() == Material.RED_SHULKER_BOX) {
                    Drop drop = dropsMap.get(Integer.parseInt(event.getCurrentItem().getItemMeta().getDisplayName().split("번")[0].replaceFirst(ChatColor.WHITE.toString(),"")));
                    drop.remove();
                    event.getInventory().setItem(event.getRawSlot(),null);
                } else if (event.getRawSlot() == 45) {
                    event.getWhoClicked().openInventory(getGUI(Math.max(0,Integer.parseInt(name[2]) - num)));
                } else if (event.getRawSlot() == 48) {
                    if(delete) {
                        delete =false;
                        updateGUIs();
                    }
                    else event.getWhoClicked().openInventory(CONFIRM_GUI);
                } else if (event.getRawSlot() == 49) {
                    try{
                    event.getWhoClicked().openInventory(new Drop().inventory);}
                    catch (IllegalStateException e){
                        MsgSender.getMsgSender().error(event.getWhoClicked(),"최대 보급 개수를 초과했습니다.");
                    }
                } else if (event.getRawSlot() == 50) {
                    event.getWhoClicked().openInventory(getRandomDrop().inventory);
                } else if (event.getRawSlot() == 53) {
                    event.getWhoClicked().openInventory(getGUI(Math.min(9,Integer.parseInt(name[2]) + num)));
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
        } else if (name[0].equals("정말로")) {
            event.setCancelled(true);
            if(event.getRawSlot()==4) {
                delete = true;
                updateGUIs();
                event.getWhoClicked().openInventory(getGUI(0));
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

        for(Drop drop : dropsMap.values()) {
            if (drop.modifier >= value) {
                return drop;
            } else {
                value -= drop.modifier;
            }
        }


        return null;
    }

    public static HashSet<Location> spawnedDrops = new HashSet<>();
    public static HashSet<Integer> spawnedFallingDrops = new HashSet<>();

    public static void spawnRandomDrop(){
        Tile tile = Tile.TILE_MAP.getRandomEmptyTile();
        if(tile == null) return;

        Location loc = tile.getDropsBlock().getLocation().add(0.5, 50, 0.5);
        FallingBlock fb = loc.getWorld().spawnFallingBlock(loc, Material.WOOD, (byte) 0);
        fb.setDropItem(false);
        fb.setHurtEntities(false);
        fb.setGlowing(true);

        spawnedFallingDrops.add(fb.getEntityId());
    }

    public static void onDropsFall(FallingBlock fb){

        Drop drop = getRandomDrop();
        if(drop == null) return;

        // CHECK : macham.do("비주얼");
        Block b = fb.getLocation().getBlock();
        b.setType(Material.CHEST);
        spawnedDrops.add(b.getLocation());

        Chest c = (Chest) b.getState();
        Inventory inv = c.getInventory();

        for(int i=0;i<27;i++){
            inv.setItem(i, drop.inventory.getItem(i));
        }
    }

    public static boolean removeSpawnedDrop(InventoryCloseEvent event){
        Inventory inventory = event.getInventory();
        Location location = inventory.getLocation();
        if(spawnedDrops.contains(location)){//보급 위치임
            if(ContainerUtil.isInvEmpty(inventory.getContents())){//보급이 비어있음
                location.getBlock().setType(Material.AIR);
                spawnedDrops.remove(location);
            }
            return true;
        }
        return false;
    }


    public static void load(){
        dropsMap.clear();
        Drop.count = 0;
        DropsManager.Sum_modifier = 0;

        Location loc1 = new Location(FTG.world,-22,8,1);
        Location loc2 = new Location(FTG.world,-22,8,4);

        Inventory inv ;
        for(int page = 0; page<5;page++) {
            inv = ContainerUtil.getFromChest(loc1);
            for (int i = 0; i < 45; i++) {
                ItemStack stack = inv.getItem(i);
                if(stack==null) continue;
                int modifier = Integer.parseInt(stack.getItemMeta().getLore().get(1).split(" ")[2].split("/")[0]);
                new Drop(modifier).setDropInventory(ContainerUtil.getFromShulker(stack));
            }
            loc1.subtract(0,1,0);
        }
        for(int page = 5; page<10;page++) {
            inv = ContainerUtil.getFromChest(loc2);
            for (int i = 0; i < 45; i++) {
                ItemStack stack = inv.getItem(i);
                if(stack==null) continue;
                int modifier = Integer.parseInt(stack.getItemMeta().getLore().get(1).split(" ")[2].split("/")[0]);
                new Drop(modifier).setDropInventory(ContainerUtil.getFromShulker(stack));
            }
            loc2.subtract(0,1,0);
        }
    }

    public static void save(){
        Location loc1 = new Location(FTG.world,-22,8,1);
        Location loc2 = new Location(FTG.world,-22,8,4);
        Inventory inv ;

        for(int page = 0; page<5;page++) {
            inv = ContainerUtil.getFromChest(loc1);
            for (int i = 0; i < 45; i++) {
                Drop drop = dropsMap.get(45*page+i);
                if(drop == null) inv.setItem(i,null);
                else inv.setItem(i,drop.getIcon(true));
            }
            loc1.subtract(0,1,0);
        }
        for(int page = 5; page<10;page++) {
            inv = ContainerUtil.getFromChest(loc2);
            for (int i = 0; i < 45; i++) {
                Drop drop = dropsMap.get(45*page+i);
                if(drop == null) inv.setItem(i,null);
                else inv.setItem(i,drop.getIcon(true));
            }
            loc2.subtract(0,1,0);
        }
    }


    public static class Drop{
        private static int count = 0;//gui 이름 구분을 위한 숫자
        private static SortedSet<Integer> blank = new TreeSet<>();
        private final int id; //고유 id

        private int modifier; //확률 관련 정수(경우의 수 느낌)
        public Inventory inventory;




        Drop(){
            this(1);
        }

        Drop(int modifier){
            this.modifier = modifier;
            Sum_modifier+=this.modifier;

            if(blank.isEmpty()) {
                if(count>=10*45) throw new IllegalStateException();
                id = count;
                count++;
            }else {
                id = blank.first();
                blank.remove(id);
            }

            this.inventory = Bukkit.createInventory(null,36,NAME+" "+id);

            dropsMap.put(id,this);
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
            blank.add(id);
            dropsMap.remove(id);
            Sum_modifier -= this.modifier;
        }

        public void setDropInventory(Inventory inventory){
            for (int i = 0 ; i < 27 ; i++){
                this.inventory.setItem(i,inventory.getItem(i));
            }
        }

        public ItemStack getIcon(){
            return getIcon(false);
        }
        public ItemStack getIcon(boolean containsItems){
            Material box;
            if(modifier==0){
                box = Material.WHITE_SHULKER_BOX;
            }else {
                box = Material.ORANGE_SHULKER_BOX;
            }

            ItemStack icon = containsItems ? ContainerUtil.toShuklerBox(box,inventory) : new ItemStack(box);

            ItemMeta meta = icon.getItemMeta();

            meta.setDisplayName(ChatColor.WHITE+this.toString());

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
