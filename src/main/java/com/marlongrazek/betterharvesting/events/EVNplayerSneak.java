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

import java.util.*;

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

                            List<Block> megaTreeSaplings = fourSaplingLocations(cropLocation.getBlock());
                            boolean megaTree = megaTreeSaplings != null;

                            if (megaTree) {

                                for (Block block : megaTreeSaplings) {

                                    Sapling sapling = (Sapling) block.getBlockData();

                                    if (sapling.getStage() != sapling.getMaximumStage()) {
                                        sapling.setStage(sapling.getStage() + 1);
                                        block.setBlockData(sapling);
                                        player.spawnParticle(Particle.VILLAGER_HAPPY, block.getX() + 0.5,
                                                block.getY(), block.getZ() + 0.5, 5, 0.25, 0, 0.25);
                                    } else {
                                        generateTree(megaTreeSaplings.get(0).getLocation(), sapling, megaTree);
                                        break;
                                    }
                                }

                            } else {

                                Sapling sapling = (Sapling) cropLocation.getBlock().getBlockData();

                                if (sapling.getStage() != sapling.getMaximumStage()) {
                                    sapling.setStage(sapling.getStage() + 1);
                                    cropLocation.getBlock().setBlockData(sapling);
                                    player.spawnParticle(Particle.VILLAGER_HAPPY, cropLocation.getX() + 0.5,
                                            cropLocation.getY(), cropLocation.getZ() + 0.5, 5, 0.25, 0, 0.25);
                                } else generateTree(cropLocation, sapling, megaTree);
                            }
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

            for (double x = location.getX(); x < location.getX() + 2; x++)
                for (double z = location.getZ(); z < location.getZ() + 2; z++)
                    new Location(location.getWorld(), x, location.getY(), z).getBlock().setType(Material.AIR);

            switch (sapling.getMaterial()) {
                case JUNGLE_SAPLING -> type = TreeType.JUNGLE;
                case SPRUCE_SAPLING -> type = TreeType.MEGA_REDWOOD;
            }
        }

        // regular tree
        else {

            location.getBlock().setType(Material.AIR);

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

        boolean megaTree = true;

        double[] min = new double[2];
        double[] max = new double[2];

        // northeast
        if (blockNorth.getType() == block.getType() && blockEast.getType() == block.getType() &&
                block.getRelative(BlockFace.NORTH_EAST).getType() == block.getType()) {
            min[0] = blockNorth.getX();
            min[1] = blockNorth.getZ();
            max[0] = blockEast.getX();
            max[1] = blockEast.getZ();
        }

        // northwest
        else if (blockNorth.getType() == block.getType() && blockWest.getType() == block.getType() &&
                block.getRelative(BlockFace.NORTH_WEST).getType() == block.getType()) {
            min[0] = block.getRelative(BlockFace.NORTH_WEST).getX();
            min[1] = block.getRelative(BlockFace.NORTH_WEST).getY();
            max[0] = block.getX();
            max[1] = block.getZ();
        }

        // southeast
        else if (blockSouth.getType() == block.getType() && blockEast.getType() == block.getType() &&
                block.getRelative(BlockFace.SOUTH_EAST).getType() == block.getType()) {
            min[0] = block.getX();
            min[1] = block.getZ();
            max[0] = block.getRelative(BlockFace.SOUTH_EAST).getX();
            max[1] = block.getRelative(BlockFace.SOUTH_EAST).getZ();
        }

        // southwest
        else if (blockSouth.getType() == block.getType() && blockWest.getType() == block.getType() &&
                block.getRelative(BlockFace.SOUTH_WEST).getType() == block.getType()) {
            min[0] = blockWest.getX();
            min[1] = blockWest.getZ();
            max[0] = blockSouth.getX();
            max[1] = blockSouth.getZ();
        } else megaTree = false;

        if (megaTree) {
            for (double x = min[0]; x < max[0] + 1; x++) {
                for (double z = min[1]; z < max[1] + 1; z++) {
                    saplingLocations.add(new Location(block.getWorld(), x, block.getY(), z).getBlock());
                }
            }
        }

        if (saplingLocations.isEmpty()) return null;
        return saplingLocations;
    }
}
