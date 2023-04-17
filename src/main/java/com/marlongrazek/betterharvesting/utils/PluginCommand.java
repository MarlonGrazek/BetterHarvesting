//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.marlongrazek.betterharvesting.utils;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.datafile.DataFile;
import java.util.Objects;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PluginCommand implements CommandExecutor {
    private final CommandInfo commandInfo;
    private final Main plugin;

    public PluginCommand(Main plugin) {
        this.plugin = plugin;
        this.commandInfo = (CommandInfo)this.getClass().getDeclaredAnnotation(CommandInfo.class);
        Objects.requireNonNull(this.commandInfo, "Plugins must have CommandInfo annotations");
    }

    public CommandInfo getCommandInfo() {
        return this.commandInfo;
    }

    public boolean onCommand(CommandSender sender, Command command, String lbl, String[] args) {
        if (!this.commandInfo.permission().isEmpty() && !sender.hasPermission(this.commandInfo.permission())) {
            DataFile config = this.plugin.getDataFile("config");
            String prefix = config.getString("prefix");
            String no_permission = config.getString("no_permission");
            sender.sendMessage(prefix + " §7| " + no_permission);
            return true;
        } else if (this.commandInfo.requiresPlayer()) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cYou must be player");
                return true;
            } else {
                this.execute((Player)sender, args);
                return true;
            }
        } else {
            this.execute(sender, args);
            return true;
        }
    }

    public void execute(Player player, String[] args) {
    }

    public void execute(CommandSender sender, String[] args) {
    }
}
