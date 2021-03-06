package jonjar.ftg.entity;

import jonjar.ftg.FTG;
import jonjar.ftg.file.DataManager;
import jonjar.ftg.manager.GameManager;
import jonjar.ftg.util.ContainerUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class PlayerInfo {

    public final static int RESPWAN_TICK = 100;

    public static HashMap<String, PlayerInfo> PlayerInfoList = new HashMap<>();

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

    public static Collection<PlayerInfo> getPlayerInfos(){
        return PlayerInfoList.values();
    }


    private final String name;
    private final UUID uuid;

    private boolean observe = false;
    public boolean isDead;
    private Team team;



    public DataManager.PlayerStats stats;

    private Tile nowLocation;


    public PlayerInfo(String name, UUID uuid){
        this.stats = new DataManager.PlayerStats(this);
        this.name = name;
        this.uuid = uuid;
        this.isDead = false;
        PlayerInfoList.put(name.toLowerCase(), this);
    }
    @Deprecated//수정하기 귀찮아서 일단 태그만 달아둠.
    public int getKill(){ return (int) stats.stat_map.get(DataManager.PlayerStats.Stats.kill); }
    @Deprecated
    public int getDeath() { return (int) stats.stat_map.get(DataManager.PlayerStats.Stats.death); }
    @Deprecated
    public int getTileAssisted() { return (int) stats.stat_map.get(DataManager.PlayerStats.Stats.tile_assist); }

    public Tile getNowLocation() { return this.nowLocation; }
    public String getName() { return this.name; }
    public UUID getUUID() { return this.uuid; }
    public Team getTeam() { return this.team; }
    public boolean isObserver() { return this.observe; }
    public boolean isDead() { return this.isDead; }



    public void reset() {
        this.stats.reset();
    }

    public BukkitTask respawnTimer;
    public void onDeath(){
        if(observe) return;

        addDeath();

        Player p = Bukkit.getPlayer(uuid);
        p.setGameMode(GameMode.SPECTATOR);
        if(nowLocation != null){
            nowLocation.removePlayer(p);
            nowLocation.removeBossBar(p);
        }
        nowLocation = null;
        isDead = true;


        respawnTimer = new BukkitRunnable() {
            public int tick = RESPWAN_TICK;

            @Override
            public void cancel(){
                isDead = false;
                super.cancel();
            }

            public void run(){

                if(GameManager.STATE != GameManager.GameState.START){
                    cancel();
                    return;
                }

                if(!p.isOnline())
                    return;

                tick--;

                Team t = PlayerInfo.getPlayerInfo(p).getTeam();

                if(tick % 10 == 0){//1초마다 메세지 출력,
                    if(GameManager.isFever) { //연장시간이면
                        if (t.isSurvived){// 팀 생존상태
                        p.sendTitle("남은 인원 : "+ t.survivedPlayerNum, "§f연장시간 §c" + -GameManager.runnable.remain_sec + "초§f 경과", 0, 24, 0);
                        }else if (!t.isSurvived){//팀 비생존 상태(관전 only)
                        p.sendTitle("", "§f연장시간 §c" + -GameManager.runnable.remain_sec + "초§f 경과", 0, 24, 0);
                        }
                    } else{ //연장시간이 아님.
                        p.sendTitle("", "§e" + tick/20 + "§f초 뒤 리스폰됩니다.", 0, 24, 0);
                    }

                    if (t.cantRespawn) { //리스폰 불가인지 확인하고, 불가이면 return해서 끊기.
                        return;  //리스폰 불가일때 끊기
                    }

                    if(tick == 0){
                        p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,3,5,false,false));

                        this.cancel();
                        equipKits(true);
                        teleportBase();
                        isDead = false;
                    }
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
            team.removePlayer(name);
            team.playerInfos.remove(this);
            team = null;
        }
    }

    public void joinTeam(Team team){
        if(this.team != null)
            leaveTeam();

        team.addPlayer(name);
        team.playerInfos.add(this);
        this.team = team;

        if(Bukkit.getPlayer(uuid) != null)
            Bukkit.getPlayer(uuid).setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    @Deprecated
    public void addKill(){
        stats.add(DataManager.PlayerStats.Stats.kill);
    }
    @Deprecated
    public void addDeath(){
        stats.add(DataManager.PlayerStats.Stats.death);
    }
    @Deprecated
    public void addTileAssisted() { stats.add(DataManager.PlayerStats.Stats.tile_assist); }
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


        ContainerUtil.setInventory(p,team.getColor().getInventory());

        /*
         p.getInventory().addItem(new ItemStack(Material.WOOD_SWORD));
         p.getInventory().setArmorContents(team.getArmors());
         p.updateInventory();
        */
    }

    public void teleportBase(){
        if(this.team == null || Bukkit.getPlayer(uuid) == null) return;
        Location loc = team.getColor().getBaseTile().getCenter().clone();
        loc.setDirection(Tile.TILE_MAP.getTile(0,6).getCenter().toVector().subtract(loc.toVector()));//중앙을 보도록
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
