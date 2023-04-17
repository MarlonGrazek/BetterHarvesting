//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.marlongrazek.betterharvesting;

import com.marlongrazek.betterharvesting.main.Main;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class Recipes {
    private final Main plugin;

    public Recipes(Main plugin) {
        this.plugin = plugin;
    }

    public void setUp() {
        Bukkit.addRecipe(this.water_bottle());
        Recipes.SaplingType[] var1 = Recipes.SaplingType.values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            Recipes.SaplingType type = var1[var3];
            Bukkit.addRecipe(this.sapling(type));
        }

    }

    private ShapelessRecipe water_bottle() {
        ItemStack waterBottle = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta)waterBottle.getItemMeta();
        PotionData data = new PotionData(PotionType.WATER);
        meta.setBasePotionData(data);
        waterBottle.setItemMeta(meta);
        NamespacedKey key = new NamespacedKey(this.plugin, "water_bottle");
        ShapelessRecipe recipe = new ShapelessRecipe(key, waterBottle);
        recipe.addIngredient(Material.GLASS_BOTTLE);
        recipe.addIngredient(Material.CACTUS);
        return recipe;
    }

    private ShapedRecipe sapling(Recipes.SaplingType type) {
        ItemStack sapling = new ItemStack(type.getSapling());
        NamespacedKey key = new NamespacedKey(this.plugin, type.name().toLowerCase() + "_sapling");
        ShapedRecipe recipe = new ShapedRecipe(key, sapling);
        recipe.shape(new String[]{"L", "D"});
        recipe.setIngredient('L', type.getLeaves());
        recipe.setIngredient('D', Material.DEAD_BUSH);
        return recipe;
    }

    public List<NamespacedKey> getRecipeKeys() {
        List<NamespacedKey> keys = new ArrayList();
        keys.add(this.water_bottle().getKey());
        Recipes.SaplingType[] var2 = Recipes.SaplingType.values();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Recipes.SaplingType value = var2[var4];
            keys.add(this.sapling(value).getKey());
        }

        return keys;
    }

    public static enum SaplingType {
        ACACIA,
        AZALEA,
        BIRCH,
        DARK_OAK,
        FLOWERING_AZALEA,
        JUNGLE,
        OAK,
        SPRUCE;

        private Material sapling;
        private Material leaves;

        private SaplingType() {
        }

        public Material getSapling() {
            return this.sapling;
        }

        public Material getLeaves() {
            return this.leaves;
        }

        static {
            ACACIA.sapling = Material.ACACIA_SAPLING;
            AZALEA.sapling = Material.AZALEA;
            BIRCH.sapling = Material.BIRCH_SAPLING;
            DARK_OAK.sapling = Material.DARK_OAK_SAPLING;
            FLOWERING_AZALEA.sapling = Material.FLOWERING_AZALEA;
            JUNGLE.sapling = Material.JUNGLE_SAPLING;
            OAK.sapling = Material.OAK_SAPLING;
            SPRUCE.sapling = Material.SPRUCE_SAPLING;
            ACACIA.leaves = Material.ACACIA_LEAVES;
            AZALEA.leaves = Material.AZALEA_LEAVES;
            BIRCH.leaves = Material.BIRCH_LEAVES;
            DARK_OAK.leaves = Material.DARK_OAK_LEAVES;
            FLOWERING_AZALEA.leaves = Material.FLOWERING_AZALEA_LEAVES;
            JUNGLE.leaves = Material.JUNGLE_LEAVES;
            OAK.leaves = Material.OAK_LEAVES;
            SPRUCE.leaves = Material.SPRUCE_LEAVES;
        }
    }
}
