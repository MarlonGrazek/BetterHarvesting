//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.marlongrazek.betterharvesting.commands;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.betterharvesting.utils.CommandInfo;
import com.marlongrazek.betterharvesting.utils.PluginCommand;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandInfo(
        name = "betterharvesting",
        permission = "betterharvesting.info",
        requiresPlayer = true
)
public class CMDbetterharvesting extends PluginCommand {
    private final Main plugin;

    public CMDbetterharvesting(Main plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            String name = this.plugin.getDescription().getName();
            String version = this.plugin.getDescription().getVersion();
            List<String> authors = this.plugin.getDescription().getAuthors();
            String description = this.plugin.getDescription().getDescription();
            Bukkit.broadcastMessage(" ");
            Bukkit.broadcastMessage("§7--- §6" + name + " §7---");
            Bukkit.broadcastMessage("§fVersion: §e" + version);
            Bukkit.broadcastMessage("§fAuthors: §e" + String.join("§7, §e", authors));
            Bukkit.broadcastMessage("§7-----");
            Bukkit.broadcastMessage("§f" + description);
            Bukkit.broadcastMessage("§7-----");
            Bukkit.broadcastMessage("§fUse §e/betterharvesting help §fto view the commands");
            Bukkit.broadcastMessage(" ");
        } else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            Map<String, Map<String, Object>> commands = this.plugin.getDescription().getCommands();
            commands.keySet().forEach((commandName) -> {
                Map<String, Object> command = (Map)commands.get(commandName);
                String description = (String)command.get("description");
                Bukkit.broadcastMessage("§e" + commandName + ": §f" + description);
            });
        }

    }
}
