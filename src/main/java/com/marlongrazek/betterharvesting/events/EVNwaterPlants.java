package com.marlongrazek.betterharvesting.events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.*;

public class EVNwaterPlants implements Listener {

    private static final String newMaterialKey = "new_material";
    private static final String growingKey = "growing";
    private static final String requiresKey = "requires";

    private enum WaterablePlant {

        GRASS_BLOCK, DIRT, POPPY, DANDELION, BLUE_ORCHID, ALLIUM, AZURE_BLUET, RED_TULIP, ORANGE_TULIP, WHITE_TULIP,
        PINK_TULIP, OXEYE_DAISY, CORNFLOWER, LILY_OF_THE_VALLEY, GRASS, TALL_GRASS, FERN, LARGE_FERN, ACACIA_SAPLING,
        AZALEA, BIRCH_SAPLING, DARK_OAK_SAPLING, FLOWERING_AZALEA, JUNGLE_SAPLING, OAK_SAPLING, SPRUCE_SAPLING;

        private Material material;

        private final Map<String, Object> wateringData = new HashMap<>();
        private final Map<String, Object> poisoningData = new HashMap<>();

        static {

            for (WaterablePlant plant : WaterablePlant.values()) plant.material = Material.valueOf(plant.name());

            List<WaterablePlant> flowers = Arrays.asList(POPPY, DANDELION, BLUE_ORCHID, ALLIUM, AZURE_BLUET, RED_TULIP,
                    ORANGE_TULIP, WHITE_TULIP, PINK_TULIP, OXEYE_DAISY, CORNFLOWER, LILY_OF_THE_VALLEY);

            GRASS_BLOCK.wateringData.put(growingKey, new HashMap<>() {{
                put(Material.GRASS, 10);
                put(Material.TALL_GRASS, 6);
                flowers.forEach(flower -> put(Material.valueOf(flower.name()), 2));
            }});

            GRASS_BLOCK.poisoningData.put(newMaterialKey, new HashMap<>() {{
                put(Material.DIRT, 20);
                put(Material.MYCELIUM, 20);
            }});

            DIRT.wateringData.put(newMaterialKey, Collections.singletonMap(Material.GRASS_BLOCK, 20));
            DIRT.poisoningData.put(newMaterialKey, Collections.singletonMap(Material.MYCELIUM, 20));

            flowers.forEach(flower -> {

                flower.poisoningData.put(newMaterialKey, new HashMap<>() {{
                    put(Material.AIR, 20);
                    put(Material.RED_MUSHROOM, 10);
                    put(Material.BROWN_MUSHROOM, 10);
                    put(Material.WITHER_ROSE, 1);
                }});

                flower.poisoningData.put(requiresKey, new HashMap<>() {{
                    put(Material.RED_MUSHROOM, Material.MYCELIUM);
                    put(Material.BROWN_MUSHROOM, Material.MYCELIUM);
                }});
            });

            GRASS.wateringData.put(newMaterialKey, Collections.singletonMap(TALL_GRASS, 20));
            TALL_GRASS.poisoningData.put(newMaterialKey, Collections.singletonMap(GRASS, 20));

            FERN.wateringData.put(newMaterialKey, Collections.singletonMap(LARGE_FERN, 20));
            LARGE_FERN.poisoningData.put(newMaterialKey, Collections.singletonMap(FERN, 20));

            Arrays.asList(ACACIA_SAPLING, AZALEA, BIRCH_SAPLING, DARK_OAK_SAPLING, FLOWERING_AZALEA,
                    JUNGLE_SAPLING, OAK_SAPLING, SPRUCE_SAPLING).forEach(sapling ->
                    sapling.poisoningData.put(newMaterialKey, Collections.singletonMap(Material.DEAD_BUSH, 20)));

            AZALEA.wateringData.put(newMaterialKey, Collections.singletonMap(Material.FLOWERING_AZALEA, 20));
        }

        public Material getMaterial() {
            return material;
        }

        public Map<String, Object> getWateringData() {
            return wateringData;
        }

        public Map<String, Object> getPoisoningData() {
            return poisoningData;
        }

    }

    @EventHandler
    public void onWatering(ProjectileHitEvent e) {

        if (!(e.getEntity() instanceof ThrownPotion)) return;

        Random random = new Random();

        Block hitBlock = e.getHitBlock();
        Block plant = hitBlock.getRelative(BlockFace.UP);
        List<Block> area = getAffectedBlocks(hitBlock);
        boolean isPoisonous = !((ThrownPotion) e.getEntity()).getEffects().isEmpty();

        List<Material> waterablePlants = new ArrayList<>();
        for (WaterablePlant waterablePlant : WaterablePlant.values()) waterablePlants.add(waterablePlant.getMaterial());

        area.forEach(block -> {

            WaterablePlant waterablePlant;

            if (waterablePlants.contains(hitBlock.getType()))
                waterablePlant = WaterablePlant.valueOf(hitBlock.getType().name());
            else waterablePlant = WaterablePlant.valueOf(plant.getType().name());

            if (waterablePlants.contains(hitBlock.getType())) {

                Map<String, Object> data;

                if (isPoisonous) data = waterablePlant.getPoisoningData();
                else data = waterablePlant.getWateringData();

                data.keySet().forEach(key -> {

                    switch (key) {
                        case newMaterialKey -> {
                            Map<Material, Integer> newMaterial = (Map<Material, Integer>) data.get(key);
                            for (Material material : newMaterial.keySet()) {
                                if(random.nextInt(100) <= newMaterial.get(material)) {
                                    hitBlock.setType(material);
                                    break;
                                }
                            }
                        }
                        case growingKey -> {
                            Map<Material, Integer> growing = (Map<Material, Integer>) data.get(key);
                            for (Material material : growing.keySet()) {
                                if(random.nextInt(100) <= growing.get(material)) {
                                    plant.setType(material);
                                    break;
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    public List<Block> getAffectedBlocks(Block hitBlock) {

        List<Block> affectedBlocks = new ArrayList<>();

        for (double x = hitBlock.getLocation().getX() - 1; x < hitBlock.getLocation().getX() + 2; x++) {
            for (double z = hitBlock.getLocation().getZ() - 1; z < hitBlock.getLocation().getZ() + 2; z++) {

                Location location = new Location(hitBlock.getLocation().getWorld(), x, hitBlock.getLocation().getY(), z);

                Random random = new Random();
                int randomInt = random.nextInt(100);

                if (randomInt < 30) affectedBlocks.add(location.getBlock());
            }
        }

        return affectedBlocks;
    }
}
