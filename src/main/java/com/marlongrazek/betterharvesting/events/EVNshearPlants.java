//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.datafile.DataFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class EVNshearPlants implements Listener {
    private final Main plugin;

    public EVNshearPlants(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShear(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getItem() != null) {
                if (e.getItem().getType() == Material.SHEARS) {
                    if (e.getHand() == EquipmentSlot.HAND) {
                        DataFile settings = this.plugin.getDataFile("settings");
                        if (settings.getBoolean("shearing.enabled", true)) {
                            List<String> permissions = new ArrayList(settings.getStringList("shearing.permissions"));
                            if (this.hasPermissionFromList(e.getPlayer(), permissions)) {
                                String item = e.getClickedBlock().getType().name().toLowerCase();
                                if (settings.getBoolean("shearing.blocks." + item, false)) {
                                    e.setCancelled(true);
                                    boolean fortune_enabled = settings.getBoolean("shearing.fortune");
                                    Player player = e.getPlayer();
                                    Block block = e.getClickedBlock();
                                    ItemStack tool = e.getItem();
                                    List<Material> shearableBlocks = new ArrayList();
                                    EVNshearPlants.ShearableBlock[] var10 = EVNshearPlants.ShearableBlock.values();
                                    int multiplier = var10.length;

                                    for(int var12 = 0; var12 < multiplier; ++var12) {
                                        EVNshearPlants.ShearableBlock shearableBlock = var10[var12];
                                        shearableBlocks.add(shearableBlock.getMaterial());
                                    }

                                    if (shearableBlocks.contains(block.getType())) {
                                        EVNshearPlants.ShearableBlock shearableBlock = EVNshearPlants.ShearableBlock.valueOf(block.getType().name());
                                        multiplier = 1;
                                        if (fortune_enabled) {
                                            multiplier = this.getDropMultiplier(tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS));
                                        }

                                        Block belowBlock = block.getRelative(BlockFace.DOWN);
                                        if (this.isTallPlant(block, belowBlock)) {
                                            belowBlock.setType(shearableBlock.getNewMaterial());
                                        } else {
                                            block.setType(shearableBlock.getNewMaterial());
                                        }

                                        Iterator var17 = shearableBlock.getDrops().iterator();

                                        while(var17.hasNext()) {
                                            ItemStack drop = (ItemStack)var17.next();
                                            drop.setAmount(drop.getAmount() * multiplier);
                                            block.getWorld().dropItemNaturally(block.getLocation(), drop);
                                        }

                                        player.playSound(block.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1.0F, 1.0F);
                                        if (player.getGameMode() != GameMode.CREATIVE && this.damageTool(tool.getEnchantmentLevel(Enchantment.DURABILITY))) {
                                            Damageable meta = (Damageable)tool.getItemMeta();
                                            meta.setDamage(meta.getDamage() + 1);
                                            tool.setItemMeta(meta);
                                            if (meta.getDamage() >= tool.getType().getMaxDurability()) {
                                                player.getInventory().removeItem(tool);
                                                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
                                                player.getWorld().spawnParticle(Particle.ITEM_CRACK, player.getLocation().add(player.getLocation().getDirection()).add(0.0D, 1.0D, 0.0D), 10, 0.3D, 0.5D, 0.3D, 0.0D, tool);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean damageTool(int enchantmentLevel) {
        Random random = new Random();
        int randomInt = random.nextInt(100) + 1;
        int chance = 100 - 100 / (enchantmentLevel + 1);
        return randomInt > chance;
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

    public boolean isTallPlant(Block block, Block belowBlock) {
        return block.getType() == Material.TALL_GRASS && belowBlock.getType() == Material.TALL_GRASS || block.getType() == Material.TALL_SEAGRASS && belowBlock.getType() == Material.TALL_SEAGRASS || block.getType() == Material.LARGE_FERN && belowBlock.getType() == Material.LARGE_FERN;
    }

    public static enum ShearableBlock {
        ACACIA_SAPLING,
        AZALEA,
        BIRCH_SAPLING,
        DARK_OAK_SAPLING,
        FLOWERING_AZALEA,
        JUNGLE_SAPLING,
        OAK_SAPLING,
        SPRUCE_SAPLING,
        TALL_GRASS,
        TALL_SEAGRASS,
        LARGE_FERN;

        private Material material;
        private Material newMaterial;
        private List<ItemStack> drops;

        private ShearableBlock() {
            this.newMaterial = Material.AIR;
            this.drops = new ArrayList();
        }

        public Material getMaterial() {
            return this.material;
        }

        public Material getNewMaterial() {
            return this.newMaterial;
        }

        public List<ItemStack> getDrops() {
            return this.drops;
        }

        static {
            EVNshearPlants.ShearableBlock[] var0 = values();
            int var1 = var0.length;

            for(int var2 = 0; var2 < var1; ++var2) {
                EVNshearPlants.ShearableBlock block = var0[var2];
                block.material = Material.valueOf(block.name());
            }

            ACACIA_SAPLING.drops = Collections.singletonList(new ItemStack(Material.ACACIA_LEAVES));
            AZALEA.drops = Collections.singletonList(new ItemStack(Material.AZALEA_LEAVES));
            BIRCH_SAPLING.drops = Collections.singletonList(new ItemStack(Material.BIRCH_LEAVES));
            DARK_OAK_SAPLING.drops = Collections.singletonList(new ItemStack(Material.DARK_OAK_LEAVES));
            FLOWERING_AZALEA.drops = Collections.singletonList(new ItemStack(Material.FLOWERING_AZALEA_LEAVES));
            JUNGLE_SAPLING.drops = Collections.singletonList(new ItemStack(Material.JUNGLE_LEAVES));
            OAK_SAPLING.drops = Collections.singletonList(new ItemStack(Material.OAK_LEAVES));
            SPRUCE_SAPLING.drops = Collections.singletonList(new ItemStack(Material.SPRUCE_LEAVES));
            Arrays.asList(ACACIA_SAPLING, AZALEA, BIRCH_SAPLING, DARK_OAK_SAPLING, FLOWERING_AZALEA, JUNGLE_SAPLING, OAK_SAPLING, SPRUCE_SAPLING).forEach((sapling) -> {
                sapling.newMaterial = Material.DEAD_BUSH;
            });
            TALL_GRASS.newMaterial = Material.GRASS;
            TALL_GRASS.drops = Collections.singletonList(new ItemStack(Material.GRASS));
            TALL_SEAGRASS.newMaterial = Material.SEAGRASS;
            TALL_SEAGRASS.drops = Collections.singletonList(new ItemStack(Material.SEAGRASS));
            LARGE_FERN.newMaterial = Material.FERN;
            LARGE_FERN.drops = Collections.singletonList(new ItemStack(Material.FERN));
        }
    }
}
