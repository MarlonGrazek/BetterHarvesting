//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.datafile.DataFile;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class EVNwaterCrops implements Listener {
    private final Main plugin;

    public EVNwaterCrops(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        if (e.getEntity() instanceof ThrownPotion) {
            ThrownPotion potion = (ThrownPotion)e.getEntity();
            Location location = potion.getLocation().getBlock().getLocation();
            DataFile settings = this.plugin.getDataFile("settings");
            if (!settings.getBoolean("watering.enabled", true)) {
                return;
            }

            for(double x = location.getX() - 1.0D; x < location.getX() + 2.0D; ++x) {
                for(double z = location.getZ() - 1.0D; z < location.getZ() + 2.0D; ++z) {
                    Random random = new Random();
                    int randomInt = random.nextInt(100);
                    Location cropLocation = new Location(location.getWorld(), x, location.getY(), z);
                    Location blockLocation = new Location(location.getWorld(), x, location.getY() - 1.0D, z);
                    switch(cropLocation.getBlock().getType()) {
                        case POTATOES:
                        case BEETROOTS:
                        case CARROTS:
                        case WHEAT:
                        case NETHER_WART:
                            Ageable crop = (Ageable)cropLocation.getBlock().getBlockData();
                            if (potion.getEffects().isEmpty()) {
                                if (randomInt < 40 && crop.getAge() != crop.getMaximumAge()) {
                                    crop.setAge(crop.getAge() + 1);
                                    cropLocation.getBlock().setBlockData(crop);
                                }
                            } else if (randomInt < 5) {
                                cropLocation.getBlock().setType(Material.AIR);
                            } else if (crop.getAge() > 1) {
                                crop.setAge(crop.getAge() - 1);
                                cropLocation.getBlock().setBlockData(crop);
                            } else {
                                cropLocation.getBlock().setType(Material.AIR);
                            }
                            break;
                        case POPPY:
                        case DANDELION:
                            if (potion.getEffects().isEmpty()) {
                                break;
                            }

                            if (blockLocation.getBlock().getType() == Material.MYCELIUM) {
                                if (randomInt < 10) {
                                    cropLocation.getBlock().setType(Material.BROWN_MUSHROOM);
                                } else if (randomInt >= 10 && randomInt < 20) {
                                    cropLocation.getBlock().setType(Material.RED_MUSHROOM);
                                } else if (randomInt >= 20 && randomInt < 40) {
                                    cropLocation.getBlock().setType(Material.AIR);
                                }
                            } else if (randomInt < 20) {
                                cropLocation.getBlock().setType(Material.AIR);
                            }

                            if (randomInt == 69) {
                                cropLocation.getBlock().setType(Material.WITHER_ROSE);
                            }
                            break;
                        case GRASS:
                            if (potion.getEffects().isEmpty() && randomInt < 20) {
                                cropLocation.getBlock().setType(Material.TALL_GRASS);
                            }
                            break;
                        case AZALEA:
                            if (!potion.getEffects().isEmpty()) {
                                if (randomInt < 15) {
                                    cropLocation.getBlock().setType(Material.DEAD_BUSH);
                                }
                            } else if (randomInt < 15) {
                                cropLocation.getBlock().setType(Material.FLOWERING_AZALEA);
                            }
                            break;
                        case FLOWERING_AZALEA:
                        case ACACIA_SAPLING:
                        case BIRCH_SAPLING:
                        case DARK_OAK_SAPLING:
                        case JUNGLE_SAPLING:
                        case OAK_SAPLING:
                        case SPRUCE_SAPLING:
                            if (!potion.getEffects().isEmpty() && randomInt < 15) {
                                cropLocation.getBlock().setType(Material.DEAD_BUSH);
                            }
                    }

                    if (blockLocation.getBlock().getType() == Material.FARMLAND && potion.getEffects().isEmpty() && randomInt < 15) {
                        Farmland farmland = (Farmland)blockLocation.getBlock().getBlockData();
                        farmland.setMoisture(7);
                        blockLocation.getBlock().setBlockData(farmland);
                    }
                }
            }
        }

    }
}
