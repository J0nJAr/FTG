package jonjar.ftg.manager;

import jonjar.ftg.entity.PlayerInfo;
import jonjar.ftg.entity.Team;
import jonjar.ftg.util.MsgSender;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TeamManager extends MsgSender{

    public void help(Player p){
        msg(p, "§a/ftg team random §f관전자를 제외한 모두의 팀을 변경합니다.");
        msg(p, "§a/ftg team setting [2/3/4/6] §f[2/3/4/6]파전 팀을 세팅합니다.");
        msg(p, "§a/ftg team list §f현재 등록된 팀 목록을 확인합니다.");
        msg(p, "§a/ftg team join [COLOR] [PLAYER] §f플레이어를 팀에 추가합니다.");
        msg(p, "§a/ftg team leave [PLAYER] §f플레이어를 팀에서 제거합니다.");
        msg(p, "§a/ftg team reset §f모든 팀 설정을 리셋시킵니다.");
    }

    public void random(Player p, String[] args){
        // 1. 관전자 걸러내기
        List<Player> PlayerList = Bukkit.getOnlinePlayers().stream().filter(ap -> !PlayerInfo.getPlayerInfo(ap).isObserver()).collect(Collectors.toList());

        // 2. 셔플
        Collections.shuffle(PlayerList);

        // 3. 설정값 구하기
        List<Team> teams = Team.getTeams();
        int teamSize = teams.size();

        // 4. 팀 배치
        for(int i=0;i<PlayerList.size();i++){
            int temp = i % teamSize;
            join(PlayerInfo.getPlayerInfo(PlayerList.get(i)), teams.get(temp));
        }

        msg(p, "설정이 완료되었습니다.");
    }

    public void setting(Player p, String[] args){
        if(args.length <= 2){
            error(p, "올바른 명령어 : /ftg team setting [2/3/4/6]");
        } else {
            int value = 0;
            try{
                value = Integer.parseInt(args[2]);
            } catch(NumberFormatException ignored){ }

            if(value == 2 || value == 3 || value == 4 || value == 6){
                Team.resetAll();

                List<Team.TeamColor> teams = new ArrayList<>();
                switch(value){
                    case 2:
                        teams.add(Team.TeamColor.BLUE);
                        teams.add(Team.TeamColor.RED);
                    case 3:
                        teams.remove(Team.TeamColor.RED);
                        teams.add(Team.TeamColor.YELLOW);
                        teams.add(Team.TeamColor.PINK);
                    case 4:
                        teams.remove(Team.TeamColor.YELLOW);
                        teams.add(Team.TeamColor.GREEN);
                        teams.add(Team.TeamColor.RED);
                    case 6:
                        teams.add(Team.TeamColor.ORANGE);
                        teams.add(Team.TeamColor.YELLOW);
                }

                for(Team.TeamColor tc : teams){
                    Team team = new Team(tc);
                    team.register();
                }

                msg(p, value + "파전으로 세팅 완료!");
            } else {
                error(p, "2파전, 3파전, 4파전, 6파전만 가능합니다. 파전에간장찍어먹고싶다");
            }
        }
    }

    public void list(Player p){
        if(Team.getTeams().isEmpty()){
            error(p, "세팅된 팀이 없습니다.");
        } else {
            msg(p, "현재 팀 목록 : ");
            for(Team t : Team.getTeams()){
                msg(p, t.getColor().getChatColor() + t.getColor().name() + " §7[" + t.getTeam().getEntries().size() + "명]");
            }
        }
    }

    public void join(Player p, String[] args){
        if(args.length <= 3)
            error(p, "올바른 명령어 : /ftg team join [TEAM] [PLAYER]");
        else {
            Team team = Team.getTeamByName(args[2]);
            if(team == null){
                error(p, args[2] + " 팀은 없는 팀입니다.");
                return;
            }

            PlayerInfo pi = PlayerInfo.getPlayerInfo(args[3]);
            if(pi == null){
                error(p, args[3] + " 은(는) 없는 플레이어입니다.");
                return;
            } else if(pi.isObserver()){
                error(p, "대상 플레이어가 관전자로 등록되어있습니다.");
                return;
            }

            join(pi, team);
            msg(p, args[3] + " 님을 " + team.getColor().getKoreanName() + " §f팀으로 이동시켰습니다.");
        }
    }

    public void leave(Player p, String[] args){
        if(args.length <= 2)
            error(p, "올바른 명령어 : /ftg team leave [PLAYER]");
        else {
            PlayerInfo pi = PlayerInfo.getPlayerInfo(args[2]);
            if(pi == null){
                error(p, args[2] + " 은(는) 없는 플레이어입니다.");
                return;
            } else if(pi.getTeam() == null){
                error(p, "해당 플레이어는 팀이 없습니다.");
                return;
            }

            leave(pi);
            msg(p, args[2] + " 님을 팀에서 탈퇴시켰습니다.");
        }
    }

    public void reset(Player p){
        Team.resetAll();
        msg(p, "모든 팀을 리셋시켰습니다.");
    }



    private void join(PlayerInfo info, Team team){
        info.joinTeam(team);
    }

    private void leave(PlayerInfo info){
        info.leaveTeam();
    }
}
