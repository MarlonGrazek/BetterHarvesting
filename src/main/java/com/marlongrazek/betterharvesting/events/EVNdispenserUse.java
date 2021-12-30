package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.builder.StringBuilder;
import com.marlongrazek.betterharvesting.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EVNdispenserUse implements Listener {

    private enum DispensableBlock {

        GRASS, TALL_GRASS, DEAD_BUSH, SEAGRASS, TALL_SEAGRASS, NETHER_SPROUTS, FERN, LARGE_FERN, SMALL_DRIPLEAF,
        AZALEA_LEAVES, ACACIA_LEAVES, BIRCH_LEAVES, DARK_OAK_LEAVES, FLOWERING_AZALEA_LEAVES, JUNGLE_LEAVES, OAK_LEAVES,
        SPRUCE_LEAVES, ACACIA_SAPLING, AZALEA, BIRCH_SAPLING, DARK_OAK_SAPLING, FLOWERING_AZALEA, JUNGLE_SAPLING,
        OAK_SAPLING, SPRUCE_SAPLING, WARPED_ROOTS, CRIMSON_ROOTS, WARPED_FUNGUS, CRIMSON_FUNGUS, BROWN_MUSHROOM, RED_MUSHROOM,
        PUMPKIN, DANDELION, POPPY, BLUE_ORCHID, ALLIUM, AZURE_BLUET, RED_TULIP, ORANGE_TULIP, WHITE_TULIP, PINK_TULIP,
        OXEYE_DAISY, CORNFLOWER, LILY_OF_THE_VALLEY, WITHER_ROSE, DIRT, GRASS_BLOCK, WHEAT, BEETROOTS, CARROTS, POTATOES;

        private Material material;
        private final HashMap<Material, Material> leftOver = new HashMap<>();
        private final HashMap<Material, List<ItemStack>> drops = new HashMap<>();

        static {
            for (DispensableBlock block : DispensableBlock.values()) block.material = Material.valueOf(block.name());

            TALL_GRASS.leftOver.put(Material.SHEARS, Material.GRASS);
            TALL_GRASS.drops.put(Material.SHEARS, Collections.singletonList(new ItemStack(Material.GRASS)));

            TALL_SEAGRASS.leftOver.put(Material.SHEARS, Material.SEAGRASS);
            TALL_SEAGRASS.drops.put(Material.SHEARS, Collections.singletonList(new ItemStack(Material.SEAGRASS)));

            LARGE_FERN.leftOver.put(Material.SHEARS, Material.FERN);
            LARGE_FERN.drops.put(Material.SHEARS, Collections.singletonList(new ItemStack(Material.FERN)));

            WARPED_ROOTS.leftOver.put(Material.SHEARS, Material.NETHER_SPROUTS);
            WARPED_ROOTS.drops.put(Material.SHEARS, Collections.singletonList(new ItemStack(Material.NETHER_SPROUTS)));

            PUMPKIN.leftOver.put(Material.SHEARS, Material.CARVED_PUMPKIN);
            PUMPKIN.drops.put(Material.SHEARS, Collections.singletonList(new ItemStack(Material.PUMPKIN_SEEDS, 4)));

            List<DispensableBlock> saplings = Arrays.asList(ACACIA_SAPLING, AZALEA, BIRCH_SAPLING, DARK_OAK_SAPLING,
                    FLOWERING_AZALEA, JUNGLE_SAPLING, OAK_SAPLING, SPRUCE_SAPLING);

            saplings.forEach(sapling -> {
                sapling.leftOver.put(Material.SHEARS, Material.DEAD_BUSH);

                StringBuilder leavesName = new StringBuilder(sapling.name());
                if (!sapling.name().endsWith("SAPLING")) leavesName.add("_LEAVES");
                leavesName.replace("SAPLING", "LEAVES");

                sapling.drops.put(Material.SHEARS, Collections.singletonList(new ItemStack(Material.valueOf(leavesName.toString()))));
            });

            Arrays.asList(Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE,
                    Material.DIAMOND_HOE, Material.NETHERITE_HOE).forEach(hoe -> {

                for (DispensableBlock block : DispensableBlock.values()) {
                    block.drops.put(hoe, Collections.singletonList(new ItemStack(block.material)));
                    block.leftOver.put(hoe, null);
                }

                TALL_SEAGRASS.drops.put(hoe, Collections.singletonList(new ItemStack(Material.SEAGRASS, 2)));

                DIRT.drops.put(hoe, null);
                DIRT.leftOver.put(hoe, Material.FARMLAND);

                GRASS_BLOCK.drops.put(hoe, null);
                GRASS_BLOCK.leftOver.put(hoe, Material.FARMLAND);

                Arrays.asList(WHEAT, BEETROOTS, CARROTS, POTATOES).forEach(crop -> crop.drops.put(hoe, null));
            });
        }

        public HashMap<Material, List<ItemStack>> getDrops() {
            return drops;
        }

        public Material getMaterial() {
            return material;
        }

        public HashMap<Material, Material> getLeftOver() {
            return leftOver;
        }
    }

    private enum DispensableItem {

        SUGAR_CANE, CACTUS, BAMBOO, ACACIA_SAPLING, AZALEA, BIRCH_SAPLING, DARK_OAK_SAPLING, FLOWERING_AZALEA, JUNGLE_SAPLING,
        OAK_SAPLING, SPRUCE_SAPLING, WHEAT_SEEDS, BEETROOT_SEEDS, CARROT, POTATO, DANDELION, POPPY, BLUE_ORCHID, ALLIUM,
        AZURE_BLUET, RED_TULIP, ORANGE_TULIP, WHITE_TULIP, PINK_TULIP, OXEYE_DAISY, CORNFLOWER, LILY_OF_THE_VALLEY;

        private Material material;
        private Material placingMaterial;
        private List<Material> requiredBlocks = new ArrayList<>();
        private boolean requiresWater = false;
        private boolean requiresAir = false;

        static {
            for (DispensableItem item : DispensableItem.values()) {
                item.material = Material.valueOf(item.name());
                item.placingMaterial = item.material;
            }

            List<DispensableItem> saplings = Arrays.asList(ACACIA_SAPLING, AZALEA, BIRCH_SAPLING, DARK_OAK_SAPLING,
                    FLOWERING_AZALEA, JUNGLE_SAPLING, OAK_SAPLING, SPRUCE_SAPLING);

            List<DispensableItem> flowers = Arrays.asList(POPPY, BLUE_ORCHID, ALLIUM, AZURE_BLUET, RED_TULIP, ORANGE_TULIP,
                    WHITE_TULIP, PINK_TULIP, OXEYE_DAISY, CORNFLOWER, LILY_OF_THE_VALLEY);

            SUGAR_CANE.requiredBlocks = Arrays.asList(Material.GRASS_BLOCK, Material.MYCELIUM, Material.PODZOL,
                    Material.DIRT, Material.ROOTED_DIRT, Material.COARSE_DIRT, Material.SAND);
            SUGAR_CANE.requiresWater = true;

            CACTUS.requiredBlocks = Collections.singletonList(Material.SAND);
            CACTUS.requiresAir = true;

            BAMBOO.placingMaterial = Material.BAMBOO_SAPLING;
            BAMBOO.requiredBlocks = Arrays.asList(Material.GRASS_BLOCK, Material.MYCELIUM, Material.PODZOL,
                    Material.DIRT, Material.ROOTED_DIRT, Material.COARSE_DIRT, Material.SAND);

            saplings.forEach(sapling -> sapling.requiredBlocks = Arrays.asList(Material.GRASS_BLOCK, Material.MYCELIUM,
                    Material.PODZOL, Material.DIRT, Material.ROOTED_DIRT, Material.COARSE_DIRT));

            flowers.forEach(flower -> flower.requiredBlocks = Arrays.asList(Material.GRASS_BLOCK, Material.MYCELIUM,
                    Material.PODZOL, Material.DIRT, Material.ROOTED_DIRT, Material.COARSE_DIRT));

            WHEAT_SEEDS.placingMaterial = Material.WHEAT;
            WHEAT_SEEDS.requiredBlocks = Collections.singletonList(Material.FARMLAND);

            BEETROOT_SEEDS.placingMaterial = Material.BEETROOTS;
            BEETROOT_SEEDS.requiredBlocks = Collections.singletonList(Material.FARMLAND);

            CARROT.placingMaterial = Material.CARROTS;
            CARROT.requiredBlocks = Collections.singletonList(Material.FARMLAND);

            POTATO.placingMaterial = Material.POTATOES;
            POTATO.requiredBlocks = Collections.singletonList(Material.FARMLAND);
        }

        public Material getMaterial() {
            return material;
        }

        public List<Material> getRequiredBlocks() {
            return requiredBlocks;
        }

        public boolean requiresWater() {
            return requiresWater;
        }

        public Material getPlacingMaterial() {
            return placingMaterial;
        }

        public boolean requiresAir() {
            return requiresAir;
        }
    }

    @EventHandler
    public void onDispense(BlockDispenseEvent e) {

        List<Material> dispensableBlocks = new ArrayList<>();
        for (DispensableBlock block : DispensableBlock.values()) dispensableBlocks.add(block.getMaterial());

        List<Material> dispensableItems = new ArrayList<>();
        for (DispensableItem item : DispensableItem.values()) dispensableItems.add(item.getMaterial());

        Dispenser dispenser = (Dispenser) e.getBlock().getState();
        Block facing = getFacingBlock(e.getBlock());

        // dispensable item
        if (dispensableItems.contains(e.getItem().getType())) {

            e.setCancelled(true);

            DispensableItem item = DispensableItem.valueOf(e.getItem().getType().name());
            Block place;
            Block block;

            // block
            if (item.getRequiredBlocks().contains(facing.getType()) && facing.getRelative(BlockFace.UP).getType() == Material.AIR) {
                place = facing.getRelative(BlockFace.UP);
                block = facing;
            }

            // air
            else if (item.getRequiredBlocks().contains(facing.getRelative(BlockFace.DOWN).getType()) && facing.getType() == Material.AIR) {
                place = facing;
                block = facing.getRelative(BlockFace.DOWN);
            }

            // other
            else return;

            if (item.requiresWater()) if (!getSurroundingMaterials(block).contains(Material.WATER)) return;

            if (item.requiresAir())
                for (Material material : getSurroundingMaterials(place)) if (material != Material.AIR) return;

            place.setType(item.getPlacingMaterial());
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> dispenser.getInventory().removeItem(e.getItem()), 1);
        }

        // dispensable blocks
        else if (dispensableBlocks.contains(facing.getType())) {

            e.setCancelled(true);

            DispensableBlock block = DispensableBlock.valueOf(facing.getType().name());

            // crops
            if (Arrays.asList(Material.WHEAT, Material.BEETROOTS, Material.POTATOES, Material.CARROTS).contains(block.getMaterial())) {

                Ageable crop = (Ageable) facing.getBlockData();
                if (crop.getAge() != crop.getMaximumAge()) return;

                getCropDrops(facing).forEach(drop -> facing.getWorld().dropItemNaturally(facing.getLocation(), drop));

                facing.setType(facing.getType());
                return;
            }

            // leftover
            if (block.getLeftOver().containsKey(e.getItem().getType())) {
                if (block.getLeftOver().get(e.getItem().getType()) == null) facing.setType(Material.AIR);
                else facing.setType(block.getLeftOver().get(e.getItem().getType()));
            }

            // drops
            if (block.getDrops().containsKey(e.getItem().getType()) && block.getDrops().get(e.getItem().getType()) != null)
                for (ItemStack drop : block.getDrops().get(e.getItem().getType()))
                    facing.getWorld().dropItemNaturally(facing.getLocation(), drop);
        }
    }

    public Block getFacingBlock(Block block) {

        Location location = block.getLocation();
        Directional directional = (Directional) block.getBlockData();

        switch (directional.getFacing()) {
            case NORTH -> location.setZ(location.getZ() - 1);
            case EAST -> location.setX(location.getX() + 1);
            case SOUTH -> location.setZ(location.getZ() + 1);
            case WEST -> location.setX(location.getX() - 1);
            case UP -> location.setY(location.getY() + 1);
            case DOWN -> location.setY(location.getY() - 1);
        }

        return location.getBlock();
    }

    public List<Material> getSurroundingMaterials(Block block) {
        List<Material> materials = new ArrayList<>();
        materials.add(block.getRelative(BlockFace.NORTH).getType());
        materials.add(block.getRelative(BlockFace.SOUTH).getType());
        materials.add(block.getRelative(BlockFace.EAST).getType());
        materials.add(block.getRelative(BlockFace.WEST).getType());
        return materials;
    }

    public List<ItemStack> getCropDrops(Block block) {

        List<ItemStack> drops = new ArrayList<>(block.getDrops());

        boolean removedSeed = false;
        for (ItemStack drop : drops) {
            if ((drop.getType() == Material.POTATO || drop.getType() == Material.CARROT || drop.getType() == Material.BEETROOT_SEEDS
                    || drop.getType() == Material.WHEAT_SEEDS) && !removedSeed) {
                drop.setAmount(drop.getAmount() - 1);
                removedSeed = true;
            }
        }

        return drops;
    }
}
