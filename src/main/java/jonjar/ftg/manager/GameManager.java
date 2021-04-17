package jonjar.ftg.manager;

import jonjar.ftg.FTG;
import jonjar.ftg.entity.PlayerInfo;
import jonjar.ftg.entity.Team;
import jonjar.ftg.entity.Tile;
import jonjar.ftg.file.ConfigManger;
import jonjar.ftg.file.DataManager;
import jonjar.ftg.util.ContainerUtil;
import jonjar.ftg.util.MsgSender;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

public class GameManager extends MsgSender {

    private final FTG plugin;
    public DataManager dm;

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

    public static int GAME_DURATION_SEC;
    public static int DROPS_TEMP;
    public static int PARTICLE;

    private int elapsed_tick;
    public static boolean PAUSE = false;

    public static GameManagerTask runnable;
    private MainGameTask main_task;
    public static boolean isFever;

    /*
    1. 관리자가 team 명령어를 통해 팀 배분 시켰는지 확인
    2.
     */

    public void start(Player p){
        GAME_DURATION_SEC = FTG.INSTANCE.config.getIntSetting(ConfigManger.SETTINGS.time);
        DROPS_TEMP = FTG.INSTANCE.config.getIntSetting(ConfigManger.SETTINGS.drops);
        PARTICLE = FTG.INSTANCE.config.getIntSetting(ConfigManger.SETTINGS.particle);
        if(runnable != null && !runnable.isCancelled()){
            error(p, "이미 게임이 시작되어있습니다.");
        } else {

            if(checkTeam()){
                if(DropsManager.Drop.Sum_modifier==0) error(p,"§c보급이 존재하지 않습니다. 확률을 확인해주세요.");
                runnable = new GameManagerTask();
                runnable.runTaskTimer(plugin, 0L, 1L);
                broadcast("§f§l" + p.getName() + "님께서 게임을 시작하셨습니다!");
                STATE = GameState.READY;
                isFever = false;
                for (Team t : Team.getTeams()) {
                    t.isSurvived = true;
                    t.cantRespawn = false;
                }
                SimpleDateFormat format1 = new SimpleDateFormat ("yyyy_MM_dd_HH_mm_ss");
                dm = new DataManager(format1.format(new Date()));
            } else {
                error(p, "§c팀 배분을 받지 못한 플레이어가 있습니다.");
            }

        }
    }

    public void stop(Player p){
        if(runnable == null || runnable.isCancelled()){
            error(p, "이미 게임이 종료되어있습니다.");
        } else {
            end();
            broadcast("§c§l" + p.getName() + "님께서 게임을 강제 종료하셨습니다.");
        }
    }

    private void end(){
        dm.saveData();
        dm.saveYaml();
        dm.resetData();

        STATE = GameState.WAIT;
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
        DropsManager.spawnedDrops.clear();

        for(PlayerInfo pi : PlayerInfo.getPlayerInfos()){ //FIXME:이거 잠깐 나가있으면 리셋 안되지 않니?
            UUID uuid = pi.getUUID();
            Player ap = Bukkit.getPlayer(uuid);
            if(ap != null){
                ap.getInventory().clear();
                ap.teleport(FTG.world.getSpawnLocation());
                ap.setGameMode(GameMode.ADVENTURE);
            }
            pi.reset();
        }

        Team.resetAll();

        Scoreboard sc = Bukkit.getScoreboardManager().getMainScoreboard();
        if(sc.getObjective(DisplaySlot.SIDEBAR) != null)
            sc.getObjective(DisplaySlot.SIDEBAR).setDisplaySlot(null);
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

            if(PlayerInfo.getPlayerInfo(p).isObserver())
                return;

            // 액션바 제어
            PlayerInfo pi = PlayerInfo.getPlayerInfo(p);
            String text = pi.getTeam().getColor().getKoreanName() + "팀 | §f" + pi.getKill() + "킬 " + pi.getDeath() + "데스 " + pi.getTileAssisted() + "점령";
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));

        }
    }

    public class GameManagerTask extends BukkitRunnable {
        public int remain_sec = 0;
        public final Random rn;

        private int drops_now = 0;
        private int drops_tick = 0;


        public GameManagerTask(){
            this.rn = new Random();
        }

        public void run() {

            if (PAUSE)
                return;


            // 보급
            if(elapsed_tick>200)
                drops_tick++;
            if (drops_tick == DROPS_TEMP) {
                drops_tick = 0;
                int increase = rn.nextInt(6) + 2; // 2~7
                drops_now += increase;
                if (drops_now >= 100) {
                    DropsManager.spawnRandomDrop();
                    MsgSender.getMsgSender().broadcast("보급 생성 뵤로롱");

                    drops_now = 0;
                }
            }
                for (Location l : DropsManager.spawnedDrops) {// CHECK : 비주얼자리 더 나은 파티클 생각 좀 해주셈.
                    for (int i = 0; i < PARTICLE; i++) {
                        FTG.world.spawnParticle(Particle.SPELL_MOB,
                                l.clone().add((rn.nextInt(100)) / 100D, 0, (rn.nextInt(100)) / 100D)
                                , 0, 0 / 255D, 255 / 255D, 0 / 255D, 1);
                    }
                }
                if (elapsed_tick % 20 == 0) {


                    remain_sec = (GAME_DURATION_SEC * 20 - elapsed_tick + 200) / 20;
                    if (remain_sec == 0) {

                        HashSet<Team> winners = new HashSet<>();

                        int max = 0;
                        Objective obj = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("tile");
                        for (Team t : Team.getTeams()) {
                            t.cantRespawn = true;
                            t.isSurvived = false; //일단 모든 팀 생존 플래그 끄기.
                            t.survivedPlayerNum = 0;

                            if (winners.isEmpty()) {
                                winners.add(t);
                                max = obj.getScore(t.getColor().getChatColor() + t.getTeam().getName()).getScore();
                            } else {
                                int i = obj.getScore(t.getTeam().getColor() + t.getTeam().getName()).getScore();
                                if (max < i) {
                                    winners.clear();
                                    winners.add(t);
                                    max = i;
                                } else if (max == i) {
                                    winners.add(t);
                                }
                            }
                        }
                        if (winners.size() != 1) {//무승부

                            if (FTG.INSTANCE.config.getIntSetting(ConfigManger.SETTINGS.fever) == 0) {
                                StringBuilder sb = new StringBuilder().append("우승 팀 : ");
                                for (Team t : winners) {
                                    sb.append(t.getColor().getKoreanName()).append(ChatColor.WHITE).append(", ");
                                }
                                sb.delete(sb.length() - 2, sb.length() - 1);
                                broadcast("============================");
                                broadcast(sb.toString());
                                broadcast("============================");
                                end();
                            } else {
                                StringBuilder sb = new StringBuilder().append("동률인 팀 : ");
                                for (Team t : Team.getTeams()) { //각 팀에서
                                    if (winners.contains(t)) { //동률 팀인경우
                                        sb.append(t.getColor().getKoreanName()).append(ChatColor.WHITE).append(", ");

                                        for (String UUID : t.getTeam().getEntries()) { //플레이어들을 뽑는다
                                            PlayerInfo pi = PlayerInfo.getPlayerInfoByUUID(UUID);

                                            t.isSurvived = true; //동률인 팀들만 생존 플래그 켜기.

                                            Player p = Bukkit.getPlayer(pi.getName());

                                            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 3, 5, false, false));
                                            if (pi.isDead) {//CHECK: 죽어있는 플레이어들 일괄부활
                                                pi.respawnTimer.cancel();
                                                pi.equipKits(true);
                                                pi.isDead = false;
                                            } else {
                                                p.setHealth(20);
                                            }
                                            pi.teleportBase();
                                            pi.getTeam().survivedPlayerNum++;
                                        }
                                    } else {//동률이 아닌 플레이어들 사망처리.
                                        for (String name : t.getTeam().getEntries()) { //플레이어들을 뽑는다
                                            PlayerInfo pi = PlayerInfo.getPlayerInfo(name);
                                            pi.onDeath();
                                        }

                                    }

                                }

                                sb.delete(sb.length() - 2, sb.length() - 1);

                                isFever = true;
                                broadcast("============================");
                                broadcast("현재 동률인 팀이 있으므로 리스폰이 금지되며, 최후의 팀이 나올 때 까지 게임이 진행됩니다.");
                                broadcast(sb.toString());
                                broadcast("============================");
                            }
                        } else {
                            broadcast("============================");
                            broadcast("우승 팀 : " + winners.iterator().next().getColor().getKoreanName());
                            broadcast("============================");
                            end();
                            return;
                        }


                    } else if (isFever) {
                        int feverTime = -remain_sec;
                        if (feverTime % 60 == 0) {
                            broadcast("§c연장 시간 " + feverTime / 60 + "분 경과.");
                        } else if (feverTime == 30) {
                            broadcast("§c연장 시간 " + feverTime + "초 경과.");
                        } else if (feverTime % 30 == 0) {
                            broadcast("§c연장 시간 " + feverTime / 60 + "분 " + feverTime % 60 + "초 경과.");
                        }
                    } else if (remain_sec <= 10 && remain_sec > 0) {
                        broadcast("§c§l게임 종료까지 " + remain_sec + "초 남았습니다.");
                    } else if (remain_sec == 30) {
                        broadcast("§c게임 종료까지 " + remain_sec + "초 남았습니다.");
                    } else if (remain_sec % 60 == 0) {
                        broadcast("§e게임 종료까지 " + (remain_sec / 60) + "분 남았습니다.");
                    }
                }

                switch (elapsed_tick++) {
                    case 40:
                        broadcast("§f==================");
                        broadcast("§b땅따먹기 미니게임");
                        broadcast("§7제작 : KimBepo, macham");
                        broadcast("§f==================");
                        break;
                    case 60:
                        broadcast("§7게임 설정 초기화 중...");
                        Tile.TILE_MAP.empty_tiles_set.clear();
                        for (Tile t : Tile.TILE_SET) {
                            t.resetInfo();
                        }
                        Tile.registerMasterTiles();
                        ContainerUtil.registerAllInventory();
                        break;
                    case 120:
                        broadcast("§f초기화 완료. 3초 뒤 게임을 시작합니다.");
                        break;
                    case 140:
                    case 160:
                    case 180:
                        broadcast("§c§l" + (10 - elapsed_tick / 20) + "초 전...");
                        break;
                    case 200:
                        for (Player ap : Bukkit.getOnlinePlayers()) {
                            PlayerInfo pi = PlayerInfo.getPlayerInfo(ap);
                            pi.teleportBase();
                            pi.equipKits(true);
                        }
                        broadcast("§b§l게임 시작! 최대한 많은 땅을 점령하세요!");
                        main_task = new MainGameTask();
                        main_task.runTaskTimer(plugin, 5L, 5L);
                        STATE = GameState.START;
                        Bukkit.getScoreboardManager().getMainScoreboard().getObjective("tile").setDisplaySlot(DisplaySlot.SIDEBAR);
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
