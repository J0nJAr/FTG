package jonjar.ftg.manager;

import jonjar.ftg.FTG;
import jonjar.ftg.entity.PlayerInfo;
import jonjar.ftg.entity.Team;
import jonjar.ftg.entity.Tile;
import jonjar.ftg.util.LocationUtil;
import jonjar.ftg.util.MsgSender;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;

public class EventManager implements Listener {

    private final FTG main;

    private final int TILES_Y = 5;

    public EventManager(FTG plugin){
        main = plugin;
    }

    // TODO  : 보급상자 클릭하면 아시죠?
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
            Player e = (Player) event.getEntity();
            if(PlayerInfo.getPlayerInfo(e).isDead())
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event){
        if(event.getEntity() instanceof Player){
            Player e = (Player) event.getEntity();
            Player p = null;
            if(event.getDamager() instanceof Player)
                p = (Player) event.getDamager();
            else if(event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() != null){
                Projectile proj = (Projectile) event.getDamager();
                if(proj.getShooter() != null && proj.getShooter() instanceof Player){
                    p = (Player) proj.getShooter();
                }
            }
            if(p != null){
                PlayerInfo pi = PlayerInfo.getPlayerInfo(p);
                PlayerInfo ei = PlayerInfo.getPlayerInfo(e);
                if(pi.getTeam() == ei.getTeam())
                    event.setCancelled(true);
            }

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
    public void onFalling(EntityChangeBlockEvent event){
        if(event.getEntity() instanceof FallingBlock){
            FallingBlock fb = (FallingBlock) event.getEntity();
            if(DropsManager.spawnedFallingDrops.contains(fb.getEntityId())){
                DropsManager.spawnedFallingDrops.remove(fb.getEntityId());
                DropsManager.onDropsFall(fb);
                event.setCancelled(true);
                fb.remove();
            }
         }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        if(DropsManager.removeSpawnedDrop(event)) return;

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


        Team t = ei.getTeam();

        if(GameManager.isFever&&t.isSurvived&&!ei.isDead){
            t.survivedPlayerNum--;
            if(t.survivedPlayerNum==0){ //해당 팀 탈락 확인
                t.isSurvived = false;
                MsgSender.getMsgSender().broadcast(t.getColor().getKoreanName(t.getColor().getChatColor().toString()+ChatColor.BOLD)+"팀"+ ChatColor.RED +ChatColor.BOLD+"이 탈락했습니다.");
                /*TODO:팀 탈락 이벤트
                       팀 등수 확인하기
                       우승 이벤트 필요.
                */
            }
        }

        ei.onDeath();

        if(p != null){
            PlayerInfo pi = PlayerInfo.getPlayerInfo(p);
            pi.addKill();
        }

        e.spigot().respawn();
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

    @EventHandler
    public void InventoryClickEvent(InventoryClickEvent event){
        DropsManager.InventoryClickEvent(event);
    }
}
