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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class EventManager implements Listener {

    private final FTG main;

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
    public void onMove(PlayerMoveEvent event){
        Player p = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        if(from.getX() != to.getX() || from.getZ() != to.getZ()){

            Material mat = to.getWorld().getBlockAt(to.getBlockX(), 5, to.getBlockZ()).getType();
            if(mat == Material.CONCRETE){
                Entity near = LocationUtil.getClosestEntityType(to, 10.0D, EntityType.ARMOR_STAND);
                String name = near.getCustomName();

                String[] split = name.split(",");
                int x = Integer.parseInt(split[0]);
                int z = Integer.parseInt(split[1]);

                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§e" + x + " / " + z));
            } else {
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cNONE"));
            }

        }
    }

}
