package jonjar.ftg.entity;


import jonjar.ftg.FTG;
import org.bukkit.Bukkit;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;


public class Tile {
    //final private Material outline = FTG.outline;
    static final private int radius = 6;
    final private static int RADIUS = 6;

    private Set<Location> locationSet;
    private Set<Block> blockSet;
    private Set<Block> belowblockSet;


    public static final Location LOCATION_0 =  new Location(FTG.world,153D,5D,-52D);
    public static final float DOMINATE_RATIO = 0.03F;

    private ArrayList<Set<Tile>> nearTileList; //거리별 타일
    private HashMap<Tile,Integer> tileDistanceList;//타일별 거리



    public static TileMapC TILE_MAP= new TileMapC();
    public static Set<Tile> TILE_SET = new HashSet<>();


    private final TileRunnable runnable;
    private BossBar bar;

    private final Location center;
    public final int x_index, z_index;

    private Team own;
    private Team attempt;
    private boolean isMasterTile;

    private Block dropsBlock;

    //
    private HashMap<Team.TeamColor, Float> progress;

    private final HashMap<Team, List<Player>> PlayerList;

    Tile(Location center, int x_index, int y_index){
        this.center = center;
        this.dropsBlock = center.getBlock().getRelative(BlockFace.UP);
        this.x_index=x_index;
        this.z_index=y_index;


        this.bar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);

        this.isMasterTile = false;

        this.registerLocations();
        nearTileList = new ArrayList<>();
        PlayerList = new HashMap<>();
        progress = new HashMap<>();
        for(Team.TeamColor tc : Team.TeamColor.values()){
            progress.put(tc, 0F);
        }

        this.runnable = new TileRunnable();
        runnable.runTaskTimer(FTG.INSTANCE, 0L, 2L);
    }

    public void printAll(){
        Bukkit.broadcastMessage("=== tiles info ===");
        Bukkit.broadcastMessage("own : " + own);
        Bukkit.broadcastMessage("attempt : " + attempt);

        for(Team t : PlayerList.keySet()){
            Bukkit.broadcastMessage("PlayerList : ");
            for(Player ap : PlayerList.get(t)){
                Bukkit.broadcastMessage("- " + ap.getName());
            }
        }

    }

    public boolean isMasterTile(){
        return this.isMasterTile;
    }
    public Team getOwnTeam() { return this.own; }

    public void setMasterTile(){
        this.isMasterTile = true;
    }

    public void removeBossBar(Player p){
        bar.removePlayer(p);
    }

    public void addPlayer(Player p){

        PlayerInfo pi = PlayerInfo.getPlayerInfo(p);
        if(pi.isObserver()) return;

        Team tc = pi.getTeam();
        if(!PlayerList.containsKey(tc))
            PlayerList.put(tc, new ArrayList<>());

        List<Player> list = PlayerList.get(tc);
        if(!list.contains(p))
            list.add(p);
    }

    public void removePlayer(Player p){
        PlayerInfo pi = PlayerInfo.getPlayerInfo(p);
        if(pi.isObserver()) return;

        Team tc = pi.getTeam();
        if(!PlayerList.containsKey(tc))
            PlayerList.put(tc, new ArrayList<>());

        PlayerList.get(tc).remove(p);
    }

    @Override
    public String toString() {
        return this.x_index+","+this.z_index;
    }

    public Set<Block> getBlocks(){
        if(this.blockSet == null){//아직 블럭 set가 없으면 생성
            blockSet = new HashSet<>();
            belowblockSet = new HashSet<>();
            for (Location location : this.getLocations()) {
                blockSet.add(location.getBlock());
                belowblockSet.add(location.getBlock().getRelative(BlockFace.DOWN));
            }
        }
        return blockSet;
    }

    private void modifyProgress(Team.TeamColor tc, float amount){
        float now = progress.get(tc);
        now += amount;
        if(now < 0F) now = 0F;
        else if(now > 1F) now = 1F;

        progress.put(tc, now);
    }

    private void onMasterTileOccupied(Team t, List<Player> players){

        isMasterTile = false;

        if(own == null) return;

        Team cut = own;

        for(String member : cut.getTeam().getEntries()){
            PlayerInfo pi = PlayerInfo.getPlayerInfo(member);
            pi.onDeath();
        }
        cut.isSurvived = false;
        own.removeAllTiles();

        Player p = players.get(0);
        String sub = PlayerInfo.getPlayerInfo(p).getTeam().getColor().getChatColor() + p.getName() + "§f님이 마스터 타일을 점령하셨습니다!";

        for(Player ap : Bukkit.getOnlinePlayers()){
            ap.playSound(ap.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0F, 1.0F);
            ap.sendTitle(cut.getColor().getKoreanName() + " §c§l탈락!", sub, 20, 60, 20);
        }

    }

    public void processOccupation(Team t, int n){

        float amount = DOMINATE_RATIO + ((n-1) * DOMINATE_RATIO / 2F);
        if(own != null && own == t){
            // 주인장이 점령하는 중
            for(Team.TeamColor key : progress.keySet()){
                if(t.getColor() != key)
                    modifyProgress(key, -amount);
            }
            colorAll(t);
        } else {
            // 주인장 아닌놈이 점령하는 중
            modifyProgress(t.getColor(), amount);
            if(progress.get(t.getColor()) >= 1.0F){
                // 점령!
                List<Player> players = PlayerList.get(t);

                for(Team.TeamColor key : progress.keySet()){
                    if(t.getColor() != key)
                        progress.put(key, 0F);
                }

                if(isMasterTile){
                    // 마스터 타일 점령 시
                    onMasterTileOccupied(t, players); // CHECK: 테스트 해볼 것
                }

                if(own != null){
                    own.removeTile(this);
                }

                own = t;

                for(Player ap : players){
                    PlayerInfo pi = PlayerInfo.getPlayerInfo(ap);
                    pi.addTileAssisted();
                }
                t.addTile(this);

                colorAll(t);
            } else {
                colorAll(t, true);
            }
            runnable.flag_colored = false;
        }
    }

    public void resetInfo(){
        isMasterTile = false;
        PlayerList.clear();
        dropsBlock.setType(Material.AIR);
        for(Team.TeamColor tc : Team.TeamColor.values()){
            progress.put(tc, 0F);
        }
        own = null;
        attempt = null;
        bar.removeAll();
        colorAll(null);
        TILE_MAP.empty_tiles_set.add(this);
    }

    public void colorAll(Team t){
        colorAll(t, false);
    }

    public void colorAll(Team t, boolean onProgress){
        Material mat = !onProgress ? Material.CONCRETE : Material.STAINED_GLASS;
        short dura = 8;
        if(t != null){
            Team.TeamColor tc = t.getColor();
            dura = tc.getData();
        }
        if(!onProgress)
            own = t;
        for(Block b : getBlocks()){
            b.setType(mat);
            b.setData((byte) dura);
        }

        if(onProgress){
            for(Block b : belowblockSet){
                b.setType(Material.CONCRETE);
                b.setData((byte) dura);
            }
        }

    }

    private void colorTeams_random(Team... t){
        Random rn = new Random();
        int i = rn.nextInt(t.length);
        Team now = t[i];

        for(Block b : belowblockSet){
            short dura = now.getColor().getData();
            b.setType(Material.CONCRETE);
            b.setData((byte) dura);
        }
        for(Block b : blockSet){
            short dura = now.getColor().getData();
            b.setType(Material.STAINED_GLASS);
            b.setData((byte) dura);
        }
    }

    public void colorTeams(Team... t){

        int temp = 0;
        int size = t.length;

        for(Block b : belowblockSet){
            Team now = t[temp % size];
            short dura = now.getColor().getData();
            b.setType(Material.CONCRETE);
            b.setData((byte) dura);
            temp++;
        }

        temp = 0;

        for(Block b : blockSet){
            Team now = t[temp % size];
            short dura = now.getColor().getData();
            b.setType(Material.STAINED_GLASS);
            b.setData((byte) dura);
            temp++;
        }
    }

    public void registerLocations(){
        locationSet = new HashSet<>();

        for (int z=0;z<=2*radius;z++) {
            locationSet.add(center.clone().add(0,0,z-radius));
        }

        for(int x=1;x<=radius;x++){
            locationSet.add(center.clone().add(x,0,0));
            locationSet.add(center.clone().subtract(x,0,0));

            for(int z=1;z<=Math.min(radius,radius*2+1-2*x);z++){
                locationSet.add(center.clone().add(x,0,z));
                locationSet.add(center.clone().add(-x,0,z));
                locationSet.add(center.clone().subtract(x,0,z));
                locationSet.add(center.clone().subtract(-x,0,z));
            }

        }
    }

    public static boolean isValid(int x_index,int z_index){
        if(z_index<0) return false;
        if(Math.abs(x_index)>RADIUS) return false;
        if((RADIUS*2-Math.abs(x_index))<z_index) return false;

        return true;
    }

    @Deprecated
    public boolean isAdjacent(int x_index,int z_index){

        if(isValid(x_index,z_index)) return false;

        if(Math.abs(z_index-this.z_index)<=1 && Math.abs(x_index-this.x_index)<=1){
            return true;
        }

        return false;
    }

    public static void registerAllNearTileList(){
        for(Tile tile:TILE_SET) tile.registerNearTileList();
    }

    public HashMap<Tile, Integer> getTileDistanceList() {
        return tileDistanceList;
    }

    public void registerNearTileList() { //BFS
        boolean[][] visited;

        visited = new boolean[RADIUS*2+1][RADIUS*2+1];
        for(int i=0;i<RADIUS*2+1;i++) for(int j=0;j<RADIUS*2+1;j++){
            visited[i][j]=false;
        }

        int distance = 0;
        nearTileList.add(new HashSet<Tile>());
        nearTileList.get(distance).add(this);//자기 자신 추가
        visited[6+this.x_index][this.z_index] = true;
        Queue<Tile> q = new LinkedList<>();
        q.add(this);

        while (!q.isEmpty()){
            distance++;

           // Bukkit.broadcastMessage("==========깊이 :"+distance+"============");
            int size = q.size();
            for(int i=0;i<size;i++) {
                int[] _x = { 1, 1, 0, 0,-1,-1};
                int[] _z = {-1, 0,-1, 1, 0, 1};
               // Bukkit.broadcastMessage("---:"+i+"---");
                Tile tile = q.poll();
                for (int j=0;j<6;j++) {
                        int z;
                        int x;
                        x = tile.x_index + _x[j];
                        if(x>0) z = tile.z_index + _z[j];
                        else if (x==0&&_x[j]!=0) z= tile.z_index + Math.abs(_z[j]);
                        else z = tile.z_index - _z[j];
                    //    Bukkit.broadcastMessage("x, z"+x+","+z);

                        if (isValid(x,z)&&!visited[6 + x][z]) {
                            visited[6 + x][z] = true;

                            if (!(nearTileList.size() < distance)) {
                                nearTileList.add(new HashSet<>());
                            }
                            Tile theTile = TILE_MAP.getTile(x,z);

                            nearTileList.get(distance).add(theTile);

                           // Bukkit.broadcastMessage("거리 : " + distance + "[타일a " + this.toString() + "타일b " + theTile.toString() + "]");
                            q.add(theTile);
                        }
                    }
            }
        }

    }
    public void registerDistanceTileList(){
        int i = 0;
        for(Set<Tile> tile : this.getNearTileList()){
            for(Tile t : tile) tileDistanceList.put(t, i);
            i++;
        }
    }

    public ArrayList<Set<Tile>> getNearTileList() {
        return nearTileList;
    }

    public Set<Location> getLocations(){
        return this.locationSet;
    }

    public Location getCenter(){
        return center;
    }
    public Block getDropsBlock(){
        return dropsBlock;
    }

    public static void registerTiles(){
        final int distance = RADIUS*2-1;
        int amount = RADIUS*2+1;

       //final Location CENTER =
       Location upperF = LOCATION_0.clone();
       Location lowerF = upperF.clone();

       TILE_MAP.addTiles(0,amount,upperF);

       for(int x=1;x<=RADIUS;x++){
           amount--;

           upperF.add(distance,0,RADIUS+1);
           TILE_MAP.addTiles(x,amount,upperF);

           lowerF.add(-distance,0,RADIUS+1);
           TILE_MAP.addTiles(-x,amount,lowerF);
       }
    }

    public static void registerMasterTiles(){
        for(Team t : Team.getTeams()){
            Tile tile = t.getColor().getBaseTile();
            tile.colorAll(t);
            tile.setMasterTile();
            TILE_MAP.empty_tiles_set.remove(tile);
        }
    }



    public static class TileMapC{
        private HashMap<Integer,ArrayList<Tile>> map;
        public ArrayList<Tile> empty_tiles_set;

        TileMapC(){
            map = new HashMap<>();
            empty_tiles_set = new ArrayList<>();
        }

        public void addTile(int x_index,Tile tile){
            if(map.get(x_index)==null) {
                map.put(x_index,new ArrayList<Tile>());
            }
            map.get(x_index).add(tile);
            TILE_SET.add(tile);
        }

        public void addTiles(int x_index,int amount,Location first){
            for (int i=0;i<amount;i++){
                addTile(x_index,new Tile(first.clone().add(0,0,i*(RADIUS*2+2)), x_index, i));
            }
        }

        public Tile getTile(int x_index,int z_index){
            if(isValid(x_index,z_index)) return map.get(x_index).get(z_index);
            else return null;
        }

        public Tile getRandomEmptyTile(){
            if(empty_tiles_set.size() == 0) return null;
            Random rn = new Random();
            int i = rn.nextInt(empty_tiles_set.size());
            return empty_tiles_set.get(i);
        }
    }

    public class TileRunnable extends BukkitRunnable {

        private int tick = 0;



        private boolean flag_colored = false;

        public TileRunnable(){

        }

        public void run(){

            if(isMasterTile){
                bar.setTitle(own.getColor().getKoreanName() + "팀§f의 본진 [점령 불가]");
                bar.setProgress(1.0F);
                return;
            }

            tick++;
            if(PlayerList.isEmpty()) return;

            HashMap<Team, Integer> dom = new HashMap<>();
            ArrayList<Team> teams = new ArrayList<>();
            for(Team t : PlayerList.keySet()) {
                if(t == null) continue;

                boolean check = false;
                for(Tile nt : getNearTileList().get(1)){
                    if(nt.getOwnTeam() != null && nt.getOwnTeam() == t){
                        check = true;
                        break;
                    }
                }
                if(!check)
                    continue;

                List<Player> in = PlayerList.get(t);
                if(in.size() > 0) {
                    dom.put(t, in.size());
                    teams.add(t);
                }
                for (Player ap : in) {
                    bar.addPlayer(ap);
                }
            }

            if(dom.size() == 0){
                bar.setTitle(own != null ? own.getColor().getKoreanName() + " 영토" : "§7빈 땅");

                if(own != null && !flag_colored){
                    colorAll(own, false);
                    flag_colored = true;
                }
            } else if(dom.size() == 1){
                Team t = teams.get(0);
                if(!flag_colored){
                    colorAll(t, true);
                    flag_colored = true;
                }
                processOccupation(t, dom.get(t));
                bar.setTitle(t.getColor().getKoreanName() + " 점령 중...");
                bar.setProgress(progress.get(t.getColor()));
                bar.setColor(t.getColor().getBarColor());
            } else {
                if(flag_colored){
                    colorTeams(teams.toArray(new Team[0]));
                    flag_colored = false;
                }

                int temp = (tick / 10) % dom.size();
                int temp2 = temp + 1;
                if(temp2 >= dom.size())
                    temp2 = 0;
                bar.setColor(teams.get(temp).getColor().getBarColor());
                bar.setTitle(teams.get(temp).getColor().getChatColor() + ">> 대치" + teams.get(temp2).getColor().getChatColor() + " 중 <<");
            }


            /*
            if((dom.size() == 1 && dom.get(0) == own) || (dom.size() == 0 && own != null)){
                bar.setTitle(own.getColor().getKoreanName() + "팀의 영토");
                bar.setColor(own.getColor().getBarColor());
                bar.setProgress(1.0F);
                if(state_tick > 0) state_tick--;

                if(state_tick == 0){
                    colorAll(own);
                } else {
                    if(!flag_colored){
                        colorAll(own, true);
                        flag_colored = true;
                    }
                }


            } else if(dom.size() > 0){



                int temp = (tick / 10) % dom.size();
                int temp2 = temp + 1;
                if(temp2 >= dom.size())
                    temp2 = 0;

                bar.setColor(same ? dom.get(temp).getColor().getBarColor()
                        : dom.get(0).getColor().getBarColor());
                bar.setTitle(same ? dom.get(temp).getColor().getChatColor() + ">> 대치" + dom.get(temp2).getColor().getChatColor() + " 중 <<" : dom.size() > 0 ? dom.get(0).getColor().getKoreanName() + "팀 §f점령 중..." : "§f빈 땅입니다.");
                if(same) {
                    colorTeams(dom.toArray(new Team[0]));
                    flag_colored = false;
                }

            }

            float progress = (float) state_tick / (float) NEED_DOMINATE_TICK;
            if(own != null){
                progress = 1.0F - progress;
            }
            bar.setProgress(progress);

            if(dom.size() == 1)
                processOccupation(dom.get(0));
            */
        }

    }
}
