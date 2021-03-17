package jonjar.ftg.manager;

import jonjar.ftg.FTG;
import jonjar.ftg.entity.PlayerInfo;
import jonjar.ftg.util.MsgSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class GameManager extends MsgSender {
    
    private final FTG plugin;
    public GameManager(FTG main){
        this.plugin = main;
    }

    private int elapsed_tick;

    private GameManagerTask runnable;

    /*
    1. 관리자가 team 명령어를 통해 팀 배분 시켰는지 확인
    2.
     */

    public void start(Player p){
        if(runnable != null && !runnable.isCancelled()){
            error(p, "이미 게임이 시작되어있습니다.");
        } else {
            reset();
            runnable = new GameManagerTask();
            runnable.runTaskTimer(plugin, 0L, 20L);
        }
    }

    private void reset(){
        elapsed_tick = 0;
    }


    public class GameManagerTask extends BukkitRunnable {

        public void run(){

        }

    }
}
