package jonjar.ftg.entity;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerInfo {

    private static HashMap<String, PlayerInfo> PlayerInfoList = new HashMap<>();

    public static PlayerInfo getPlayerInfo(Player p){
        return PlayerInfoList.get(p.getName().toLowerCase());
    }
    public static PlayerInfo getPlayerInfo(String name) { return PlayerInfoList.get(name.toLowerCase()); }


    private final String name;
    private final UUID uuid;

    private boolean observe = false;
    private Team team;


    public PlayerInfo(String name, UUID uuid){
        this.name = name;
        this.uuid = uuid;
        PlayerInfoList.put(name.toLowerCase(), this);
    }

    public String getName() { return this.name; }
    public UUID getUUID() { return this.uuid; }
    public Team getTeam() { return this.team; }
    public boolean isObserver() { return this.observe; }

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
    }

    public void equipKits(boolean reset){
        if(this.team == null || Bukkit.getPlayer(uuid) == null) return;

        Player p = Bukkit.getPlayer(uuid);
        if(reset){
            p.getInventory().clear();
            p.setGameMode(GameMode.ADVENTURE);
            p.setHealth(20.0D);
        }

        p.getInventory().addItem(new ItemStack(Material.WOOD_SWORD));
        p.getInventory().setArmorContents(team.getArmors());
        p.updateInventory();
    }

    public void teleportBase(){
        if(this.team == null || Bukkit.getPlayer(uuid) == null) return;
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
