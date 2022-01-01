package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.betterharvesting.main.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.ArrayList;
import java.util.List;
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
                            } else {

                                boolean megaTree = fourSaplingLocations(cropLocation.getBlock()) != null;
                                if (megaTree)
                                    fourSaplingLocations(cropLocation.getBlock()).forEach(block -> block.setType(Material.AIR));
                                cropLocation.getBlock().setType(Material.AIR);
                                generateTree(cropLocation, sapling, megaTree);
                                player.sendMessage("Tree Growing soon");
                            }

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

    public void generateTree(Location location, Sapling sapling, boolean megaTree) {

        TreeType type = TreeType.TREE;
        Random random = new Random();
        boolean tallTree = random.nextInt(10) == 1;

        // 4 sapling tree
        if (megaTree) {
            switch (sapling.getMaterial()) {
                case JUNGLE_SAPLING -> type = TreeType.JUNGLE;
                case SPRUCE_SAPLING -> type = TreeType.MEGA_REDWOOD;
            }
        }

        // regular tree
        else {
            switch (sapling.getMaterial()) {
                case ACACIA_SAPLING -> type = TreeType.ACACIA;
                case AZALEA -> type = TreeType.AZALEA;
                case BIRCH_SAPLING -> {
                    if (tallTree) type = TreeType.TALL_BIRCH;
                    else type = TreeType.BIRCH;
                }
                case DARK_OAK_SAPLING -> type = TreeType.DARK_OAK;
                case JUNGLE_SAPLING -> type = TreeType.SMALL_JUNGLE;
                case OAK_SAPLING -> {
                    if (tallTree) type = TreeType.BIG_TREE;
                    else type = TreeType.TREE;
                }
                case SPRUCE_SAPLING -> {
                    if (tallTree) type = TreeType.TALL_REDWOOD;
                    else type = TreeType.REDWOOD;
                }
            }
        }

        location.getWorld().generateTree(location, type);
    }

    public List<Block> fourSaplingLocations(Block block) {

        List<Block> saplingLocations = new ArrayList<>();

        Block blockNorth = block.getRelative(BlockFace.NORTH);
        Block blockSouth = block.getRelative(BlockFace.SOUTH);
        Block blockEast = block.getRelative(BlockFace.EAST);
        Block blockWest = block.getRelative(BlockFace.WEST);

        // northeast
        if (blockNorth.getType() == block.getType() && blockEast.getType() == block.getType() &&
                block.getRelative(BlockFace.NORTH_EAST).getType() == block.getType()) {
            saplingLocations.add(blockNorth);
            saplingLocations.add(blockEast);
            saplingLocations.add(block.getRelative(BlockFace.NORTH_EAST));
        }

        // northwest
        else if(blockNorth.getType() == block.getType() && blockWest.getType() == block.getType() &&
                block.getRelative(BlockFace.NORTH_WEST).getType() == block.getType()) {
            saplingLocations.add(blockNorth);
            saplingLocations.add(blockWest);
            saplingLocations.add(block.getRelative(BlockFace.NORTH_WEST));
        }

        // southeast
        else if(blockSouth.getType() == block.getType() && blockEast.getType() == block.getType() &&
                block.getRelative(BlockFace.SOUTH_EAST).getType() == block.getType()) {
            saplingLocations.add(blockSouth);
            saplingLocations.add(blockEast);
            saplingLocations.add(block.getRelative(BlockFace.SOUTH_EAST));
        }

        // southwest
        else if(blockSouth.getType() == block.getType() && blockWest.getType() == block.getType() &&
                block.getRelative(BlockFace.SOUTH_WEST).getType() == block.getType()) {
            saplingLocations.add(blockSouth);
            saplingLocations.add(blockWest);
            saplingLocations.add(block.getRelative(BlockFace.SOUTH_WEST));
        }

        if(saplingLocations.isEmpty()) return null;
        return saplingLocations;
    }
}
