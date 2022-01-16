package com.marlongrazek.betterharvesting.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.*;

public class EVNwaterPlants implements Listener {

    @EventHandler
    public void onHit(ProjectileHitEvent e) {

        if (!(e.getEntity() instanceof ThrownPotion)) return;

        Block hitblock = e.getHitBlock();
        Block plant = hitblock.getRelative(BlockFace.UP);

        ThrownPotion potion = (ThrownPotion) e.getEntity();
        boolean isPoisonous = !potion.getEffects().isEmpty();

        Random random = new Random();
        int randomInt = random.nextInt(100) + 1;

        List<Material> flowers = List.of(Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM,
                Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP,
                Material.PINK_TULIP, Material.OXEYE_DAISY, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY);

        // water
        if (!isPoisonous) {

            if (plant.getBlockData() instanceof Ageable) {

                Ageable ageable = (Ageable) plant.getBlockData();

                if (randomInt <= 40) {
                    if (ageable.getAge() != ageable.getMaximumAge()) {
                        ageable.setAge(ageable.getAge() + 1);
                        plant.setBlockData(ageable);
                    }
                }
            }

            // azalea -> flowering azalea
            else if (plant.getType() == Material.AZALEA) if (randomInt <= 15) plant.setType(Material.FLOWERING_AZALEA);

                // grass -> tall grass
            else if (plant.getType() == Material.GRASS) if (randomInt <= 20) plant.setType(Material.TALL_GRASS);

                // fern -> large fern
            else if (plant.getType() == Material.FERN) if (randomInt <= 20) plant.setType(Material.LARGE_FERN);


            // dirt -> grass block
            if (hitblock.getType() == Material.DIRT) if (randomInt <= 20) hitblock.setType(Material.GRASS_BLOCK);

            if (hitblock.getType() == Material.GRASS_BLOCK && plant.getType() == Material.AIR) {

                if (randomInt <= 10) plant.setType(Material.GRASS);
                else if (randomInt > 10 && randomInt <= 16) plant.setType(Material.TALL_GRASS);

                for (Material flower : flowers) {
                    if (randomInt > 16 && randomInt <= 18) {
                        plant.setType(flower);
                        break;
                    }
                }
            }
        }

        // poison
        else {

            // crops
            if (plant.getBlockData() instanceof Ageable) {

                Ageable ageable = (Ageable) plant.getBlockData();

                if (randomInt <= 5 || ageable.getAge() <= 1) plant.setType(Material.AIR);
                else {
                    ageable.setAge(ageable.getAge() - 1);
                    plant.setBlockData(ageable);
                }
            }

            // saplings -> dead bush
            else if (plant.getBlockData() instanceof Sapling) if (randomInt <= 15) plant.setType(Material.DEAD_BUSH);

                // tall grass -> grass
            else if (plant.getType() == Material.TALL_GRASS) if (randomInt <= 20) plant.setType(Material.GRASS);

                // large fern -> fern
            else if (plant.getType() == Material.LARGE_FERN) if (randomInt <= 20) plant.setType(Material.FERN);

            // flower -> mushroom or wither rose
            else if(flowers.contains(plant.getType())) {
                if(randomInt <= 3) {
                    hitblock.setType(Material.MYCELIUM);
                    plant.setType(Material.RED_MUSHROOM);
                }
                else if(randomInt > 3 && randomInt <= 6) {
                    hitblock.setType(Material.MYCELIUM);
                    plant.setType(Material.BROWN_MUSHROOM);
                }
                else if(randomInt == 69) plant.setType(Material.WITHER_ROSE);
                else if(randomInt > 10 && randomInt <= 20) plant.setType(Material.AIR);
            }


            // dirt -> mycelium
            if (hitblock.getType() == Material.DIRT) {
                if (randomInt <= 20) {
                    hitblock.setType(Material.MYCELIUM);
                    plant.setType(Material.AIR);
                }
            }

            // grass block -> dirt or mycelium
            else if (hitblock.getType() == Material.GRASS_BLOCK) {
                if (randomInt <= 20) {
                    hitblock.setType(Material.DIRT);
                    plant.setType(Material.AIR);
                } else if (randomInt > 20 && randomInt <= 40) {
                    hitblock.setType(Material.MYCELIUM);
                    plant.setType(Material.AIR);
                }
            }
        }
    }
}
