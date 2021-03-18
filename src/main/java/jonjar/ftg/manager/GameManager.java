package jonjar.ftg.manager;

import jonjar.ftg.FTG;
import jonjar.ftg.entity.PlayerInfo;
import jonjar.ftg.entity.Team;
import jonjar.ftg.entity.Tile;
import jonjar.ftg.util.MsgSender;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class GameManager extends MsgSender {

    private final FTG plugin;
    public GameManager(FTG main){
        this.plugin = main;
    }

    public enum GameState {
        WAIT,
        READY,
        START,
        END;
    }

    public static GameState STATE = GameState.WAIT;

    private int elapsed_sec;
    public static boolean PAUSE = false;

    private GameManagerTask runnable;


    /*
    1. 관리자가 team 명령어를 통해 팀 배분 시켰는지 확인
    2.
     */

    public void start(Player p){
        if(runnable != null && !runnable.isCancelled()){
            error(p, "이미 게임이 시작되어있습니다.");
        } else {

            if(checkTeam()){
                runnable = new GameManagerTask();
                runnable.runTaskTimer(plugin, 0L, 20L);
                broadcast("§f§l" + p.getName() + "님께서 게임을 시작하셨습니다!");
                STATE = GameState.READY;
            } else {
                error(p, "§c팀 배분을 받지 못한 플레이어가 있습니다.");
            }

        }
    }

    public void stop(Player p){
        if(runnable == null || runnable.isCancelled()){
            error(p, "이미 게임이 종료되어있습니다.");
        } else {
            runnable.cancel();
            runnable = null;
            reset();
            broadcast("§c§l" + p.getName() + "님께서 게임을 강제 종료하셨습니다.");
        }
    }
    
    public void pause(Player p){
        if(runnable == null || runnable.isCancelled())
            error(p, "§c게임이 시작되어있지 않습니다.");
        else {
            PAUSE = !PAUSE;
            broadcast(PAUSE ? "§c§l" + p.getName() + "님께서 게임을 일시정지하셨습니다." : "§f§l" + p.getName() + "님께서 게임을 재개하셨습니다!");
        }
    }

    private boolean checkTeam(){

        for(Player ap : Bukkit.getOnlinePlayers()){
            PlayerInfo pi = PlayerInfo.getPlayerInfo(ap);
            if(pi.getTeam() == null && !pi.isObserver())
                return false;
        }

        return true;
    }
    
    

    private void reset(){
        this.elapsed_sec = 0;

        for(Player ap : Bukkit.getOnlinePlayers()){
            ap.getInventory().clear();
            ap.teleport(FTG.world.getSpawnLocation());
            ap.setGameMode(GameMode.ADVENTURE);
        }

        Team.resetAll();
        Tile.unregisterDummy();

        /*
        TODO :
        1. 플레이어 모두 티피올 O
        2. 플레이어 아이템 초기화 O
        3. 팀 초기화 O
        4. 타일 초기화 O
        5. 점수 등 DB 초기화
         */
    }


    public class GameManagerTask extends BukkitRunnable {

        public void run(){
            
            if(PAUSE)
                return;
            
            switch(elapsed_sec++){
                case 1:
                    broadcast("§f==================");
                    broadcast("§b땅따먹기 미니게임");
                    broadcast("§7제작 : KimBepo, macham");
                    broadcast("§f==================");
                    break;
                case 2:
                    broadcast("§7게임 설정 초기화 중...");
                    Tile.registerTiles();

                    break;
                case 5:
                    broadcast("§f초기화 완료. 3초 뒤 게임을 시작합니다.");
                    break;
                case 6:
                case 7:
                case 8:
                    broadcast("§c§l" + (9-elapsed_sec) + "초 전...");
                    break;
                case 9:
                    for(Player ap : Bukkit.getOnlinePlayers()) {
                        PlayerInfo pi = PlayerInfo.getPlayerInfo(ap);
                        pi.teleportBase();
                        pi.equipKits(true);
                    }
                    broadcast("§b§l게임 시작! 최대한 많은 땅을 점령하세요!");
                    break;
            }
        }

    }
}
