package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.datafile.DataFile;
import org.bukkit.Material;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class EVNblockPlace implements Listener {

    private final Main plugin;

    public EVNblockPlace(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (e.getBlock().getType() != Material.FARMLAND)
            return;
        DataFile settings = this.plugin.getDataFile("settings");
        boolean requiresWater = settings.getBoolean("farmland_requires_water", true);
        if (requiresWater) return;
        Farmland farmland = (Farmland)e.getBlock().getBlockData();
        farmland.setMoisture(farmland.getMaximumMoisture());
        e.getBlock().setBlockData(farmland);
    }
}
