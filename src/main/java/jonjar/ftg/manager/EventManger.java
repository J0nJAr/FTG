package jonjar.ftg.manager;

import jonjar.ftg.FTG;
import jonjar.ftg.entity.PlayerInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class EventManger implements Listener {

    private final FTG main;

    public EventManager(FTG plugin){
        main = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player p = event.getPlayer();
        PlayerInfo pi = PlayerInfo.getPlayerInfo(p);
        if(pi == null){
            new PlayerInfo(p.getName(), p.getUniqueId());
        }


    }

}
