package com.marlongrazek.betterharvesting.events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.HashMap;
import java.util.Random;

public class EVNwaterCrops implements Listener {

    @EventHandler
    public void onHit(ProjectileHitEvent e) {

        if (e.getEntity() instanceof ThrownPotion) {

            ThrownPotion potion = (ThrownPotion) e.getEntity();
            Location location = potion.getLocation().getBlock().getLocation();

            HashMap<Material, Material[]> replacingBlocks = new HashMap<>();
            replacingBlocks.put(Material.DEAD_BRAIN_CORAL_BLOCK, new Material[]{Material.BRAIN_CORAL_BLOCK, null});
            replacingBlocks.put(Material.DEAD_BUBBLE_CORAL_BLOCK, new Material[]{Material.BUBBLE_CORAL_BLOCK, null});
            replacingBlocks.put(Material.DEAD_FIRE_CORAL_BLOCK, new Material[]{Material.FIRE_CORAL_BLOCK, null});
            replacingBlocks.put(Material.DEAD_HORN_CORAL_BLOCK, new Material[]{Material.HORN_CORAL_BLOCK, null});
            replacingBlocks.put(Material.DEAD_TUBE_CORAL_BLOCK, new Material[]{Material.TUBE_CORAL_BLOCK, null});
            replacingBlocks.put(Material.DIRT, new Material[]{Material.GRASS_BLOCK, Material.MYCELIUM});

            for (double x = location.getX() - 1; x < location.getX() + 2; x++) {
                for (double z = location.getZ() - 1; z < location.getZ() + 2; z++) {

                    Random random = new Random();
                    int randomInt = random.nextInt(100);
                    Location cropLocation = new Location(location.getWorld(), x, location.getY(), z);
                    Location blockLocation = new Location(location.getWorld(), x, location.getY() - 1, z);

                    Material blockType = blockLocation.getBlock().getType();

                    /*if (replacingBlocks.containsKey(blockType)) {
                        if (randomInt < 20) {
                            if (potion.getEffects().isEmpty() && replacingBlocks.get(blockType)[0] != null)
                                blockLocation.getBlock().setType(replacingBlocks.get(blockType)[0]);
                            else if (!potion.getEffects().isEmpty() && replacingBlocks.get(blockType)[1] != null)
                                blockLocation.getBlock().setType(replacingBlocks.get(blockType)[1]);
                        }
                    }*/

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

                    // dirt to grass
                    /*if (blockLocation.getBlock().getType() == Material.DIRT) {
                        if (potion.getEffects().isEmpty()) {
                            if (randomInt < 15) blockLocation.getBlock().setType(Material.GRASS_BLOCK);
                        } else {
                            if (randomInt < 15) blockLocation.getBlock().setType(Material.MYCELIUM);
                        }
                    }*/
                    if (blockLocation.getBlock().getType() == Material.FARMLAND) {
                        if (potion.getEffects().isEmpty()) {
                            if (randomInt < 15) {
                                Farmland farmland = (Farmland) blockLocation.getBlock().getBlockData();
                                farmland.setMoisture(7);
                                blockLocation.getBlock().setBlockData(farmland);
                            }
                        }
                    }

                    // grass on grass_block
                    /*else if (blockLocation.getBlock().getType() == Material.GRASS_BLOCK) {
                        if (potion.getEffects().isEmpty()) {
                            if (cropLocation.getBlock().getType() == Material.AIR) {
                                if (randomInt < 5) cropLocation.getBlock().setType(Material.GRASS);
                                else if (randomInt >= 5 && randomInt < 8)
                                    cropLocation.getBlock().setType(Material.TALL_GRASS);
                                else if (randomInt == 9)
                                    cropLocation.getBlock().setType(Material.POPPY);
                                else if (randomInt == 10)
                                    cropLocation.getBlock().setType(Material.DANDELION);
                            } else if (cropLocation.getBlock().getType() == Material.TALL_GRASS) {
                                if (randomInt < 5) cropLocation.getBlock().setType(Material.TALL_GRASS);
                            }
                        } else {
                            if (cropLocation.getBlock().getType() == Material.TALL_GRASS) {
                                if (randomInt < 10) cropLocation.getBlock().setType(Material.GRASS);
                            } else if (cropLocation.getBlock().getType() == Material.GRASS) {
                                if (randomInt < 10) cropLocation.getBlock().setType(Material.AIR);
                            } else if (cropLocation.getBlock().getType() == Material.POPPY ||
                                    cropLocation.getBlock().getType() == Material.DANDELION) {
                                if (randomInt < 1) cropLocation.getBlock().setType(Material.WITHER_ROSE);
                                else if (randomInt >= 10 && randomInt < 30)
                                    cropLocation.getBlock().setType(Material.AIR);
                            } else if (cropLocation.getBlock().getType() == Material.AIR) {
                                if (randomInt < 15) blockLocation.getBlock().setType(Material.DIRT);
                                else if (randomInt >= 15 && randomInt < 30)
                                    blockLocation.getBlock().setType(Material.MYCELIUM);
                            }
                        }
                    }*/
                }
            }
        }
    }
}
