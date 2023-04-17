//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.datafile.DataFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
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

public class EVNmodifyBlocks implements Listener {
    private final Main plugin;

    public EVNmodifyBlocks(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = e.getClickedBlock();
            Player player = e.getPlayer();
            DataFile settings = this.plugin.getDataFile("settings");
            if (block.getBlockData() instanceof Ageable) {
                boolean enabled = settings.getBoolean("crop_harvesting.enabled", true);
                boolean no_item_enabled = settings.getBoolean("crop_harvesting.tools.no_tool", true);
                boolean hoe_enabled = settings.getBoolean("crop_harvesting.tools.hoe", true);
                boolean fortune_enabled = settings.getBoolean("crop_harvesting.fortune", true);
                List<String> harvesting_permissions = settings.getStringList("crop_harvesting.permissions");
                List<Material> hoes = List.of(Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE);
                if (e.getHand() == EquipmentSlot.HAND) {
                    if (enabled) {
                        if (e.getItem() == null) {
                            if (!no_item_enabled) {
                                return;
                            }
                        } else {
                            if (e.getItem() == null || !hoes.contains(e.getItem().getType())) {
                                return;
                            }

                            if (!hoe_enabled) {
                                return;
                            }
                        }

                        if (this.hasPermissionFromList(player, harvesting_permissions)) {
                            String clicked;
                            switch(e.getClickedBlock().getType()) {
                                case WHEAT:
                                    clicked = "wheat_seeds";
                                    break;
                                case BEETROOTS:
                                    clicked = "beetroot_seeds";
                                    break;
                                case CARROTS:
                                    clicked = "carrot";
                                    break;
                                case POTATOES:
                                    clicked = "potato";
                                    break;
                                case COCOA:
                                    clicked = "cocoa_beans";
                                    break;
                                case MELON_STEM:
                                    clicked = "melon_seeds";
                                    break;
                                case PUMPKIN_STEM:
                                    clicked = "pumpkin_seeds";
                                    break;
                                default:
                                    return;
                            }

                            if (settings.getBoolean("crop_harvesting.crops." + clicked, false)) {
                                if (block.getType() != Material.SWEET_BERRY_BUSH) {
                                    Ageable crop = (Ageable)e.getClickedBlock().getBlockData();
                                    if (crop.getAge() == crop.getMaximumAge()) {
                                        e.setCancelled(true);
                                        ItemStack tool = e.getItem();
                                        int multiplier = 1;
                                        if (tool != null && fortune_enabled) {
                                            multiplier = this.getDropMultiplier(tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS));
                                        }

                                        ArrayList<ItemStack> drops = new ArrayList(e.getClickedBlock().getDrops());
                                        drops.removeIf((dropx) -> {
                                            return dropx == null || dropx.getType() == Material.AIR;
                                        });
                                        boolean removedSeed = false;
                                        Iterator var17 = drops.iterator();

                                        while(var17.hasNext()) {
                                            ItemStack drop = (ItemStack)var17.next();
                                            switch(drop.getType()) {
                                                case POTATO:
                                                case CARROT:
                                                case BEETROOT_SEEDS:
                                                case WHEAT_SEEDS:
                                                case NETHER_WART:
                                                case COCOA_BEANS:
                                                case MELON_SEEDS:
                                                case PUMPKIN_SEEDS:
                                                    if (!removedSeed) {
                                                        drop.setAmount(drop.getAmount() - 1);
                                                        removedSeed = true;
                                                    }
                                            }

                                            if (drop != null && drop.getAmount() > 0 && drop.getType() != Material.AIR) {
                                                drop.setAmount(drop.getAmount() * multiplier);
                                                e.getClickedBlock().getWorld().dropItemNaturally(e.getClickedBlock().getLocation(), drop);
                                            }
                                        }

                                        crop.setAge(0);
                                        block.setBlockData(crop);
                                        if (player.getGameMode() != GameMode.CREATIVE && tool != null && this.damageTool(tool.getEnchantmentLevel(Enchantment.DURABILITY))) {
                                            Damageable meta = (Damageable)tool.getItemMeta();
                                            meta.setDamage(meta.getDamage() + 1);
                                            tool.setItemMeta(meta);
                                            if (meta.getDamage() >= tool.getType().getMaxDurability()) {
                                                player.getInventory().removeItem(tool);
                                                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
                                                player.getWorld().spawnParticle(Particle.ITEM_CRACK, player.getLocation().add(player.getLocation().getDirection()).add(0.0D, 1.0D, 0.0D), 10, 0.3D, 0.5D, 0.3D, 0.0D, tool);
                                            }
                                        }

                                        player.playSound(player.getLocation(), Sound.BLOCK_CROP_BREAK, 0.9F, 1.0F);
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (settings.getBoolean("right_clicking.enabled", true)) {
                List<String> permissions = new ArrayList(settings.getStringList("right_clicking.permissions"));
                if (this.hasPermissionFromList(player, permissions)) {
                    String item = e.getClickedBlock().getType().name().toLowerCase();
                    if (settings.getBoolean("right_clicking.blocks." + item, true)) {
                        if (!(block.getBlockData() instanceof SeaPickle) && !(block.getBlockData() instanceof Candle)) {
                            BlockFace face;
                            Directional directional;
                            if (block.getType() == Material.CARVED_PUMPKIN) {
                                if (e.getItem() == null || e.getItem().getType() != Material.TORCH) {
                                    return;
                                }

                                e.setCancelled(true);
                                face = ((Directional)block.getBlockData()).getFacing();
                                block.setType(Material.JACK_O_LANTERN);
                                directional = (Directional)block.getBlockData();
                                directional.setFacing(face);
                                block.setBlockData(directional);
                                if (player.getGameMode() != GameMode.CREATIVE) {
                                    e.getItem().setAmount(e.getItem().getAmount() - 1);
                                }
                            } else if (block.getType() == Material.JACK_O_LANTERN) {
                                if (e.getItem() != null) {
                                    return;
                                }

                                e.setCancelled(true);
                                face = ((Directional)block.getBlockData()).getFacing();
                                block.setType(Material.CARVED_PUMPKIN);
                                directional = (Directional)block.getBlockData();
                                directional.setFacing(face);
                                block.setBlockData(directional);
                                player.playSound(player.getLocation(), Sound.BLOCK_WOOD_PLACE, 1.0F, 0.8F);
                                if (player.getGameMode() != GameMode.CREATIVE || !player.getInventory().contains(new ItemStack(Material.TORCH))) {
                                    player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.TORCH)});
                                }
                            }
                        } else {
                            if (e.getItem() != null) {
                                return;
                            }

                            if (e.getHand() != EquipmentSlot.HAND) {
                                return;
                            }

                            e.setCancelled(true);
                            if (player.getGameMode() != GameMode.CREATIVE || !player.getInventory().contains(block.getType())) {
                                player.getInventory().addItem(new ItemStack[]{new ItemStack(block.getType(), 1)});
                            }

                            int amount;
                            if (block.getBlockData() instanceof SeaPickle) {
                                SeaPickle pickle = (SeaPickle)block.getBlockData();
                                amount = pickle.getPickles() - 1;
                                if (amount > 0) {
                                    pickle.setPickles(amount);
                                    block.setBlockData(pickle);
                                } else {
                                    block.setType(Material.AIR);
                                }

                                player.playSound(player.getLocation(), Sound.BLOCK_SLIME_BLOCK_BREAK, 1.0F, 1.0F);
                            } else if (block.getBlockData() instanceof Candle) {
                                Candle candle = (Candle)block.getBlockData();
                                amount = candle.getCandles() - 1;
                                if (amount > 0) {
                                    candle.setCandles(amount);
                                    block.setBlockData(candle);
                                } else {
                                    block.setType(Material.AIR);
                                }

                                player.playSound(player.getLocation(), Sound.BLOCK_CANDLE_BREAK, 1.0F, 1.0F);
                            }
                        }

                    }
                }
            }
        }
    }

    public int getDropMultiplier(int enchantmentLevel) {
        Random random = new Random();
        int randomInt = random.nextInt(100) + 1;
        byte multiplier;
        switch(enchantmentLevel) {
            case 1:
                if (randomInt <= 66) {
                    multiplier = 1;
                } else {
                    multiplier = 2;
                }
                break;
            case 2:
                if (randomInt <= 50) {
                    multiplier = 1;
                } else if (randomInt > 50 && randomInt <= 75) {
                    multiplier = 2;
                } else {
                    multiplier = 3;
                }
                break;
            case 3:
                if (randomInt <= 40) {
                    multiplier = 1;
                } else if (randomInt > 40 && randomInt <= 60) {
                    multiplier = 2;
                } else if (randomInt > 60 && randomInt <= 80) {
                    multiplier = 3;
                } else {
                    multiplier = 4;
                }
                break;
            default:
                multiplier = 1;
        }

        return multiplier;
    }

    public boolean damageTool(int enchantmentLevel) {
        Random random = new Random();
        int randomInt = random.nextInt(100) + 1;
        int chance = 100 - 100 / (enchantmentLevel + 1);
        return randomInt > chance;
    }

    public boolean hasPermissionFromList(Player player, List<String> permissions) {
        if (permissions.isEmpty()) {
            return true;
        } else {
            Iterator var3 = permissions.iterator();

            String permission;
            do {
                if (!var3.hasNext()) {
                    return false;
                }

                permission = (String)var3.next();
            } while(!player.hasPermission(permission));

            return true;
        }
    }
}
