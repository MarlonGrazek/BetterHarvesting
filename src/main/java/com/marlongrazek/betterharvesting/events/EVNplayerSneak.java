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
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class EVNplayerSneak implements Listener {
    private final Main plugin;
    private int taskid;

    public EVNplayerSneak(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGrow(PlayerToggleSneakEvent e) {

        DataFile settings = this.plugin.getDataFile("settings");

        if(!settings.getBoolean("sneaking.enabled", false)) return;

        Player player = e.getPlayer();
        List<String> permissions = new ArrayList(settings.getStringList("sneaking.permissions"));

        if(!hasPermissionFromList(player, permissions)) return;

        if (e.isSneaking()) {
            this.taskid = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
                Location playerLocation = player.getLocation().getBlock().getLocation();
                if (settings.getBoolean("sneaking.enabled", true)) {
                    if (this.hasPermissionFromList(player, permissions)) {
                        int range = settings.getInt("sneaking.range");

                        for(double x = playerLocation.getX() - (double)range; x <= playerLocation.getX() + (double)range; ++x) {
                            for(double z = playerLocation.getZ() - (double)range; z <= playerLocation.getZ() + (double)range; ++z) {
                                Random random = new Random();
                                int randomInt = random.nextInt(100) + 1;
                                Location cropLocation = new Location(playerLocation.getWorld(), x, playerLocation.getY(), z);
                                String item = cropLocation.getBlock().getType().name().toLowerCase();
                                switch(cropLocation.getBlock().getType()) {
                                    case WHEAT:
                                        item = "wheat_seeds";
                                        break;
                                    case BEETROOTS:
                                        item = "beetroot_seeds";
                                        break;
                                    case CARROTS:
                                        item = "carrot";
                                        break;
                                    case POTATOES:
                                        item = "potato";
                                        break;
                                    case COCOA:
                                        item = "cocoa_beans";
                                        break;
                                    case MELON_STEM:
                                        item = "melon_seeds";
                                        break;
                                    case PUMPKIN_STEM:
                                        item = "pumpkin_seeds";
                                }

                                if (settings.getBoolean("sneaking.blocks." + item, false)) {
                                    int chance = settings.getInt("sneaking.chance");
                                    if (cropLocation.getBlock().getBlockData() instanceof Ageable) {
                                        Ageable crop = (Ageable)cropLocation.getBlock().getBlockData();
                                        if (randomInt <= chance && crop.getAge() != crop.getMaximumAge()) {
                                            crop.setAge(crop.getAge() + 1);
                                            cropLocation.getBlock().setBlockData(crop);
                                            player.spawnParticle(Particle.VILLAGER_HAPPY, cropLocation.getX() + 0.5D, cropLocation.getY(), cropLocation.getZ() + 0.5D, 5, 0.25D, 0.0D, 0.25D);
                                        }
                                    } else if (cropLocation.getBlock().getBlockData() instanceof Sapling && randomInt <= chance) {
                                        boolean experimental_enabled = settings.getBoolean("experimental.enabled", false);
                                        List<String> experimental_permissions = settings.getStringList("experimental.permissions");
                                        boolean hasPermission = this.hasPermissionFromList(player, experimental_permissions);
                                        boolean mega_trees_enabled = settings.getBoolean("experimental.settings.mega_trees", false);
                                        List<Block> megaTreeSaplings = this.fourSaplingLocations(cropLocation.getBlock());
                                        boolean megaTree = megaTreeSaplings != null;
                                        if (experimental_enabled && mega_trees_enabled && megaTree && hasPermission) {
                                            Iterator var26 = megaTreeSaplings.iterator();

                                            while(var26.hasNext()) {
                                                Block block = (Block)var26.next();
                                                Sapling saplingx = (Sapling)block.getBlockData();
                                                if (saplingx.getStage() == saplingx.getMaximumStage()) {
                                                    this.generateTree(((Block)megaTreeSaplings.get(0)).getLocation(), saplingx, true);
                                                    break;
                                                }

                                                saplingx.setStage(saplingx.getStage() + 1);
                                                block.setBlockData(saplingx);
                                                player.spawnParticle(Particle.VILLAGER_HAPPY, (double)block.getX() + 0.5D, (double)block.getY(), (double)block.getZ() + 0.5D, 5, 0.25D, 0.0D, 0.25D);
                                            }
                                        } else {
                                            Sapling sapling = (Sapling)cropLocation.getBlock().getBlockData();
                                            if (sapling.getStage() != sapling.getMaximumStage()) {
                                                sapling.setStage(sapling.getStage() + 1);
                                                cropLocation.getBlock().setBlockData(sapling);
                                                player.spawnParticle(Particle.VILLAGER_HAPPY, cropLocation.getX() + 0.5D, cropLocation.getY(), cropLocation.getZ() + 0.5D, 5, 0.25D, 0.0D, 0.25D);
                                            } else {
                                                this.generateTree(cropLocation, sapling, false);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }, 0L, 5L);
        } else {
            Bukkit.getScheduler().cancelTask(this.taskid);
        }

    }

    public void generateTree(Location location, Sapling sapling, boolean megaTree) {
        TreeType type = TreeType.TREE;
        Random random = new Random();
        boolean tallTree = random.nextInt(10) == 1;
        if (megaTree) {
            for(double x = location.getX(); x < location.getX() + 2.0D; ++x) {
                for(double z = location.getZ(); z < location.getZ() + 2.0D; ++z) {
                    (new Location(location.getWorld(), x, location.getY(), z)).getBlock().setType(Material.AIR);
                }
            }

            switch(sapling.getMaterial()) {
                case JUNGLE_SAPLING:
                    type = TreeType.JUNGLE;
                    break;
                case SPRUCE_SAPLING:
                    type = TreeType.MEGA_REDWOOD;
            }
        } else {
            location.getBlock().setType(Material.AIR);
            switch(sapling.getMaterial()) {
                case JUNGLE_SAPLING:
                    type = TreeType.SMALL_JUNGLE;
                    break;
                case SPRUCE_SAPLING:
                    if (tallTree) {
                        type = TreeType.TALL_REDWOOD;
                    } else {
                        type = TreeType.REDWOOD;
                    }
                    break;
                case ACACIA_SAPLING:
                    type = TreeType.ACACIA;
                    break;
                case AZALEA:
                case FLOWERING_AZALEA:
                    type = TreeType.AZALEA;
                    break;
                case BIRCH_SAPLING:
                    if (tallTree) {
                        type = TreeType.TALL_BIRCH;
                    } else {
                        type = TreeType.BIRCH;
                    }
                    break;
                case DARK_OAK_SAPLING:
                    type = TreeType.DARK_OAK;
                    break;
                case OAK_SAPLING:
                    if (tallTree) {
                        type = TreeType.BIG_TREE;
                    } else {
                        type = TreeType.TREE;
                    }
            }
        }

        location.getWorld().generateTree(location, type);
    }

    public List<Block> fourSaplingLocations(Block block) {
        List<Block> saplingLocations = new ArrayList();
        Block blockNorth = block.getRelative(BlockFace.NORTH);
        Block blockSouth = block.getRelative(BlockFace.SOUTH);
        Block blockEast = block.getRelative(BlockFace.EAST);
        Block blockWest = block.getRelative(BlockFace.WEST);
        boolean megaTree = true;
        double[] min = new double[2];
        double[] max = new double[2];
        if (blockNorth.getType() == block.getType() && blockEast.getType() == block.getType() && block.getRelative(BlockFace.NORTH_EAST).getType() == block.getType()) {
            min[0] = (double)blockNorth.getX();
            min[1] = (double)blockNorth.getZ();
            max[0] = (double)blockEast.getX();
            max[1] = (double)blockEast.getZ();
        } else if (blockNorth.getType() == block.getType() && blockWest.getType() == block.getType() && block.getRelative(BlockFace.NORTH_WEST).getType() == block.getType()) {
            min[0] = (double)block.getRelative(BlockFace.NORTH_WEST).getX();
            min[1] = (double)block.getRelative(BlockFace.NORTH_WEST).getY();
            max[0] = (double)block.getX();
            max[1] = (double)block.getZ();
        } else if (blockSouth.getType() == block.getType() && blockEast.getType() == block.getType() && block.getRelative(BlockFace.SOUTH_EAST).getType() == block.getType()) {
            min[0] = (double)block.getX();
            min[1] = (double)block.getZ();
            max[0] = (double)block.getRelative(BlockFace.SOUTH_EAST).getX();
            max[1] = (double)block.getRelative(BlockFace.SOUTH_EAST).getZ();
        } else if (blockSouth.getType() == block.getType() && blockWest.getType() == block.getType() && block.getRelative(BlockFace.SOUTH_WEST).getType() == block.getType()) {
            min[0] = (double)blockWest.getX();
            min[1] = (double)blockWest.getZ();
            max[0] = (double)blockSouth.getX();
            max[1] = (double)blockSouth.getZ();
        } else {
            megaTree = false;
        }

        if (megaTree) {
            for(double x = min[0]; x < max[0] + 1.0D; ++x) {
                for(double z = min[1]; z < max[1] + 1.0D; ++z) {
                    saplingLocations.add((new Location(block.getWorld(), x, (double)block.getY(), z)).getBlock());
                }
            }
        }

        return saplingLocations.isEmpty() ? null : saplingLocations;
    }

    public boolean hasPermissionFromList(Player player, List<String> permissions) {
        if (permissions.isEmpty()) {
            return true;
        } else {
            Iterator var3 = permissions.iterator();

            String permission;
            do {
                if (!var3.hasNext()) {
                    return false;
                }

                permission = (String)var3.next();
            } while(!player.hasPermission(permission));

            return true;
        }
    }
}
