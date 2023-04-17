//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.datafile.DataFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class EVNprepareCrafting implements Listener {
    private final Main plugin;

    public EVNprepareCrafting(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent e) {
        Recipe recipe = e.getRecipe();
        if (recipe != null) {
            DataFile config = this.plugin.getDataFile("settings");
            String result = recipe.getResult().getType().name().toLowerCase();
            if (config.contains("crafting.recipes." + result)) {
                if (config.getBoolean("crafting.enabled", true) && config.getBoolean("crafting.recipes." + result, false)) {
                    List<String> permissions = new ArrayList(config.getStringList("crafting.permissions"));
                    boolean hasPermission = false;
                    if (!permissions.isEmpty()) {
                        Iterator var7 = permissions.iterator();

                        label39:
                        while(true) {
                            while(true) {
                                if (!var7.hasNext()) {
                                    break label39;
                                }

                                String permission = (String)var7.next();
                                Iterator var9 = e.getViewers().iterator();

                                while(var9.hasNext()) {
                                    HumanEntity viewer = (HumanEntity)var9.next();
                                    if (viewer.hasPermission(permission)) {
                                        hasPermission = true;
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        hasPermission = true;
                    }

                    if (!hasPermission) {
                        e.getInventory().setResult(new ItemStack(Material.AIR));
                    }

                } else {
                    e.getInventory().setResult(new ItemStack(Material.AIR));
                }
            }
        }
    }
}
