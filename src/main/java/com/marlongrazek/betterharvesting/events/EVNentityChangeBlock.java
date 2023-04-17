package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.betterharvesting.main.Main;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class EVNentityChangeBlock implements Listener {

    private final Main plugin;

    public EVNentityChangeBlock(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChangeBlock(EntityChangeBlockEvent e) {

        // farmland check
        if(e.getBlock().getType() != Material.FARMLAND) return;

        // feature check
        if(plugin.getCFCSettings().getBoolean("farmland_trampeling", true)) return;

        e.setCancelled(true);
    }
}
