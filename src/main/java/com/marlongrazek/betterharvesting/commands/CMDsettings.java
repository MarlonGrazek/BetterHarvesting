package com.marlongrazek.betterharvesting.commands;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.betterharvesting.utils.CommandInfo;
import com.marlongrazek.betterharvesting.utils.GUI;
import com.marlongrazek.betterharvesting.utils.PluginCommand;
import com.marlongrazek.ui.History;
import org.bukkit.entity.Player;

@CommandInfo(name = "bhsettings", requiresPlayer = true, permission = "betterharvesting.settings")
public class CMDsettings extends PluginCommand {

    @Override
    public void execute(Player player, String[] args) {

        if(args.length == 0) {

            History history = Main.getHistory(player);
            history.clear();
            history.addPage(null);

            GUI gui = new GUI(player);
            gui.open(gui.settings(""));
        }
    }
}
