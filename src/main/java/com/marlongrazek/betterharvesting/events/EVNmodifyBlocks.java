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

public class EVNmodifyBlocks implements Listener {

    private final Main plugin;

    public EVNmodifyBlocks(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {

        // check action
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        // check hand
        if (e.getHand() != EquipmentSlot.HAND) return;

        Block block = e.getClickedBlock();
        ItemStack tool = e.getItem();
        Player player = e.getPlayer();

        CFC settings = plugin.getCFCSettings();

        if (block.getBlockData() instanceof Ageable) {

            boolean enabled = settings.getBoolean("harvesting.enabled", true);
            boolean requires_hoe = settings.getBoolean("harvesting.requires_tool", true);
            boolean fortune = settings.getBoolean("harvesting.fortune", true);
            boolean quick_harvest = settings.getBoolean("harvesting.quick", false);

            // check feature enabled
            if (!enabled) return;

            // check crop enabled
            if (!settings.getBoolean("harvesting.crops." + block.getType().name().toLowerCase(), false)) return;

            // no permission
            if (!settings.getStringList("harvesting.permissions").isEmpty() &&
                    settings.getStringList("harvesting.permissions").stream().noneMatch(player::hasPermission)) return;

            // check item
            List<Material> hoes = List.of(Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE,
                    Material.DIAMOND_HOE, Material.NETHERITE_HOE);
            if (requires_hoe && (tool == null || !hoes.contains(tool.getType()))) return;
            else if (!requires_hoe && tool != null) return;

            // check sweetberry
            if (block.getType() == Material.SWEET_BERRY_BUSH) return;

            // check max age
            Ageable crop = (Ageable) e.getClickedBlock().getBlockData();
            if (crop.getAge() != crop.getMaximumAge()) return;

            e.setCancelled(true);

            // define multiplier
            int multiplier = 1;
            if (tool != null && fortune)
                multiplier = getDropMultiplier(tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS));

            if (quick_harvest) {

                List<Block> blocks = getConnectedBlocks(block, 100);

                for (Block b : blocks) {

                    ArrayList<ItemStack> drops = new ArrayList<>(b.getDrops());
                    drops.removeIf(drop -> drop == null || drop.getType() == Material.AIR);

                    // drop items
                    boolean removedSeed = false;
                    for (ItemStack drop : drops) {
                        if (List.of(Material.POTATO, Material.CARROT, Material.BEETROOT_SEEDS, Material.WHEAT_SEEDS, Material.NETHER_WART,
                                Material.COCOA_BEANS, Material.MELON_SEEDS, Material.PUMPKIN_SEEDS).contains(drop.getType())) {
                            if (!removedSeed) {
                                drop.setAmount(drop.getAmount() - 1);
                                removedSeed = true;
                            }
                        }

                        if (drop == null || drop.getAmount() <= 0 || drop.getType() == Material.AIR) continue;
                        drop.setAmount(drop.getAmount() * multiplier);
                        b.getWorld().dropItemNaturally(b.getLocation(), drop);
                    }

                    Ageable ageable = (Ageable) b.getBlockData();
                    ageable.setAge(0);
                    b.setBlockData(ageable);

                    // handle damage
                    if (player.getGameMode() != GameMode.CREATIVE && tool != null && damageTool(tool.getEnchantmentLevel(Enchantment.DURABILITY))) {
                        Damageable meta = (Damageable) tool.getItemMeta();
                        meta.setDamage(meta.getDamage() + 1);
                        tool.setItemMeta(meta);
                        if (meta.getDamage() >= tool.getType().getMaxDurability()) {
                            player.getInventory().removeItem(tool);
                            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                            player.getWorld().spawnParticle(Particle.ITEM_CRACK, player.getLocation().add(player.getLocation().getDirection()).add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0, tool);
                            break;
                        }
                    }

                    player.getWorld().playSound(b.getLocation(), Sound.BLOCK_CROP_BREAK, 0.9F, 1);
                }

            } else {

                ArrayList<ItemStack> drops = new ArrayList<>(e.getClickedBlock().getDrops());
                drops.removeIf(drop -> drop == null || drop.getType() == Material.AIR);

                // drop items
                boolean removedSeed = false;
                for (ItemStack drop : drops) {
                    if (List.of(Material.POTATO, Material.CARROT, Material.BEETROOT_SEEDS, Material.WHEAT_SEEDS, Material.NETHER_WART,
                            Material.COCOA_BEANS, Material.MELON_SEEDS, Material.PUMPKIN_SEEDS).contains(drop.getType())) {
                        if (!removedSeed) {
                            drop.setAmount(drop.getAmount() - 1);
                            removedSeed = true;
                        }
                    }

                    if (drop == null || drop.getAmount() <= 0 || drop.getType() == Material.AIR) continue;
                    drop.setAmount(drop.getAmount() * multiplier);
                    e.getClickedBlock().getWorld().dropItemNaturally(e.getClickedBlock().getLocation(), drop);
                }

                // aply change
                crop.setAge(0);
                block.setBlockData(crop);

                // handle damage
                if (player.getGameMode() != GameMode.CREATIVE && tool != null && damageTool(tool.getEnchantmentLevel(Enchantment.DURABILITY))) {
                    Damageable meta = (Damageable) tool.getItemMeta();
                    meta.setDamage(meta.getDamage() + 1);
                    tool.setItemMeta(meta);
                    if (meta.getDamage() >= tool.getType().getMaxDurability()) {
                        player.getInventory().removeItem(tool);
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                        player.getWorld().spawnParticle(Particle.ITEM_CRACK, player.getLocation().add(player.getLocation().getDirection()).add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0, tool);
                    }
                }

                player.getWorld().playSound(block.getLocation(), Sound.BLOCK_CROP_BREAK, 0.9F, 1);
            }
        }

        /*
        // feature disabled
        if (!settings.getBoolean("right_clicking.enabled", true)) return;

        // no permissions
        List<String> permissions = new ArrayList<>(settings.getStringList("right_clicking.permissions"));
        if(!hasPermissionFromList(player, permissions)) return;

        // block disabled
        String item = e.getClickedBlock().getType().name().toLowerCase();
        if (!settings.getBoolean("right_clicking.blocks." + item, true)) return;

        // candles and sea pickles
        if (block.getBlockData() instanceof SeaPickle || block.getBlockData() instanceof Candle) {

            if (e.getItem() != null) return;
            if (e.getHand() != EquipmentSlot.HAND) return;

            e.setCancelled(true);

            if (player.getGameMode() != GameMode.CREATIVE || !player.getInventory().contains(block.getType()))
                player.getInventory().addItem(new ItemStack(block.getType(), 1));

            // sea pickle
            if (block.getBlockData() instanceof SeaPickle) {

                SeaPickle pickle = (SeaPickle) block.getBlockData();
                int amount = pickle.getPickles() - 1;
                if (amount > 0) {
                    pickle.setPickles(amount);
                    block.setBlockData(pickle);
                } else block.setType(Material.AIR);

                player.playSound(player.getLocation(), Sound.BLOCK_SLIME_BLOCK_BREAK, 1, 1);
            }

            // candle
            else if (block.getBlockData() instanceof Candle) {

                Candle candle = (Candle) block.getBlockData();
                int amount = candle.getCandles() - 1;
                if (amount > 0) {
                    candle.setCandles(amount);
                    block.setBlockData(candle);
                } else block.setType(Material.AIR);

                player.playSound(player.getLocation(), Sound.BLOCK_CANDLE_BREAK, 1, 1);
            }
        }

        // pumpkin to jack o lantern
        else if (block.getType() == Material.CARVED_PUMPKIN) {

            if (e.getItem() == null || e.getItem().getType() != Material.TORCH) return;

            e.setCancelled(true);

            BlockFace face = ((Directional) block.getBlockData()).getFacing();
            block.setType(Material.JACK_O_LANTERN);

            Directional directional = (Directional) block.getBlockData();
            directional.setFacing(face);
            block.setBlockData(directional);

            if (player.getGameMode() != GameMode.CREATIVE) e.getItem().setAmount(e.getItem().getAmount() - 1);
        }

        // jack o lantern to pumpkin
        else if (block.getType() == Material.JACK_O_LANTERN) {

            if (e.getItem() != null) return;

            e.setCancelled(true);

            BlockFace face = ((Directional) block.getBlockData()).getFacing();
            block.setType(Material.CARVED_PUMPKIN);

            Directional directional = (Directional) block.getBlockData();
            directional.setFacing(face);
            block.setBlockData(directional);

            player.playSound(player.getLocation(), Sound.BLOCK_WOOD_PLACE, 1, 0.8F);
            if (player.getGameMode() != GameMode.CREATIVE || !player.getInventory().contains(new ItemStack(Material.TORCH)))
                player.getInventory().addItem(new ItemStack(Material.TORCH));
        }*/
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

    public boolean damageTool(int enchantmentLevel) {

        Random random = new Random();
        int randomInt = random.nextInt(100) + 1;

        int chance = 100 - (100 / (enchantmentLevel + 1));

        return randomInt > chance;
    }

    public List<Block> getConnectedBlocks(Block start, int limit) {
        List<Block> connectedBlocks = new ArrayList<>();
        Queue<Block> queue = new LinkedList<>();
        Set<Block> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);
        Material type = start.getType();
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST};
        while (!queue.isEmpty() && connectedBlocks.size() < limit) {
            Block current = queue.poll();
            if (isMaxAgeCrop(current)) {
                connectedBlocks.add(current);
                for (BlockFace face : faces) {
                    Block relative = current.getRelative(face);
                    if (relative.getType() == type && !visited.contains(relative) && isMaxAgeCrop(relative)) {
                        queue.add(relative);
                        visited.add(relative);
                    }
                }
            }
        }
        return connectedBlocks;
    }

    private boolean isMaxAgeCrop(Block block) {
        if (block.getBlockData() instanceof Ageable) {
            Ageable ageable = (Ageable) block.getBlockData();
            return ageable.getAge() == ageable.getMaximumAge();
        }
        return false;
    }
}
