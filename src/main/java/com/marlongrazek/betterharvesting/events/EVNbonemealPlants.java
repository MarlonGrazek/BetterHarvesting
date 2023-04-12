package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.datafile.DataFile;
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

    private enum BonemealableBlock {

        POPPY, DANDELION, BLUE_ORCHID, ALLIUM, AZURE_BLUET, RED_TULIP, ORANGE_TULIP, WHITE_TULIP, PINK_TULIP, OXEYE_DAISY,
        CORNFLOWER, LILY_OF_THE_VALLEY, NETHER_SPROUTS, AZALEA_LEAVES;

        private Material material;
        private Material newMaterial;
        private List<ItemStack> drops = new ArrayList<>();

        static {
            for (BonemealableBlock block : BonemealableBlock.values()) {
                block.material = Material.valueOf(block.name());
                block.newMaterial = block.material;
            }

            Arrays.asList(POPPY, DANDELION, BLUE_ORCHID, ALLIUM, AZURE_BLUET, RED_TULIP, ORANGE_TULIP,
                            WHITE_TULIP, PINK_TULIP, OXEYE_DAISY, CORNFLOWER, LILY_OF_THE_VALLEY)
                    .forEach(item -> item.drops = Collections.singletonList(new ItemStack(item.material)));

            NETHER_SPROUTS.newMaterial = Material.WARPED_ROOTS;
            AZALEA_LEAVES.newMaterial = Material.FLOWERING_AZALEA_LEAVES;
        }

        public Material getMaterial() {
            return material;
        }

        public Material getNewMaterial() {
            return newMaterial;
        }

        public List<ItemStack> getDrops() {
            return drops;
        }
    }

    @EventHandler
    public void onBonemeal(PlayerInteractEvent e) {

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getItem() == null) return;
        if (e.getItem().getType() != Material.BONE_MEAL) return;
        if (e.getHand() != EquipmentSlot.HAND) return;

        // feature disabled
        DataFile settings = plugin.getDataFile("settings");
        if (!settings.getBoolean("bonemealing.enabled", true)) return;

        // no permission
        List<String> permissions = new ArrayList<>(settings.getStringList("bonemealing.permissions"));
        if (!hasPermissionFromList(e.getPlayer(), permissions)) return;

        // block disabled
        String item = e.getClickedBlock().getType().name().toLowerCase();
        if (!settings.getBoolean("bonemealing.blocks." + item, false)) return;

        List<Material> bonemealableBlocks = new ArrayList<>();
        for (BonemealableBlock block : BonemealableBlock.values()) bonemealableBlocks.add(block.getMaterial());

        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        int strength = getStrength();

        // wheat, potatoes, carrots
        if(List.of(Material.WHEAT, Material.POTATOES, Material.CARROTS).contains(block.getType())) {

            Ageable ageable = (Ageable) block.getBlockData();

            if(ageable.getAge() == ageable.getMaximumAge()) return;

            e.setCancelled(true);

            double bonemeal_strength = settings.getDouble("custom_drops." + block.getType().name().toLowerCase() + ".bonemeal_strength");
            int stages = (int) Math.round(ThreadLocalRandom.current().nextInt(2, 6) * bonemeal_strength);

            ageable.setAge(Math.min(ageable.getMaximumAge(), ageable.getAge() + stages));
            block.setBlockData(ageable);

            spawnParticle(player, block.getLocation());
            player.playSound(player.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1, 1);
            if (player.getGameMode() != GameMode.CREATIVE) e.getItem().setAmount(e.getItem().getAmount() - 1);
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
                spawnParticle(player, block.getLocation());
                player.playSound(player.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1, 1);
                if (player.getGameMode() != GameMode.CREATIVE) e.getItem().setAmount(e.getItem().getAmount() - 1);
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

            e.setCancelled(true);

            Location location = block.getLocation();
            Block startBlock = null;
            List<BlockFace> faces = new ArrayList<>(((MultipleFacing) block.getBlockData()).getFaces());
            faces.removeIf(face -> face == BlockFace.UP);

            while (startBlock == null) {
                if (location.getBlock().getType() == Material.VINE) location.setY(location.getY() - 1);
                else startBlock = location.getBlock();
            }

            if (startBlock.getType() == Material.AIR) {
                spawnParticle(player, block.getLocation());
                player.playSound(player.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1, 1);
                if (player.getGameMode() != GameMode.CREATIVE) e.getItem().setAmount(e.getItem().getAmount() - 1);
            }

            int minHeight = startBlock.getY() - strength;
            while (startBlock.getY() > minHeight) {
                if (startBlock.getType() == Material.AIR) {
                    startBlock.setType(Material.VINE);
                    MultipleFacing multipleFacing = (MultipleFacing) startBlock.getBlockData();
                    faces.forEach(face -> multipleFacing.setFace(face, true));
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
            spawnParticle(player, block.getLocation());
            player.playSound(player.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1, 1);
            if (player.getGameMode() != GameMode.CREATIVE) e.getItem().setAmount(e.getItem().getAmount() - 1);
        }

        // nether warts
        else if (block.getType() == Material.NETHER_WART) {

            e.setCancelled(true);

            Ageable crop = (Ageable) block.getBlockData();
            if (crop.getAge() == crop.getMaximumAge()) return;

            Random random = new Random();
            int customStrength;
            int randomint = random.nextInt(10);

            if (randomint <= 6) customStrength = 1;
            else if (randomint > 6 && randomint <= 9) customStrength = 2;
            else customStrength = 3;

            crop.setAge(Math.min(crop.getAge() + customStrength, 3));
            block.setBlockData(crop);

            spawnParticle(player, block.getLocation());
            player.playSound(player.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1, 1);
            if (player.getGameMode() != GameMode.CREATIVE) e.getItem().setAmount(e.getItem().getAmount() - 1);
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
                    Location particleLocation = currentBlock.getLocation();
                    particleLocation.setY(particleLocation.getY() + 0.5);
                    spawnParticle(player, particleLocation);
                }
            }

            player.playSound(player.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1, 1);
            if (player.getGameMode() != GameMode.CREATIVE) e.getItem().setAmount(e.getItem().getAmount() - 1);
        }

        // bonemealable blocks
        else if (bonemealableBlocks.contains(block.getType())) {

            e.setCancelled(true);

            BonemealableBlock bonemealableBlock = BonemealableBlock.valueOf(block.getType().name());

            // persistent leaves
            if (bonemealableBlock.getMaterial() == Material.AZALEA_LEAVES) {
                boolean isPersistent = ((Leaves) block.getBlockData()).isPersistent();
                block.setType(bonemealableBlock.getNewMaterial());
                Leaves leaves = (Leaves) block.getBlockData();
                leaves.setPersistent(isPersistent);
                block.setBlockData(leaves);
            }

            // new material
            else {
                if (bonemealableBlock.getNewMaterial() != null) {
                    block.setType(bonemealableBlock.getNewMaterial());
                }
            }

            // drops
            if (!bonemealableBlock.getDrops().isEmpty())
                bonemealableBlock.drops.forEach(drop -> block.getWorld().dropItemNaturally(block.getLocation(), drop));

            spawnParticle(player, block.getLocation());
            player.playSound(player.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1, 1);
            if (player.getGameMode() != GameMode.CREATIVE) e.getItem().setAmount(e.getItem().getAmount() - 1);
        }
    }

    public void spawnParticle(Player player, Location location) {
        player.spawnParticle(Particle.VILLAGER_HAPPY, location.getX() + 0.5, location.getY() + 0.5,
                location.getZ() + 0.5, 5, 0.25, 0, 0.25);
    }

    public int getStrength() {
        Random random = new Random();
        int randomInt = random.nextInt(100);

        if (randomInt > 50 && randomInt <= 80) return 2;
        else if (randomInt > 80 && randomInt <= 95) return 3;
        else if (randomInt > 95 && randomInt <= 100) return 4;
        else return 1;
    }

    public boolean hasPermissionFromList(Player player, List<String> permissions) {
        if (permissions.isEmpty()) return true;
        for (String permission : permissions) if (player.hasPermission(permission)) return true;
        return false;
    }
}
