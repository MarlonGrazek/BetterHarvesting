package com.marlongrazek.betterharvesting.utils;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.datafile.DataFile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class PluginCommand implements CommandExecutor {

    private final CommandInfo commandInfo;
    private final Main plugin;

    public PluginCommand(Main plugin) {
        this.plugin = plugin;
        commandInfo = getClass().getDeclaredAnnotation(CommandInfo.class);
        Objects.requireNonNull(commandInfo, "Plugins must have CommandInfo annotations");
    }

    public CommandInfo getCommandInfo() {
        return commandInfo;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String lbl, String[] args) {

        if(!commandInfo.permission().isEmpty() && !sender.hasPermission(commandInfo.permission())) {
            DataFile config = plugin.getDataFile("config");
            String prefix = config.getString("prefix");
            String no_permission = config.getString("no_permission");
            sender.sendMessage(prefix + " §7| " + no_permission);
            return true;
        }

        if(commandInfo.requiresPlayer()) {
            if(!(sender instanceof Player)) {
                sender.sendMessage("§cYou must be player");
                return true;
            }

            execute((Player) sender, args);
            return true;
        }

        execute(sender, args);
        return true;
    }

    public void execute(Player player, String[] args) {
    }

    public void execute(CommandSender sender, String[] args) {
    }
}
