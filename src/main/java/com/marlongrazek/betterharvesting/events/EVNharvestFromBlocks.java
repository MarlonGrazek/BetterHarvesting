package com.marlongrazek.betterharvesting.events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class EVNharvestFromBlocks implements Listener {

    public enum leaves {
        AZALEA_LEAVES, ACACIA_LEAVES, BIRCH_LEAVES, DARK_OAK_LEAVES, FLOWERING_AZALEA_LEAVES, JUNGLE_LEAVES, OAK_LEAVES,
        SPRUCE_LEAVES;

        private Material material;
        private ItemStack drop;

        static {
            AZALEA_LEAVES.material = Material.AZALEA_LEAVES;
            ACACIA_LEAVES.material = Material.ACACIA_LEAVES;
            BIRCH_LEAVES.material = Material.BIRCH_LEAVES;
            DARK_OAK_LEAVES.material = Material.DARK_OAK_LEAVES;
            FLOWERING_AZALEA_LEAVES.material = Material.FLOWERING_AZALEA_LEAVES;
            JUNGLE_LEAVES.material = Material.JUNGLE_LEAVES;
            OAK_LEAVES.material = Material.OAK_LEAVES;
            SPRUCE_LEAVES.material = Material.SPRUCE_LEAVES;

            AZALEA_LEAVES.drop = new ItemStack(Material.AZALEA);
            ACACIA_LEAVES.drop = new ItemStack(Material.ACACIA_SAPLING);
            BIRCH_LEAVES.drop = new ItemStack(Material.BIRCH_SAPLING);
            DARK_OAK_LEAVES.drop = new ItemStack(Material.DARK_OAK_SAPLING);
            FLOWERING_AZALEA_LEAVES.drop = new ItemStack(Material.FLOWERING_AZALEA);
            JUNGLE_LEAVES.drop = new ItemStack(Material.JUNGLE_SAPLING);
            OAK_LEAVES.drop = new ItemStack(Material.OAK_SAPLING);
            SPRUCE_LEAVES.drop = new ItemStack(Material.SPRUCE_SAPLING);
        }

        public Material getMaterial() {
            return material;
        }

        public ItemStack getDrop() {
            return drop;
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {

        ItemStack tool = e.getPlayer().getInventory().getItemInMainHand();

        ArrayList<Material> hoes = new ArrayList<>(Arrays.asList(Material.WOODEN_HOE, Material.STONE_HOE,
                Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE));

        ArrayList<Material> plants = new ArrayList<>(Arrays.asList(Material.GRASS, Material.TALL_GRASS,
                Material.FERN, Material.LARGE_FERN));

        if (!hoes.contains(tool.getType())) return;

        Random random = new Random();
        int randomInt = random.nextInt(100);
        Location location = e.getBlock().getLocation();

        int amount = 1;
        if (tool.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
            if (randomInt < 60) amount += (tool.getEnchantmentLevel(Enchantment.LUCK) + 1);
            else if (randomInt >= 60 && randomInt < 70)
                amount *= (tool.getEnchantmentLevel(Enchantment.LUCK) + 1);
        }

        // grass
        if (plants.contains(e.getBlock().getType())) {

            e.setDropItems(false);

            if (randomInt <= 40) dropItem(location, new ItemStack(Material.WHEAT_SEEDS, amount));
            else if (randomInt > 40 && randomInt <= 50)
                dropItem(location, new ItemStack(Material.WHEAT_SEEDS, 2 * amount));
            else if (randomInt > 50 && randomInt <= 56)
                dropItem(location, new ItemStack(Material.BEETROOT_SEEDS, amount));
            else if (randomInt > 56 && randomInt <= 58)
                dropItem(location, new ItemStack(Material.CARROT, amount));
            else if (randomInt > 58 && randomInt <= 60)
                dropItem(location, new ItemStack(Material.POTATO, amount));
            else if (randomInt > 60 && randomInt <= 65)
                dropItem(location, new ItemStack(Material.STICK, amount));
            if ((randomInt > 20 && randomInt <= 22) || (randomInt > 40 && randomInt <= 42) ||
                    (randomInt > 50 && randomInt <= 52) || (randomInt > 60 && randomInt <= 62))
                location.getWorld().spawn(location, ExperienceOrb.class).setExperience(amount);
        }

        // leaves
        else if (e.getBlock().getBlockData() instanceof Leaves) {

            e.setDropItems(false);

            if (randomInt <= 40) dropItem(location, new ItemStack(Material.STICK, amount));
            else if (randomInt > 40 && randomInt < 80) {

                leaves leaf = leaves.valueOf(e.getBlock().getType().name());
                dropItem(location, leaf.getDrop());
            } else if (randomInt > 80 && randomInt < 90) {
                if (e.getBlock().getType() == Material.OAK_LEAVES) dropItem(location, new ItemStack(Material.APPLE));
            }
            if ((randomInt > 20 && randomInt <= 22) || (randomInt > 40 && randomInt <= 42) || (randomInt > 80 && randomInt <= 82))
                location.getWorld().spawn(location, ExperienceOrb.class).setExperience(amount);
        }
    }

    public void dropItem(Location location, ItemStack item) {
        location.getWorld().dropItemNaturally(location, item);
    }
}
