//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.marlongrazek.betterharvesting.commands;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.betterharvesting.utils.CommandInfo;
import com.marlongrazek.betterharvesting.utils.GUI;
import com.marlongrazek.betterharvesting.utils.PluginCommand;
import com.marlongrazek.ui.History;
import com.marlongrazek.ui.UI.Page;
import org.bukkit.entity.Player;

@CommandInfo(
        name = "bhsettings",
        requiresPlayer = true,
        permission = "betterharvesting.settings"
)
public class CMDsettings extends PluginCommand {
    private final Main plugin;

    public CMDsettings(Main plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            History history = this.plugin.getHistory(player);
            history.clear();
            history.addPage(null);
            GUI gui = new GUI(player, this.plugin);
            gui.open(gui.settings());
        }

    }
}
