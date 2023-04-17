//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.datafile.DataFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class EVNbonemealPlants implements Listener {
    private final Main plugin;

    public EVNbonemealPlants(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBonemeal(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getItem() != null) {
                if (e.getItem().getType() == Material.BONE_MEAL) {
                    if (e.getHand() == EquipmentSlot.HAND) {
                        DataFile settings = this.plugin.getDataFile("settings");
                        if (settings.getBoolean("bonemealing.enabled", true)) {
                            List<String> permissions = new ArrayList(settings.getStringList("bonemealing.permissions"));
                            if (this.hasPermissionFromList(e.getPlayer(), permissions)) {
                                String item = e.getClickedBlock().getType().name().toLowerCase();
                                if (settings.getBoolean("bonemealing.blocks." + item, false)) {
                                    List<Material> bonemealableBlocks = new ArrayList();
                                    EVNbonemealPlants.BonemealableBlock[] var6 = EVNbonemealPlants.BonemealableBlock.values();
                                    int var7 = var6.length;

                                    int strength;
                                    EVNbonemealPlants.BonemealableBlock bonemealableBlock;
                                    for (strength = 0; strength < var7; ++strength) {
                                        bonemealableBlock = var6[strength];
                                        bonemealableBlocks.add(bonemealableBlock.getMaterial());
                                    }

                                    Player player = e.getPlayer();
                                    Block block = e.getClickedBlock();
                                    strength = this.getStrength();
                                    Block currentBlock;
                                    Material material;
                                    int z;
                                    Location location;
                                    if (block.getType() != Material.SUGAR_CANE && block.getType() != Material.CACTUS) {
                                        if (block.getType() == Material.VINE) {
                                            e.setCancelled(true);
                                            location = block.getLocation();
                                            currentBlock = null;
                                            List<BlockFace> faces = new ArrayList(((MultipleFacing) block.getBlockData()).getFaces());
                                            faces.removeIf((face) -> {
                                                return face == BlockFace.UP;
                                            });

                                            while (currentBlock == null) {
                                                if (location.getBlock().getType() == Material.VINE) {
                                                    location.setY(location.getY() - 1.0D);
                                                } else {
                                                    currentBlock = location.getBlock();
                                                }
                                            }

                                            if (currentBlock.getType() == Material.AIR) {
                                                this.spawnParticle(player, block.getLocation());
                                                player.playSound(player.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0F, 1.0F);
                                                if (player.getGameMode() != GameMode.CREATIVE) {
                                                    e.getItem().setAmount(e.getItem().getAmount() - 1);
                                                }
                                            }

                                            for (z = currentBlock.getY() - strength; currentBlock.getY() > z && currentBlock.getType() == Material.AIR; currentBlock = currentBlock.getRelative(BlockFace.DOWN)) {
                                                currentBlock.setType(Material.VINE);
                                                MultipleFacing multipleFacing = (MultipleFacing) currentBlock.getBlockData();
                                                faces.forEach((face) -> {
                                                    multipleFacing.setFace(face, true);
                                                });
                                                currentBlock.setBlockData(multipleFacing);
                                            }
                                        } else {
                                            Random random;
                                            if (block.getType() == Material.DEAD_BUSH) {
                                                e.setCancelled(true);
                                                List<Material> saplings = Arrays.asList(Material.ACACIA_SAPLING, Material.AZALEA, Material.BIRCH_SAPLING, Material.DARK_OAK_SAPLING, Material.FLOWERING_AZALEA, Material.JUNGLE_SAPLING, Material.OAK_SAPLING, Material.SPRUCE_SAPLING);
                                                random = new Random();
                                                material = (Material) saplings.get(random.nextInt(saplings.size()));
                                                block.setType(material);
                                                this.spawnParticle(player, block.getLocation());
                                                player.playSound(player.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0F, 1.0F);
                                                if (player.getGameMode() != GameMode.CREATIVE) {
                                                    e.getItem().setAmount(e.getItem().getAmount() - 1);
                                                }
                                            } else if (block.getType() == Material.NETHER_WART) {
                                                e.setCancelled(true);
                                                Ageable crop = (Ageable) block.getBlockData();
                                                if (crop.getAge() == crop.getMaximumAge()) {
                                                    return;
                                                }

                                                random = new Random();
                                                z = random.nextInt(10);
                                                byte customStrength;
                                                if (z <= 6) {
                                                    customStrength = 1;
                                                } else if (z > 6 && z <= 9) {
                                                    customStrength = 2;
                                                } else {
                                                    customStrength = 3;
                                                }

                                                crop.setAge(Math.min(crop.getAge() + customStrength, 3));
                                                block.setBlockData(crop);
                                                this.spawnParticle(player, block.getLocation());
                                                player.playSound(player.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0F, 1.0F);
                                                if (player.getGameMode() != GameMode.CREATIVE) {
                                                    e.getItem().setAmount(e.getItem().getAmount() - 1);
                                                }
                                            } else if (block.getType() != Material.DIRT && block.getType() != Material.NETHERRACK) {
                                                if (bonemealableBlocks.contains(block.getType())) {
                                                    e.setCancelled(true);
                                                    bonemealableBlock = EVNbonemealPlants.BonemealableBlock.valueOf(block.getType().name());
                                                    if (bonemealableBlock.getMaterial() == Material.AZALEA_LEAVES) {
                                                        boolean isPersistent = ((Leaves) block.getBlockData()).isPersistent();
                                                        block.setType(bonemealableBlock.getNewMaterial());
                                                        Leaves leaves = (Leaves) block.getBlockData();
                                                        leaves.setPersistent(isPersistent);
                                                        block.setBlockData(leaves);
                                                    } else if (bonemealableBlock.getNewMaterial() != null) {
                                                        block.setType(bonemealableBlock.getNewMaterial());
                                                    }

                                                    if (!bonemealableBlock.getDrops().isEmpty()) {
                                                        bonemealableBlock.drops.forEach((drop) -> {
                                                            block.getWorld().dropItemNaturally(block.getLocation(), drop);
                                                        });
                                                    }

                                                    this.spawnParticle(player, block.getLocation());
                                                    player.playSound(player.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0F, 1.0F);
                                                    if (player.getGameMode() != GameMode.CREATIVE) {
                                                        e.getItem().setAmount(e.getItem().getAmount() - 1);
                                                    }
                                                }
                                            } else {
                                                e.setCancelled(true);
                                                random = new Random();
                                                int color = random.nextInt(2);

                                                for (int x = block.getLocation().getBlockX() - strength; x < block.getLocation().getBlockX() + strength + 1; ++x) {
                                                    for (z = block.getLocation().getBlockZ() - strength; z < block.getLocation().getBlockZ() + strength + 1; ++z) {
                                                        currentBlock = (new Location(block.getWorld(), (double) x, (double) block.getY(), (double) z)).getBlock();
                                                        int randomInt = random.nextInt(100);
                                                        int chance = Math.abs(block.getLocation().getBlockX() - x) + Math.abs(block.getLocation().getBlockZ() - z) + 1;
                                                        material = null;
                                                        if (currentBlock.getType() == Material.DIRT) {
                                                            material = Material.GRASS_BLOCK;
                                                        } else if (currentBlock.getType() == Material.NETHERRACK) {
                                                            if (color == 1) {
                                                                material = Material.CRIMSON_NYLIUM;
                                                            } else {
                                                                material = Material.WARPED_NYLIUM;
                                                            }
                                                        }

                                                        if (material != null && randomInt <= 100 / chance) {
                                                            currentBlock.setType(material);
                                                            Location particleLocation = currentBlock.getLocation();
                                                            particleLocation.setY(particleLocation.getY() + 0.5D);
                                                            this.spawnParticle(player, particleLocation);
                                                        }
                                                    }
                                                }

                                                player.playSound(player.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0F, 1.0F);
                                                if (player.getGameMode() != GameMode.CREATIVE) {
                                                    e.getItem().setAmount(e.getItem().getAmount() - 1);
                                                }
                                            }
                                        }
                                    } else {
                                        e.setCancelled(true);
                                        location = block.getLocation();
                                        currentBlock = null;
                                        material = null;
                                        switch (block.getType()) {
                                            case SUGAR_CANE:
                                                material = Material.SUGAR_CANE;
                                                break;
                                            case CACTUS:
                                                material = Material.CACTUS;
                                        }

                                        while (currentBlock == null) {
                                            if (location.getBlock().getType() == material) {
                                                location.setY(location.getY() + 1.0D);
                                            } else {
                                                currentBlock = location.getBlock();
                                            }
                                        }

                                        if (currentBlock.getType() == Material.AIR) {
                                            this.spawnParticle(player, block.getLocation());
                                            player.playSound(player.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0F, 1.0F);
                                            if (player.getGameMode() != GameMode.CREATIVE) {
                                                e.getItem().setAmount(e.getItem().getAmount() - 1);
                                            }
                                        }

                                        for (z = currentBlock.getY() + strength; currentBlock.getY() < z && currentBlock.getType() == Material.AIR; currentBlock = currentBlock.getRelative(BlockFace.UP)) {
                                            currentBlock.setType(material);
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void spawnParticle(Player player, Location location) {
        player.spawnParticle(Particle.VILLAGER_HAPPY, location.getX() + 0.5D, location.getY() + 0.5D, location.getZ() + 0.5D, 5, 0.25D, 0.0D, 0.25D);
    }

    public int getStrength() {
        Random random = new Random();
        int randomInt = random.nextInt(100);
        if (randomInt > 50 && randomInt <= 80) {
            return 2;
        } else if (randomInt > 80 && randomInt <= 95) {
            return 3;
        } else {
            return randomInt > 95 && randomInt <= 100 ? 4 : 1;
        }
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

                permission = (String) var3.next();
            } while (!player.hasPermission(permission));

            return true;
        }
    }

    private static enum BonemealableBlock {
        POPPY,
        DANDELION,
        BLUE_ORCHID,
        ALLIUM,
        AZURE_BLUET,
        RED_TULIP,
        ORANGE_TULIP,
        WHITE_TULIP,
        PINK_TULIP,
        OXEYE_DAISY,
        CORNFLOWER,
        LILY_OF_THE_VALLEY,
        NETHER_SPROUTS,
        AZALEA_LEAVES;

        private Material material;
        private Material newMaterial;
        private List<ItemStack> drops = new ArrayList();

        private BonemealableBlock() {
        }

        public Material getMaterial() {
            return this.material;
        }

        public Material getNewMaterial() {
            return this.newMaterial;
        }

        public List<ItemStack> getDrops() {
            return this.drops;
        }

        static {
            EVNbonemealPlants.BonemealableBlock[] var0 = values();
            int var1 = var0.length;

            for (int var2 = 0; var2 < var1; ++var2) {
                EVNbonemealPlants.BonemealableBlock block = var0[var2];
                block.material = Material.valueOf(block.name());
                block.newMaterial = block.material;
            }

            Arrays.asList(POPPY, DANDELION, BLUE_ORCHID, ALLIUM, AZURE_BLUET, RED_TULIP, ORANGE_TULIP, WHITE_TULIP, PINK_TULIP, OXEYE_DAISY, CORNFLOWER, LILY_OF_THE_VALLEY).forEach((item) -> {
                item.drops = Collections.singletonList(new ItemStack(item.material));
            });
            NETHER_SPROUTS.newMaterial = Material.WARPED_ROOTS;
            AZALEA_LEAVES.newMaterial = Material.FLOWERING_AZALEA_LEAVES;
        }
    }
}
