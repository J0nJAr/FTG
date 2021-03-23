package jonjar.ftg.manager;

import jonjar.ftg.FTG;
import jonjar.ftg.entity.PlayerInfo;
import jonjar.ftg.entity.Tile;
import jonjar.ftg.util.LocationUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class EventManager implements Listener {

    private final FTG main;

    private final int TILES_Y = 5;

    public EventManager(FTG plugin){
        main = plugin;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event){
        Player p = event.getPlayer();
        if(!p.isOp())
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event){
        Player p = event.getPlayer();
        if(!p.isOp())
            event.setCancelled(true);
    }

    @EventHandler
    public void onHit(EntityDamageEvent event){
        if(event.getEntity() instanceof Player){
            if(GameManager.STATE != GameManager.GameState.START)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player p = event.getPlayer();
        PlayerInfo pi = PlayerInfo.getPlayerInfo(p);
        if(pi == null){
            new PlayerInfo(p.getName(), p.getUniqueId());
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        if(GameManager.STATE != GameManager.GameState.START) return;
        Player e = event.getEntity();
        Player p = e.getKiller();
        PlayerInfo ei = PlayerInfo.getPlayerInfo(e);
        ei.addDeath();
        if(p != null){
            PlayerInfo pi = PlayerInfo.getPlayerInfo(p);
            pi.addKill();
        }
        event.setKeepInventory(true);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event){
        if(GameManager.STATE != GameManager.GameState.START) return;
        Player e = event.getPlayer();
        PlayerInfo ei = PlayerInfo.getPlayerInfo(e);
        if(ei.getTeam() != null){
            event.setRespawnLocation(ei.getTeam().getColor().getBaseTile().getCenter().clone().add(0, 1, 0));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player p = event.getPlayer();
        PlayerInfo pi = PlayerInfo.getPlayerInfo(p);
        pi.updateNowTile(p, null);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event){
        Player p = event.getPlayer();
        PlayerInfo pi = PlayerInfo.getPlayerInfo(p);
        Location from = event.getFrom();
        Location to = event.getTo();
        if(from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ()){
            Tile t = LocationUtil.getClosestTile(to);
            pi.updateNowTile(p, t);

        }
    }

}
