package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.datafile.DataFile;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.Random;

public class EVNwaterCrops implements Listener {

    private final Main plugin;

    public EVNwaterCrops(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHit(ProjectileHitEvent e) {

        if (e.getEntity() instanceof ThrownPotion) {

            ThrownPotion potion = (ThrownPotion) e.getEntity();
            Location location = potion.getLocation().getBlock().getLocation();

            DataFile settings = plugin.getDataFile("settings");
            if(!settings.getBoolean("watering.enabled", true)) return;

            for (double x = location.getX() - 1; x < location.getX() + 2; x++) {
                for (double z = location.getZ() - 1; z < location.getZ() + 2; z++) {

                    Random random = new Random();
                    int randomInt = random.nextInt(100);
                    Location cropLocation = new Location(location.getWorld(), x, location.getY(), z);
                    Location blockLocation = new Location(location.getWorld(), x, location.getY() - 1, z);

                    switch (cropLocation.getBlock().getType()) {

                        // crops grow faster
                        case POTATOES, BEETROOTS, CARROTS, WHEAT, NETHER_WART -> {
                            Ageable crop = (Ageable) cropLocation.getBlock().getBlockData();

                            if (potion.getEffects().isEmpty()) {
                                if (randomInt < 40) {
                                    if (crop.getAge() != crop.getMaximumAge()) {
                                        crop.setAge(crop.getAge() + 1);
                                        cropLocation.getBlock().setBlockData(crop);
                                    }
                                }
                            } else {
                                if (randomInt < 5) cropLocation.getBlock().setType(Material.AIR);
                                else {
                                    if (crop.getAge() > 1) {
                                        crop.setAge(crop.getAge() - 1);
                                        cropLocation.getBlock().setBlockData(crop);
                                    } else cropLocation.getBlock().setType(Material.AIR);
                                }
                            }
                        }

                        // flowers to mushrooms or wither rose
                        case POPPY, DANDELION -> {
                            if (!potion.getEffects().isEmpty()) {
                                if (blockLocation.getBlock().getType() == Material.MYCELIUM) {
                                    if (randomInt < 10) cropLocation.getBlock().setType(Material.BROWN_MUSHROOM);
                                    else if (randomInt >= 10 && randomInt < 20)
                                        cropLocation.getBlock().setType(Material.RED_MUSHROOM);
                                    else if (randomInt >= 20 && randomInt < 40)
                                        cropLocation.getBlock().setType(Material.AIR);
                                } else {
                                    if (randomInt < 20) cropLocation.getBlock().setType(Material.AIR);
                                }
                                if (randomInt == 69) cropLocation.getBlock().setType(Material.WITHER_ROSE);
                            }
                        }
                        case GRASS -> {
                            if (potion.getEffects().isEmpty()) {
                                if (randomInt < 20) cropLocation.getBlock().setType(Material.TALL_GRASS);
                            }
                        }
                        case AZALEA -> {
                            if (!potion.getEffects().isEmpty()) {
                                if (randomInt < 15) cropLocation.getBlock().setType(Material.DEAD_BUSH);
                            } else {
                                if (randomInt < 15) cropLocation.getBlock().setType(Material.FLOWERING_AZALEA);
                            }
                        }
                        case FLOWERING_AZALEA, ACACIA_SAPLING, BIRCH_SAPLING, DARK_OAK_SAPLING, JUNGLE_SAPLING, OAK_SAPLING, SPRUCE_SAPLING -> {
                            if (!potion.getEffects().isEmpty()) {
                                if (randomInt < 15) cropLocation.getBlock().setType(Material.DEAD_BUSH);
                            }
                        }
                    }

                    if (blockLocation.getBlock().getType() == Material.FARMLAND) {
                        if (potion.getEffects().isEmpty()) {
                            if (randomInt < 15) {
                                Farmland farmland = (Farmland) blockLocation.getBlock().getBlockData();
                                farmland.setMoisture(7);
                                blockLocation.getBlock().setBlockData(farmland);
                            }
                        }
                    }
                }
            }
        }
    }
}
