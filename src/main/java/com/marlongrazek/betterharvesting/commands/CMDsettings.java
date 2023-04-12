package com.marlongrazek.betterharvesting.commands;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.betterharvesting.utils.CommandInfo;
import com.marlongrazek.betterharvesting.utils.GUI;
import com.marlongrazek.betterharvesting.utils.PluginCommand;
import com.marlongrazek.ui.History;
import org.bukkit.entity.Player;

@CommandInfo(name = "bhsettings", requiresPlayer = true, permission = "betterharvesting.settings")
public class CMDsettings extends PluginCommand {

    private final Main plugin;

    public CMDsettings(Main plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {

        if (args.length == 0) {

            History history = plugin.getHistory(player);
            history.clear();
            history.addPage(null);

            GUI gui = new GUI(player, plugin);
            gui.open(gui.settings(null));
        }
    }

    /*@Override
    public List<String> onTabComplete(CommandSender s, Command c, String lbl, String[] args) {

        List<String> items = new ArrayList<>();
        DataFile settings = plugin.getDataFile("settings");

        if (args.length == 1) items.addAll(settings.getConfigurationSection("", false));

        return items;
    }*/
}
