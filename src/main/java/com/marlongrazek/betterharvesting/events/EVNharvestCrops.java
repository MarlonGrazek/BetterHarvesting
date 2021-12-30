package com.marlongrazek.betterharvesting.events;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Candle;
import org.bukkit.block.data.type.SeaPickle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class EVNharvestCrops implements Listener {

    @EventHandler
    public void onClick(PlayerInteractEvent e) {

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = e.getClickedBlock();
        Player player = e.getPlayer();

        // candles and sea pickles
        if (block.getBlockData() instanceof SeaPickle || block.getBlockData() instanceof Candle) {

            if (e.getItem() != null) return;
            if (e.getHand() != EquipmentSlot.HAND) return;

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

            if (player.getGameMode() != GameMode.CREATIVE || !player.getInventory().contains(block.getType()))
                player.getInventory().addItem(new ItemStack(block.getType()));
        }

        // crops
        else if (block.getBlockData() instanceof Ageable) {

            Ageable crop = (Ageable) e.getClickedBlock().getBlockData();
            if (crop.getAge() != crop.getMaximumAge()) return;

            e.setCancelled(true);

            ArrayList<ItemStack> drops = new ArrayList<>(e.getClickedBlock().getDrops());

            boolean removedSeed = false;
            for (ItemStack drop : drops) {
                if ((drop.getType() == Material.POTATO || drop.getType() == Material.CARROT || drop.getType() == Material.BEETROOT_SEEDS
                        || drop.getType() == Material.WHEAT_SEEDS || drop.getType() == Material.NETHER_WART) && !removedSeed) {
                    drop.setAmount(drop.getAmount() - 1);
                    removedSeed = true;
                }

                if (drop.getAmount() > 0)
                    e.getClickedBlock().getWorld().dropItemNaturally(e.getClickedBlock().getLocation(), drop);
            }
            e.getClickedBlock().setType(e.getClickedBlock().getType());
        }
    }
}
