package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.betterharvesting.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.Arrays;
import java.util.Random;

public class EVNplayerSneak implements Listener {

    int taskid;

    @EventHandler
    public void onGrow(PlayerToggleSneakEvent e) {

        if (e.isSneaking()) {

            taskid = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), () -> {

                Player player = e.getPlayer();
                Location playerLocation = player.getLocation().getBlock().getLocation();

                for (double x = playerLocation.getX() - 3; x < playerLocation.getX() + 4; x++) {
                    for (double z = playerLocation.getZ() - 3; z < playerLocation.getZ() + 4; z++) {

                        Random random = new Random();
                        int randomInt = random.nextInt(100);

                        Location cropLocation = new Location(playerLocation.getWorld(), x, playerLocation.getY(), z);

                        // crops
                        if (cropLocation.getBlock().getBlockData() instanceof Ageable) {

                            Ageable crop = (Ageable) cropLocation.getBlock().getBlockData();

                            if (randomInt > 10) continue;
                            if (crop.getAge() == crop.getMaximumAge()) continue;

                            crop.setAge(crop.getAge() + 1);
                            cropLocation.getBlock().setBlockData(crop);

                            player.spawnParticle(Particle.VILLAGER_HAPPY, cropLocation.getX() + 0.5,
                                    cropLocation.getY(), cropLocation.getZ() + 0.5, 5, 0.25, 0, 0.25);
                        }

                        // saplings
                        else if (cropLocation.getBlock().getBlockData() instanceof Sapling) {

                            if (randomInt > 10) continue;

                            Sapling sapling = (Sapling) cropLocation.getBlock().getBlockData();

                            if (sapling.getStage() != sapling.getMaximumStage()) {
                                sapling.setStage(sapling.getStage() + 1);
                                cropLocation.getBlock().setBlockData(sapling);
                            } else player.sendMessage("Tree Growing soon");
                            player.spawnParticle(Particle.VILLAGER_HAPPY, cropLocation.getX() + 0.5,
                                    cropLocation.getY(), cropLocation.getZ() + 0.5, 5, 0.25, 0, 0.25);
                        }
                    }
                }
            }, 0, 5);
        } else {
            Bukkit.getScheduler().cancelTask(taskid);
        }
    }
}
