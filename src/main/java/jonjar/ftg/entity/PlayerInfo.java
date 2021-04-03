package jonjar.ftg.entity;

import jonjar.ftg.FTG;
import jonjar.ftg.manager.GameManager;
import jonjar.ftg.util.ContainerUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerInfo {

    public final static int RESPWAN_TICK = 100;

    private static HashMap<String, PlayerInfo> PlayerInfoList = new HashMap<>();

    public static PlayerInfo getPlayerInfo(Player p){
        return PlayerInfoList.get(p.getName().toLowerCase());
    }
    public static PlayerInfo getPlayerInfo(String name) { return PlayerInfoList.get(name.toLowerCase()); }
    public static PlayerInfo getPlayerInfoByUUID(String uuid){
        for(PlayerInfo pi : PlayerInfoList.values()){
            if(pi.getUUID().toString().equalsIgnoreCase(uuid)) return pi;
        }
        return null;
    }


    private final String name;
    private final UUID uuid;

    private boolean observe = false;
    private boolean isDead = false;
    private Team team;


    // Player Stats
    private int kill = 0; // 킬
    private int death = 0; // 데스
    private int tile_assist = 0; // 타일 점령 도움

    private Tile nowLocation;


    public PlayerInfo(String name, UUID uuid){
        this.name = name;
        this.uuid = uuid;
        PlayerInfoList.put(name.toLowerCase(), this);
    }

    public int getKill(){ return kill; }
    public int getDeath() { return death; }
    public int getTileAssisted() { return this.tile_assist; }

    public Tile getNowLocation() { return this.nowLocation; }
    public String getName() { return this.name; }
    public UUID getUUID() { return this.uuid; }
    public Team getTeam() { return this.team; }
    public boolean isObserver() { return this.observe; }
    public boolean isDead() { return this.isDead; }

    public void addTileAssisted() { this.tile_assist++; }

    public void reset() {
        this.kill = 0;
        this.death = 0;
        this.tile_assist = 0;
    }

    public void onDeath(){
        addDeath();

        Player p = Bukkit.getPlayer(uuid);
        p.setGameMode(GameMode.SPECTATOR);
        if(nowLocation != null){
            nowLocation.removePlayer(p);
            nowLocation.removeBossBar(p);
        }
        nowLocation = null;
        isDead = true;

        new BukkitRunnable() {
            private int tick = RESPWAN_TICK;
            public void run(){

                if(GameManager.STATE != GameManager.GameState.START){
                    this.cancel();
                    return;
                }

                if(!p.isOnline())
                    return;

                tick--;

                if(tick % 20 == 0){
                    p.sendTitle("", "§e" + tick/20 + "§f초 뒤 리스폰됩니다.", 0, 24, 0);
                }

                if(tick == 0){
                    this.cancel();
                    equipKits(true);
                    teleportBase();
                    isDead = false;
                }
            }
        }.runTaskTimer(FTG.INSTANCE, 0L, 1L);
    }

    public void updateNowTile(Player p, Tile t){

        if(isDead){
            if(nowLocation != null){
                nowLocation.removePlayer(p);
                nowLocation.removeBossBar(p);
            }
            nowLocation = null;
            return;
        }

        if(nowLocation != null && nowLocation != t){
            nowLocation.removePlayer(p);
            nowLocation.removeBossBar(p);
        }
        this.nowLocation = t;
        if(t != null)
            nowLocation.addPlayer(p);
    }

    public void setObserve(boolean observe){
        this.observe = observe;
        if(observe)
            leaveTeam();
    }

    public void leaveTeam(){
        if(team != null){
            team.removePlayer(uuid);
            team = null;
        }
    }

    public void joinTeam(Team team){
        if(this.team != null)
            leaveTeam();
        team.addPlayer(uuid);
        this.team = team;

        if(Bukkit.getPlayer(uuid) != null)
            Bukkit.getPlayer(uuid).setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public void addKill(){
        kill++;
    }

    public void addDeath(){
        death++;
    }

    //public void capture

    public void equipKits(boolean reset){
        if(this.team == null || Bukkit.getPlayer(uuid) == null) return;

        Player p = Bukkit.getPlayer(uuid);

        if(reset){
            p.getInventory().clear();
            p.setGameMode(GameMode.ADVENTURE);
            p.setHealth(20.0D);
            p.setFoodLevel(20);
        }


        ContainerUtil.getInstance().setInventory(p,team.getColor().getInventory());

        /*
         p.getInventory().addItem(new ItemStack(Material.WOOD_SWORD));
         p.getInventory().setArmorContents(team.getArmors());
         p.updateInventory();
        */
    }

    public void teleportBase(){
        if(this.team == null || Bukkit.getPlayer(uuid) == null) return;
        Location loc = team.getColor().getBaseTile().getCenter().clone();
        Bukkit.getPlayer(uuid).teleport(loc.clone().add(0, 1, 0));
    }

    private final static ItemStack KIT_SWORD;

    static {
        KIT_SWORD = new ItemStack(Material.WOOD_SWORD);
        ItemMeta ism = KIT_SWORD.getItemMeta();
        ism.setUnbreakable(true);
        KIT_SWORD.setItemMeta(ism);
        KIT_SWORD.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
    }

}
