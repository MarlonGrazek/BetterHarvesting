package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.customfileconfiguration.CFC;
import org.bukkit.*;
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

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EVNbonemealPlants implements Listener {

    private final Main plugin;

    public EVNbonemealPlants(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBonemeal(PlayerInteractEvent e) {

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getItem() == null) return;
        if (e.getItem().getType() != Material.BONE_MEAL) return;
        if (e.getHand() != EquipmentSlot.HAND) return;

        // feature disabled
        CFC settings = plugin.getCFCSettings();
        if (!settings.getBoolean("bonemeal.enabled", true)) {
            e.setCancelled(true);
            return;
        }

        // no permission
        List<String> permissions = new ArrayList<>(settings.getStringList("bonemeal.permissions"));
        if (!hasPermissionFromList(e.getPlayer(), permissions)) {
            e.setCancelled(true);
            return;
        }

        // block disabled
        String item = e.getClickedBlock().getType().name().toLowerCase();
        if(List.of(Material.TWISTING_VINES_PLANT, Material.WEEPING_VINES_PLANT).contains(e.getClickedBlock().getType()))
            item = item.replace("_plant", "");
        if (!settings.getBoolean("bonemeal.blocks." + item, false)) {
            e.setCancelled(true);
            return;
        }

        Player player = e.getPlayer();
        Block block = e.getClickedBlock();

        double multiplier = settings.getDouble("bonemeal.strength");
        int strength = (int) Math.round(ThreadLocalRandom.current().nextInt(1, 5) * multiplier);

        // wheat, potatoes, carrots
        if (List.of(Material.WHEAT, Material.POTATOES, Material.CARROTS).contains(block.getType())) {

            Ageable ageable = (Ageable) block.getBlockData();

            if (ageable.getAge() == ageable.getMaximumAge()) return;

            e.setCancelled(true);

            int stages = (int) Math.round(ThreadLocalRandom.current().nextInt(2, 6) * multiplier);

            ageable.setAge(Math.min(ageable.getMaximumAge(), ageable.getAge() + stages));
            block.setBlockData(ageable);

            if (player.getGameMode() != GameMode.CREATIVE) e.getItem().setAmount(e.getItem().getAmount() - 1);
            notifySuccess(player, block);
        }

        // beetroots
        else if (block.getType() == Material.BEETROOTS) {

            Ageable ageable = (Ageable) block.getBlockData();
            if (ageable.getAge() == ageable.getMaximumAge()) return;

            e.setCancelled(true);

            if (ThreadLocalRandom.current().nextInt(100) < 75) {
                ageable.setAge(Math.min(ageable.getMaximumAge(), ageable.getAge() + (int) Math.round(multiplier)));
                block.setBlockData(ageable);
            }

            if (player.getGameMode() != GameMode.CREATIVE) e.getItem().setAmount(e.getItem().getAmount() - 1);
            notifySuccess(player, block);
        }

        // cocoa
        else if (block.getType() == Material.COCOA) {

            Ageable ageable = (Ageable) block.getBlockData();
            if(ageable.getAge() == ageable.getMaximumAge()) return;

            e.setCancelled(true);

            ageable.setAge(Math.min(ageable.getMaximumAge(), ageable.getAge() + (int) Math.round(multiplier)));
            block.setBlockData(ageable);

            if (player.getGameMode() != GameMode.CREATIVE) e.getItem().setAmount(e.getItem().getAmount() - 1);
            notifySuccess(player, block);
        }

        // small flowers
        else if (List.of(Material.POPPY, Material.DANDELION, Material.BLUE_ORCHID, Material.ALLIUM, Material.AZURE_BLUET,
                Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP, Material.OXEYE_DAISY,
                Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY).contains(block.getType())) {

            e.setCancelled(true);

            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(block.getType()));

            if (player.getGameMode() != GameMode.CREATIVE) e.getItem().setAmount(e.getItem().getAmount() - 1);
            notifySuccess(player, block);
        }

        // nether sprouts
        else if (block.getType() == Material.NETHER_SPROUTS) {

            e.setCancelled(true);

            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.NETHER_SPROUTS));

            if (player.getGameMode() != GameMode.CREATIVE) e.getItem().setAmount(e.getItem().getAmount() - 1);
            notifySuccess(player, block);
        }

        // azalea leaves
        else if (block.getType() == Material.AZALEA_LEAVES) {

            e.setCancelled(true);

            // persistent leaves
            boolean isPersistent = ((Leaves) block.getBlockData()).isPersistent();
            block.setType(Material.FLOWERING_AZALEA_LEAVES);
            Leaves leaves = (Leaves) block.getBlockData();
            leaves.setPersistent(isPersistent);
            block.setBlockData(leaves);

            if (player.getGameMode() != GameMode.CREATIVE) e.getItem().setAmount(e.getItem().getAmount() - 1);
            notifySuccess(player, block);
        }

        // sugar cane, cactus
        else if (block.getType() == Material.SUGAR_CANE || block.getType() == Material.CACTUS) {

            e.setCancelled(true);

            Location location = block.getLocation();
            Block currentBlock = null;

            Material material = null;
            switch (block.getType()) {
                case SUGAR_CANE -> material = Material.SUGAR_CANE;
                case CACTUS -> material = Material.CACTUS;
            }

            while (currentBlock == null) {
                if (location.getBlock().getType() == material) location.setY(location.getY() + 1);
                else currentBlock = location.getBlock();
            }

            if (currentBlock.getType() == Material.AIR) {
                if (player.getGameMode() != GameMode.CREATIVE) e.getItem().setAmount(e.getItem().getAmount() - 1);
                notifySuccess(player, block);
            }

            int maxHeight = currentBlock.getY() + strength;
            while (currentBlock.getY() < maxHeight) {
                if (currentBlock.getType() == Material.AIR) {
                    currentBlock.setType(material);
                    currentBlock = currentBlock.getRelative(BlockFace.UP);
                } else break;
            }
        }

        // vine
        else if (block.getType() == Material.VINE) {

            Location location = block.getLocation();
            Block startBlock = null;
            List<BlockFace> faces = new ArrayList<>(((MultipleFacing) block.getBlockData()).getFaces());
            if (faces.size() == 1 && faces.get(0) == BlockFace.UP) return;

            while (startBlock == null) {
                if (location.getBlock().getType() == Material.VINE) location.setY(location.getY() - 1);
                else startBlock = location.getBlock();
            }

            if (startBlock.getType() == Material.AIR) {
                e.setCancelled(true);
                if (player.getGameMode() != GameMode.CREATIVE) e.getItem().setAmount(e.getItem().getAmount() - 1);

                for (BlockFace face : faces) {
                    switch (face) {
                        case NORTH -> player.spawnParticle(Particle.VILLAGER_HAPPY,
                                block.getLocation().add(0.5, 0.5, 0.1), 5, 0.25, 0.25, 0);
                        case SOUTH -> player.spawnParticle(Particle.VILLAGER_HAPPY,
                                block.getLocation().add(0.5, 0.5, 0.9), 5, 0.25, 0.25, 0);
                        case EAST -> player.spawnParticle(Particle.VILLAGER_HAPPY,
                                block.getLocation().add(0.9, 0.5, 0.5), 5, 0, 0.25, 0.25);
                        case WEST -> player.spawnParticle(Particle.VILLAGER_HAPPY,
                                block.getLocation().add(0.1, 0.5, 0.5), 5, 0, 0.25, 0.25);
                        case UP -> player.spawnParticle(Particle.VILLAGER_HAPPY,
                                block.getLocation().add(0.5, 0.9, 0.5), 5, 0.25, 0, 0.25);
                    }
                }

                player.playSound(player.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1, 1);
            }

            int minHeight = startBlock.getY() - strength;
            while (startBlock.getY() > minHeight) {
                if (startBlock.getType() == Material.AIR) {
                    startBlock.setType(Material.VINE);
                    MultipleFacing multipleFacing = (MultipleFacing) startBlock.getBlockData();
                    faces.forEach(face -> {
                        if (face != BlockFace.UP) multipleFacing.setFace(face, true);
                    });
                    startBlock.setBlockData(multipleFacing);
                    startBlock = startBlock.getRelative(BlockFace.DOWN);
                } else break;
            }
        }

        // dead bush
        else if (block.getType() == Material.DEAD_BUSH) {

            e.setCancelled(true);

            List<Material> saplings = Arrays.asList(Material.ACACIA_SAPLING, Material.AZALEA, Material.BIRCH_SAPLING,
                    Material.DARK_OAK_SAPLING, Material.FLOWERING_AZALEA, Material.JUNGLE_SAPLING, Material.OAK_SAPLING,
                    Material.SPRUCE_SAPLING);

            Random random = new Random();

            Material sapling = saplings.get(random.nextInt(saplings.size()));
            block.setType(sapling);

            if (player.getGameMode() != GameMode.CREATIVE) e.getItem().setAmount(e.getItem().getAmount() - 1);
            notifySuccess(player, block);
        }

        // nether warts
        else if (block.getType() == Material.NETHER_WART) {

            Ageable crop = (Ageable) block.getBlockData();
            if (crop.getAge() == crop.getMaximumAge()) return;

            e.setCancelled(true);

            Random random = new Random();
            int customStrength;
            int randomint = random.nextInt(10);

            if (randomint <= 6) customStrength = 1;
            else if (randomint > 6 && randomint <= 9) customStrength = 2;
            else customStrength = 3;

            crop.setAge(Math.min(crop.getAge() + customStrength, 3));
            block.setBlockData(crop);

            if (player.getGameMode() != GameMode.CREATIVE) e.getItem().setAmount(e.getItem().getAmount() - 1);
            notifySuccess(player, block);
        }

        // dirt, netherrack
        else if (block.getType() == Material.DIRT || block.getType() == Material.NETHERRACK) {

            e.setCancelled(true);

            Random random = new Random();
            int color = random.nextInt(2);

            for (int x = block.getLocation().getBlockX() - strength; x < block.getLocation().getBlockX() + strength + 1; x++) {
                for (int z = block.getLocation().getBlockZ() - strength; z < block.getLocation().getBlockZ() + strength + 1; z++) {

                    Block currentBlock = new Location(block.getWorld(), x, block.getY(), z).getBlock();

                    int randomInt = random.nextInt(100);
                    int chance = Math.abs(block.getLocation().getBlockX() - x) + Math.abs(block.getLocation().getBlockZ() - z) + 1;

                    Material material = null;
                    if (currentBlock.getType() == Material.DIRT) material = Material.GRASS_BLOCK;
                    else if (currentBlock.getType() == Material.NETHERRACK) {
                        if (color == 1) material = Material.CRIMSON_NYLIUM;
                        else material = Material.WARPED_NYLIUM;
                    }

                    if (material == null || randomInt > 100 / chance) continue;
                    currentBlock.setType(material);
                    Location particleLocation = currentBlock.getLocation().add(0, 0.5, 0);
                    player.spawnParticle(Particle.VILLAGER_HAPPY, particleLocation, 5, 0.25, 0, 0.25);
                }
            }

            player.playSound(player.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1, 1);
            if (player.getGameMode() != GameMode.CREATIVE) e.getItem().setAmount(e.getItem().getAmount() - 1);
        }
    }

    private void notifySuccess(Player player, Block block) {
        player.spawnParticle(Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 0.5, 0.5), 5, 0.25, 0, 0.25);
        player.playSound(player.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1, 1);
    }

    public boolean hasPermissionFromList(Player player, List<String> permissions) {
        if (permissions.isEmpty()) return true;
        for (String permission : permissions) if (player.hasPermission(permission)) return true;
        return false;
    }
}
