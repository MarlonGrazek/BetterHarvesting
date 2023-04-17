//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.betterharvesting.main.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EVNsetupPlayer implements Listener {
    private final Main plugin;

    public EVNsetupPlayer(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        this.plugin.setUp(e.getPlayer());
    }
}
