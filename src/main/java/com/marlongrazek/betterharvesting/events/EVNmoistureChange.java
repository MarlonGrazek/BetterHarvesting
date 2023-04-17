package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.datafile.DataFile;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.MoistureChangeEvent;

public class EVNmoistureChange implements Listener {

    private final Main plugin;

    public EVNmoistureChange(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMoistureChange(MoistureChangeEvent e) {
        DataFile settings = this.plugin.getDataFile("settings");
        boolean requiresWater = settings.getBoolean("farmland_requires_water", true);
        if (requiresWater) return;
        e.setCancelled(true);
        Farmland farmland = (Farmland)e.getBlock().getBlockData();
        farmland.setMoisture(farmland.getMaximumMoisture());
        e.getBlock().setBlockData(farmland);
    }
}
