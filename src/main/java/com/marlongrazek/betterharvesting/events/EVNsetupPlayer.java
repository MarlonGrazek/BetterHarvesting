package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.betterharvesting.main.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EVNsetupPlayer implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Main.getPlugin().setUp(e.getPlayer());
    }
}
