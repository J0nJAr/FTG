package jonjar.ftg;

import jonjar.ftg.manager.TabCompleteManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class FTG extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getCommand("FTG").setExecutor(cm);
        getCommand("FTG").tabComplete(new TabCompleteManager());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
