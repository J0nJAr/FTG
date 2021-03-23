package jonjar.ftg.manager;

import jonjar.ftg.FTG;
import jonjar.ftg.entity.PlayerInfo;
import jonjar.ftg.entity.Team;
import jonjar.ftg.entity.Tile;
import jonjar.ftg.util.ContainerUtil;
import jonjar.ftg.util.LocationUtil;
import jonjar.ftg.util.MsgSender;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

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

    public final static int GAME_DURATION_SEC = 60 * 5;

    private int elapsed_tick;
    public static boolean PAUSE = false;

    private GameManagerTask runnable;
    private MainGameTask main_task;


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
                runnable.runTaskTimer(plugin, 0L, 1L);
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

    private void end(){
        runnable.cancel();
        runnable = null;
        reset();
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
        this.elapsed_tick = 0;

        for(Player ap : Bukkit.getOnlinePlayers()){
            ap.getInventory().clear();
            ap.teleport(FTG.world.getSpawnLocation());
            ap.setGameMode(GameMode.ADVENTURE);
            PlayerInfo pi = PlayerInfo.getPlayerInfo(ap);
            pi.reset();
        }

        Team.resetAll();
        Tile.unregisterDummy();

        Scoreboard sc = Bukkit.getScoreboardManager().getMainScoreboard();
        if(sc.getObjective(DisplaySlot.SIDEBAR) != null)
            sc.getObjective(DisplaySlot.SIDEBAR).setDisplaySlot(null);

        /*
        TODO :
        1. 플레이어 모두 티피올 O
        2. 플레이어 아이템 초기화 O
        3. 팀 초기화 O
        4. 타일 초기화 O
        5. 점수 등 DB 초기화
         */
    }

    public class MainGameTask extends BukkitRunnable {
        public void run(){

            if(PAUSE)
                return;

            if(STATE == GameState.END){
                this.cancel();
                return;
            }

            for(Player ap : Bukkit.getOnlinePlayers())
                updatePlayer(ap);
        }

        private void updatePlayer(Player p){
            Scoreboard sc = p.getScoreboard();
            if(sc == null){
                sc = Bukkit.getScoreboardManager().getMainScoreboard();
                p.setScoreboard(sc);
            }

            // 액션바 제어
            PlayerInfo pi = PlayerInfo.getPlayerInfo(p);
            String text = pi.getTeam().getColor().getKoreanName() + "팀 | §f" + pi.getKill() + "킬 " + pi.getDeath() + "데스 " + pi.getTileAssisted() + "점령";
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));

            //

        }
    }

    public class GameManagerTask extends BukkitRunnable {

        public void run(){
            
            if(PAUSE)
                return;

            if(elapsed_tick % 20 == 0){
                int remain_sec = (GAME_DURATION_SEC * 20 - elapsed_tick + 200) / 20;
                if(remain_sec % 60 == 0){
                    broadcast("§e게임 종료까지 " + (remain_sec / 60) + "분 남았습니다.");
                } else if(remain_sec == 30){
                    broadcast("§c게임 종료까지 " + remain_sec + "초 남았습니다.");
                } else if(remain_sec <= 10 && remain_sec > 0){
                    broadcast("§c§l게임 종료까지 " + remain_sec + "초 남았습니다.");
                } else if(remain_sec == 0){
                    end();
                }
            }


            switch(elapsed_tick++){
                case 40:
                    broadcast("§f==================");
                    broadcast("§b땅따먹기 미니게임");
                    broadcast("§7제작 : KimBepo, macham");
                    broadcast("§f==================");
                    break;
                case 60:
                    broadcast("§7게임 설정 초기화 중...");
                    Tile.registerDummy();
                    for(Tile t : Tile.TILE_SET) {
                        t.resetInfo();
                    }
                    Tile.registerMasterTiles();
                    ContainerUtil.getInstance().registerAllInventory();
                    break;
                case 120:
                    broadcast("§f초기화 완료. 3초 뒤 게임을 시작합니다.");
                    break;
                case 140:
                case 160:
                case 180:
                    broadcast("§c§l" + (10-elapsed_tick/20) + "초 전...");
                    break;
                case 200:
                    for(Player ap : Bukkit.getOnlinePlayers()) {
                        PlayerInfo pi = PlayerInfo.getPlayerInfo(ap);
                        pi.teleportBase();
                        pi.equipKits(true);
                    }
                    broadcast("§b§l게임 시작! 최대한 많은 땅을 점령하세요!");
                    main_task = new MainGameTask();
                    main_task.runTaskTimer(plugin, 5L, 5L);

                    Bukkit.getScoreboardManager().getMainScoreboard().getObjective("tile").setDisplaySlot(DisplaySlot.SIDEBAR);
                    break;
                case GAME_DURATION_SEC * 20:
                    break;
            }
        }

        public void cancel(){
            super.cancel();
            if(main_task != null && !main_task.isCancelled())
                main_task.cancel();
            main_task = null;
        }

    }
}
