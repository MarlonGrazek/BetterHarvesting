package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.datafile.DataFile;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.ArrayList;
import java.util.List;

public class EVNprepareCrafting implements Listener {

    private final Main plugin;

    public EVNprepareCrafting(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent e) {

        Recipe recipe = e.getRecipe();

        if (recipe == null) return;

        DataFile config = plugin.getDataFile("settings");
        String result = recipe.getResult().getType().name().toLowerCase();

        // no custom recipe
        if (!config.contains("crafting.recipes." + result)) return;

        // recipe disabled
        if (!config.getBoolean("crafting.enabled", true) ||
                !config.getBoolean("crafting.recipes." + result, true)) {
            e.getInventory().setResult(new ItemStack(Material.AIR));
            return;
        }

        // permissions
        List<String> permissions = new ArrayList<>(config.getStringList("crafting.permissions"));
        boolean hasPermission = false;

        if (!permissions.isEmpty()) {
            for (String permission : permissions) {
                for (HumanEntity viewer : e.getViewers()) {
                    if (viewer.hasPermission(permission)) {
                        hasPermission = true;
                        break;
                    }
                }
            }
        } else hasPermission = true;

        if (!hasPermission) e.getInventory().setResult(new ItemStack(Material.AIR));
    }
}
