package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.datafile.DataFile;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Candle;
import org.bukkit.block.data.type.SeaPickle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EVNmodifyBlocks implements Listener {

    @EventHandler
    public void onClick(PlayerInteractEvent e) {

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = e.getClickedBlock();
        Player player = e.getPlayer();

        DataFile settings = Main.getDataFile("settings");
        if (!settings.getBoolean("modify.enabled", true)) return;

        List<String> permissions = new ArrayList<>(settings.getStringList("modify.permissions"));

        String category = "";
        String item = e.getClickedBlock().getType().name().toLowerCase();

        if (List.of(Material.WHEAT, Material.BEETROOTS, Material.CARROTS, Material.POTATOES, Material.COCOA, Material.MELON_STEM,
                Material.PUMPKIN_STEM).contains(e.getClickedBlock().getType())) category = ".crops";

        switch (e.getClickedBlock().getType()) {
            case WHEAT -> item = "wheat_seeds";
            case BEETROOTS -> item = "beetroot_seeds";
            case CARROTS -> item = "carrot";
            case POTATOES -> item = "potato";
            case COCOA -> item = "cocoa_beans";
            case MELON_STEM -> item = "melon_seeds";
            case PUMPKIN_STEM -> item = "pumpkin_seeds";
        }

        if (!settings.getBoolean("modify.enabled", true)) return;
        if (!category.isEmpty()) if (!settings.getBoolean("modify" + category + ".enabled", true)) return;
        if (!settings.getBoolean("modify" + category + "." + item + ".enabled", true)) return;

        if (!category.isEmpty()) permissions.addAll(settings.getStringList("modify" + category + ".permissions"));
        permissions.addAll(settings.getStringList("modify" + category + "." + item + ".permissions"));

        boolean hasPermission = false;
        if (!permissions.isEmpty()) {
            for (String permission : permissions) {
                if (e.getPlayer().hasPermission(permission)) {
                    hasPermission = true;
                    break;
                }
            }
        } else hasPermission = true;

        if (!hasPermission) return;

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
        }

        // crops
        else if (block.getBlockData() instanceof Ageable) {

            if (e.getItem() == null) return;
            if (!List.of(Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE,
                    Material.DIAMOND_HOE, Material.NETHERITE_HOE).contains(e.getItem().getType())) return;
            if (block.getType() == Material.SWEET_BERRY_BUSH) return;

            Ageable crop = (Ageable) e.getClickedBlock().getBlockData();
            if (crop.getAge() != crop.getMaximumAge()) return;

            e.setCancelled(true);

            ItemStack tool = e.getItem();
            int multiplier = getDropMultiplier(tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS));

            ArrayList<ItemStack> drops = new ArrayList<>(e.getClickedBlock().getDrops());

            boolean removedSeed = false;
            for (ItemStack drop : drops) {

                if (drop.getType() == Material.AIR) drops.remove(drop);

                switch (drop.getType()) {
                    case POTATO, CARROT, BEETROOT_SEEDS, WHEAT_SEEDS, NETHER_WART, COCOA_BEANS, MELON_SEEDS, PUMPKIN_SEEDS -> {
                        if (!removedSeed) {
                            drop.setAmount(drop.getAmount() - 1);
                            removedSeed = true;
                        }
                    }
                }

                if (drop != null && drop.getAmount() > 0 && drop.getType() != Material.AIR) {
                    drop.setAmount(drop.getAmount() * multiplier);
                    e.getClickedBlock().getWorld().dropItemNaturally(e.getClickedBlock().getLocation(), drop);
                }
            }

            crop.setAge(0);
            block.setBlockData(crop);

            if (player.getGameMode() != GameMode.CREATIVE && damageTool(tool.getEnchantmentLevel(Enchantment.DURABILITY))) {
                Damageable meta = (Damageable) tool.getItemMeta();
                meta.setDamage(meta.getDamage() + 1);
                tool.setItemMeta(meta);
            }

            player.playSound(player.getLocation(), Sound.BLOCK_CROP_BREAK, 0.9F, 1);
        }
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
}
