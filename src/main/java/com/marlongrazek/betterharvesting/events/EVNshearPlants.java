package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.customfileconfiguration.CFC;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.*;

public class EVNshearPlants implements Listener {

    private final Main plugin;

    public EVNshearPlants(Main plugin) {
        this.plugin = plugin;
    }

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
        if (e.getHand() != EquipmentSlot.HAND) return;

        // feature disabled
        CFC settings = plugin.getCFCSettings();
        if (!settings.getBoolean("shears.enabled", true)) return;

        // no permission
        List<String> permissions = new ArrayList<>(settings.getStringList("shears.permissions"));
        if (!hasPermissionFromList(e.getPlayer(), permissions)) return;

        // block disabled
        String item = e.getClickedBlock().getType().name().toLowerCase();
        if (!settings.getBoolean("shears.blocks." + item, false)) return;

        boolean fortune_enabled = settings.getBoolean("shears.fortune");

        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        ItemStack tool = e.getItem();

        if(List.of(Material.WHEAT, Material.BEETROOTS, Material.CARROTS, Material.POTATOES, Material.COCOA,
                Material.MELON_STEM, Material.PUMPKIN_STEM,
                Material.NETHER_WART).contains(block.getType())) {

            Ageable ageable = (Ageable) block.getBlockData();
            if(ageable.getAge() < 1) return;

            e.setCancelled(true);

            ageable.setAge(ageable.getAge() - 1);
            block.setBlockData(ageable);
            player.playSound(block.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1, 1);
            player.spawnParticle(Particle.DUST_COLOR_TRANSITION, block.getLocation().add(0.5,0.5,0.5),
                    10, 0.25, 0.25,0.25, new Particle.DustTransition(Color.GREEN, Color.YELLOW, 1));

            if (player.getGameMode() != GameMode.CREATIVE && damageTool(tool.getEnchantmentLevel(Enchantment.DURABILITY))) {
                Damageable meta = (Damageable) tool.getItemMeta();
                meta.setDamage(meta.getDamage() + 1);
                tool.setItemMeta(meta);
                if (meta.getDamage() >= tool.getType().getMaxDurability()) {
                    player.getInventory().removeItem(tool);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                    player.getWorld().spawnParticle(Particle.ITEM_CRACK, player.getLocation().add(player.getLocation().getDirection()).add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0, tool);
                }
            }

        } else {

            List<Material> shearableBlocks = new ArrayList<>();
            for (ShearableBlock shearableBlock : ShearableBlock.values())
                shearableBlocks.add(shearableBlock.getMaterial());

            if (!shearableBlocks.contains(block.getType())) return;

            e.setCancelled(true);

            ShearableBlock shearableBlock = ShearableBlock.valueOf(block.getType().name());

            int multiplier = 1;
            if (fortune_enabled)
                multiplier = getDropMultiplier(tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS));

            Block belowBlock = block.getRelative(BlockFace.DOWN);
            if (isTallPlant(block, belowBlock)) belowBlock.setType(shearableBlock.getNewMaterial());
            else block.setType(shearableBlock.getNewMaterial());

            for (ItemStack drop : shearableBlock.getDrops()) {
                drop.setAmount(drop.getAmount() * multiplier);
                block.getWorld().dropItemNaturally(block.getLocation(), drop);
            }
            player.playSound(block.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1, 1);

            if (player.getGameMode() != GameMode.CREATIVE && damageTool(tool.getEnchantmentLevel(Enchantment.DURABILITY))) {
                Damageable meta = (Damageable) tool.getItemMeta();
                meta.setDamage(meta.getDamage() + 1);
                tool.setItemMeta(meta);
                if (meta.getDamage() >= tool.getType().getMaxDurability()) {
                    player.getInventory().removeItem(tool);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                    player.getWorld().spawnParticle(Particle.ITEM_CRACK, player.getLocation().add(player.getLocation().getDirection()).add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0, tool);
                }
            }
        }
    }

    public boolean damageTool(int enchantmentLevel) {

        Random random = new Random();
        int randomInt = random.nextInt(100) + 1;

        int chance = 100 - (100 / (enchantmentLevel + 1));

        return randomInt > chance;
    }

    public int getDropMultiplier(int enchantmentLevel) {

        Random random = new Random();
        int randomInt = random.nextInt(100) + 1;

        int multiplier;

        switch (enchantmentLevel) {
            case 1 -> {
                if (randomInt <= 66) multiplier = 1;
                else multiplier = 2;
            }
            case 2 -> {
                if (randomInt <= 50) multiplier = 1;
                else if (randomInt > 50 && randomInt <= 75) multiplier = 2;
                else multiplier = 3;
            }
            case 3 -> {
                if (randomInt <= 40) multiplier = 1;
                else if (randomInt > 40 && randomInt <= 60) multiplier = 2;
                else if (randomInt > 60 && randomInt <= 80) multiplier = 3;
                else multiplier = 4;
            }
            default -> multiplier = 1;
        }

        return multiplier;
    }

    public boolean hasPermissionFromList(Player player, List<String> permissions) {
        if (permissions.isEmpty()) return true;
        for (String permission : permissions) if (player.hasPermission(permission)) return true;
        return false;
    }

    public boolean isTallPlant(Block block, Block belowBlock) {
        return (block.getType() == Material.TALL_GRASS && belowBlock.getType() == Material.TALL_GRASS) ||
                (block.getType() == Material.TALL_SEAGRASS && belowBlock.getType() == Material.TALL_SEAGRASS) ||
                (block.getType() == Material.LARGE_FERN && belowBlock.getType() == Material.LARGE_FERN);
    }
}
