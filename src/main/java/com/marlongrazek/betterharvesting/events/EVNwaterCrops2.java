//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.betterharvesting.WateringEvent;
import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.datafile.DataFile;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EVNwaterCrops2 implements Listener {
    private final Main plugin;

    public EVNwaterCrops2(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWatering(WateringEvent e) {
        DataFile settings = this.plugin.getDataFile("settings");
        if (settings.getBoolean("watering.enabled", true)) {
            Iterator var3 = e.getAffectedBlocks().iterator();

            while(true) {
                while(true) {
                    while(var3.hasNext()) {
                        Block block = (Block)var3.next();
                        Block plantBlock = block.getRelative(BlockFace.UP, 1);
                        Random random = new Random();
                        int randomInt = random.nextInt(100);
                        if (e.isPoisonous()) {
                            switch(block.getType()) {
                                case DIRT:
                                    block.setType(Material.MYCELIUM);
                                    break;
                                case GRASS_BLOCK:
                                    if (randomInt < 20) {
                                        block.setType(Material.DIRT);
                                        plantBlock.setType(Material.AIR);
                                    } else if (randomInt >= 20 && randomInt < 30) {
                                        if (Arrays.asList(Material.POPPY, Material.DANDELION).contains(plantBlock.getType())) {
                                            if (randomInt >= 24 && randomInt < 27) {
                                                plantBlock.setType(Material.RED_MUSHROOM);
                                            } else if (randomInt >= 27 && randomInt < 30) {
                                                plantBlock.setType(Material.BROWN_MUSHROOM);
                                            } else {
                                                plantBlock.setType(Material.AIR);
                                            }
                                        } else {
                                            plantBlock.setType(Material.AIR);
                                        }

                                        block.setType(Material.MYCELIUM);
                                    }

                                    if (!plantBlock.isEmpty()) {
                                        if (randomInt == 69) {
                                            switch(plantBlock.getType()) {
                                                case POPPY:
                                                case DANDELION:
                                                    plantBlock.setType(Material.WITHER_ROSE);
                                            }
                                        }

                                        if (randomInt < 10) {
                                            switch(plantBlock.getType()) {
                                                case POPPY:
                                                case DANDELION:
                                                case GRASS:
                                                    plantBlock.setType(Material.AIR);
                                                    break;
                                                case TALL_GRASS:
                                                    plantBlock.setType(Material.GRASS);
                                            }
                                        }
                                    }
                            }
                        } else {
                            switch(block.getType()) {
                                case DIRT:
                                    block.setType(Material.GRASS_BLOCK);
                                    break;
                                case GRASS_BLOCK:
                                    if (plantBlock.getType() == Material.AIR) {
                                        if (randomInt < 10) {
                                            plantBlock.setType(Material.GRASS);
                                        } else if (randomInt >= 10 && randomInt < 16) {
                                            plantBlock.setType(Material.TALL_GRASS);
                                        } else if (randomInt >= 16 && randomInt < 18) {
                                            plantBlock.setType(Material.POPPY);
                                        } else if (randomInt >= 18 && randomInt < 20) {
                                            plantBlock.setType(Material.DANDELION);
                                        }
                                    } else if (plantBlock.getType() == Material.GRASS && randomInt < 10) {
                                        plantBlock.setType(Material.TALL_GRASS);
                                    }
                            }
                        }
                    }

                    return;
                }
            }
        }
    }
}
