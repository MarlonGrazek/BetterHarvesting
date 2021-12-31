package com.marlongrazek.betterharvesting.events;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EVNshearPlants implements Listener {

    public enum ShearableBlock {
        ACACIA_SAPLING, AZALEA, BIRCH_SAPLING, DARK_OAK_SAPLING, FLOWERING_AZALEA, JUNGLE_SAPLING, OAK_SAPLING,
        SPRUCE_SAPLING, TALL_GRASS, TALL_SEAGRASS, LARGE_FERN;

        private Material material;
        private Material newMaterial = Material.AIR;
        private List<ItemStack> drops = new ArrayList<>();

        static {

            for (ShearableBlock block : ShearableBlock.values()) block.material = Material.valueOf(block.name());

            ACACIA_SAPLING.drops = Collections.singletonList(new ItemStack(Material.ACACIA_LEAVES));
            AZALEA.drops = Collections.singletonList(new ItemStack(Material.AZALEA_LEAVES));
            BIRCH_SAPLING.drops = Collections.singletonList(new ItemStack(Material.BIRCH_LEAVES));
            DARK_OAK_SAPLING.drops = Collections.singletonList(new ItemStack(Material.DARK_OAK_LEAVES));
            FLOWERING_AZALEA.drops = Collections.singletonList(new ItemStack(Material.FLOWERING_AZALEA_LEAVES));
            JUNGLE_SAPLING.drops = Collections.singletonList(new ItemStack(Material.JUNGLE_LEAVES));
            OAK_SAPLING.drops = Collections.singletonList(new ItemStack(Material.OAK_LEAVES));
            SPRUCE_SAPLING.drops = Collections.singletonList(new ItemStack(Material.SPRUCE_LEAVES));

            Arrays.asList(ACACIA_SAPLING, AZALEA, BIRCH_SAPLING, DARK_OAK_SAPLING, FLOWERING_AZALEA, JUNGLE_SAPLING,
                    OAK_SAPLING, SPRUCE_SAPLING).forEach(sapling -> sapling.newMaterial = Material.DEAD_BUSH);

            TALL_GRASS.newMaterial = Material.GRASS;
            TALL_GRASS.drops = Collections.singletonList(new ItemStack(Material.GRASS));

            TALL_SEAGRASS.newMaterial = Material.SEAGRASS;
            TALL_SEAGRASS.drops = Collections.singletonList(new ItemStack(Material.SEAGRASS));

            LARGE_FERN.newMaterial = Material.FERN;
            LARGE_FERN.drops = Collections.singletonList(new ItemStack(Material.FERN));
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
    public void onShear(PlayerInteractEvent e) {

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getItem() == null) return;
        if (e.getItem().getType() != Material.SHEARS) return;
        if (e.getHand() == EquipmentSlot.HAND && e.getHand() == EquipmentSlot.OFF_HAND) return;

        Player player = e.getPlayer();
        Block block = e.getClickedBlock();

        List<Material> shearableBlocks = new ArrayList<>();
        for (ShearableBlock shearableBlock : ShearableBlock.values()) shearableBlocks.add(shearableBlock.getMaterial());

        if (!shearableBlocks.contains(block.getType())) return;

        ShearableBlock shearableBlock = ShearableBlock.valueOf(block.getType().name());

        e.setCancelled(true);

        Block belowBlock = block.getRelative(BlockFace.DOWN);
        if ((block.getType() == Material.TALL_GRASS && belowBlock.getType() == Material.TALL_GRASS) ||
                (block.getType() == Material.TALL_SEAGRASS && belowBlock.getType() == Material.TALL_SEAGRASS) ||
                (block.getType() == Material.LARGE_FERN && belowBlock.getType() == Material.LARGE_FERN))
            belowBlock.setType(shearableBlock.getNewMaterial());
        else block.setType(shearableBlock.getNewMaterial());

        player.playSound(block.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1, 1);
        shearableBlock.getDrops().forEach(drop -> block.getWorld().dropItemNaturally(block.getLocation(), drop));
    }
}
