package com.marlongrazek.betterharvesting.main;

import com.marlongrazek.betterharvesting.commands.CMDbetterharvesting;
import com.marlongrazek.betterharvesting.commands.CMDsettings;
import com.marlongrazek.betterharvesting.Recipes;
import com.marlongrazek.betterharvesting.events.*;
import com.marlongrazek.customfileconfiguration.CFC;
import com.marlongrazek.ui.History;
import de.jeff_media.updatechecker.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends JavaPlugin {

    private final Map<Player, History> history = new HashMap<>();
    private final Map<Player, Map<String, Integer>> page = new HashMap<>();

    private CFC config;
    private CFC settings;

    @Override
    public void onEnable() {

        setUp();
        registerRecipes();
        registerCommands();
        registerEvents();

        checkForUpdates();

        String lang = config.getString("language");
        FileConfiguration languageFile = getLanguageFile(lang);

        Bukkit.getServer().getConsoleSender().sendMessage(String.format("§e" + languageFile.getString("messages.enabled"), config.get("prefix") + "§f"));
    }

    private void registerRecipes() {
        Recipes recipes = new Recipes(this);
        recipes.setUp();
    }

    private void registerCommands() {
        getCommand("betterharvesting").setExecutor(new CMDbetterharvesting(this));
        getCommand("bhsettings").setExecutor(new CMDsettings(this));
        //getCommand("bhsettings").setTabCompleter(new CMDsettings(this));
    }

    private void registerEvents() {
        PluginManager pm = Bukkit.getPluginManager();
        //pm.registerEvents(new EVNdispenserUse(), this);
        pm.registerEvents(new EVNsetupPlayer(this), this);
        pm.registerEvents(new EVNmodifyBlocks(this), this);
        pm.registerEvents(new EVNbonemealPlants(this), this);
        pm.registerEvents(new EVNplayerSneak(this), this);
        pm.registerEvents(new EVNhoeHarvesting(this), this);
        pm.registerEvents(new EVNshearPlants(this), this);
        pm.registerEvents(new EVNprepareCrafting(this), this);
        pm.registerEvents(new EVNwaterPlants(this), this);
        pm.registerEvents(new EVNmoistureChange(this), this);
        pm.registerEvents(new EVNblockPlace(this), this);
        pm.registerEvents(new EVNentityChangeBlock(this), this);
    }

    private void checkForUpdates() {
        int spigotID = 98816;
        UpdateChecker.init(this, spigotID)
                .setDownloadLink("https://www.spigotmc.org/resources/better-harvesting.98816/")
                .setDonationLink("https://www.paypal.com/donate?hosted_button_id=X7CSZTZE3K9LW")
                .setChangelogLink(spigotID)
                .checkEveryXHours(24)
                .checkNow();
    }

    private void setUp() {

        config = new CFC("config", this);
        settings = new CFC("settings", this);

        if(!config.contains("language")) config.set("language", "en");
        if (!config.contains("prefix")) config.set("prefix", "§eBetterHarvesting");
        if (!config.contains("no_permission"))
            config.set("no_permission", "§cYou don't have the permission to do that");

        List<Material> saplings = List.of(Material.ACACIA_SAPLING, Material.AZALEA, Material.BIRCH_SAPLING, Material.DARK_OAK_SAPLING,
                Material.FLOWERING_AZALEA, Material.JUNGLE_SAPLING, Material.OAK_SAPLING, Material.SPRUCE_SAPLING);

        List<Material> crops = List.of(Material.WHEAT_SEEDS, Material.BEETROOT_SEEDS, Material.POTATO, Material.CARROT,
                Material.COCOA_BEANS, Material.PUMPKIN_SEEDS, Material.MELON_SEEDS, Material.NETHER_WART);

        List<Material> leaves = List.of(Material.ACACIA_LEAVES, Material.AZALEA_LEAVES, Material.BIRCH_LEAVES, Material.DARK_OAK_LEAVES,
                Material.FLOWERING_AZALEA_LEAVES, Material.JUNGLE_LEAVES, Material.OAK_LEAVES, Material.SPRUCE_LEAVES);

        List<String> setupPaths = new ArrayList<>();

        if (!settings.contains("crafting")) setupPaths.add("crafting");
        saplings.forEach(sapling -> {
            String name = sapling.name().toLowerCase();
            if (!settings.contains("crafting.recipes" + name)) settings.set("crafting.recipes." + name, true);
        });
        if (!settings.contains("crafting.recipes.potion")) settings.set("crafting.recipes.potion", true);

        if (!settings.contains("right_clicking")) setupPaths.add("right_clicking");
        if (!settings.contains("right_clicking.blocks.candle")) settings.set("right_clicking.blocks.candle", true);
        if (!settings.contains("right_clicking.blocks.sea_pickle"))
            settings.set("right_clicking.blocks.sea_pickle", true);
        if (!settings.contains("right_clicking.blocks.carved_pumpkin"))
            settings.set("right_clicking.blocks.carved_pumpkin", true);
        if (!settings.contains("right_clicking.blocks.jack_o_lantern"))
            settings.set("right_clicking.blocks.jack_o_lantern", true);

        // watering
        if (!settings.contains("watering.enabled")) settings.set("watering.enabled", false);
        if (!settings.contains("watering.permissions")) settings.set("watering.permissions", new ArrayList<>());
        if (!settings.contains("watering.range")) settings.set("watering.range", 2);
        if (!settings.contains("watering.strength")) settings.set("watering.strength", 1);
        if (!settings.contains("watering.duration")) settings.set("watering.duration", 5);
        if (!settings.contains("watering.chance")) settings.set("watering.chance", 0.2);
        List.of(Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS, Material.COCOA,
                Material.NETHER_WART, Material.MELON_STEM, Material.PUMPKIN_STEM).forEach(item -> {
            if (!settings.contains("watering.blocks." + item.name().toLowerCase()))
                settings.set("watering.blocks." + item.name().toLowerCase(), true);
        });

        // poisoning
        if (!settings.contains("poisoning.enabled")) settings.set("poisoning.enabled", false);
        if (!settings.contains("poisoning.permissions")) settings.set("poisoning.permissions", new ArrayList<>());
        if (!settings.contains("poisoning.range")) settings.set("poisoning.range", 2);
        if (!settings.contains("poisoning.strength")) settings.set("poisoning.strength", 1);
        if (!settings.contains("poisoning.duration")) settings.set("poisoning.duration", 5);
        if (!settings.contains("poisoning.chance")) settings.set("poisoning.chance", 0.2);
        List.of(Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS, Material.COCOA,
                Material.NETHER_WART, Material.MELON_STEM, Material.PUMPKIN_STEM).forEach(item -> {
            if (!settings.contains("poisoning.blocks." + item.name().toLowerCase()))
                settings.set("poisoning.blocks." + item.name().toLowerCase(), true);
        });

        // harvesting
        if (!settings.contains("harvesting.enabled")) settings.set("harvesting.enabled", false);
        if (!settings.contains("harvesting.permissions")) settings.set("harvesting.permissions", new ArrayList<>());
        if (!settings.contains("harvesting.requires_tool")) settings.set("harvesting.requires_tool", false);
        if (!settings.contains("harvesting.fortune")) settings.set("harvesting.fortune", true);
        if (!settings.contains("harvesting.quick")) settings.set("harvesting.quick", false);
        List.of(Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS, Material.COCOA,
                Material.NETHER_WART, Material.MELON_STEM, Material.PUMPKIN_STEM).forEach(item -> {
            if (!settings.contains("harvesting.crops." + item.name().toLowerCase()))
                settings.set("harvesting.crops." + item.name().toLowerCase(), true);
        });

        if (!settings.contains("better_drops")) setupPaths.add("better_drops");
        leaves.forEach(leaf -> {
            String name = leaf.name().toLowerCase();
            if (!settings.contains("better_drops.blocks." + name)) settings.set("better_drops.blocks." + name, true);
        });
        if (!settings.contains("better_drops.blocks.grass")) settings.set("better_drops.blocks.grass", true);
        if (!settings.contains("better_drops.blocks.tall_grass")) settings.set("better_drops.blocks.tall_grass", true);
        if (!settings.contains("better_drops.blocks.fern")) settings.set("better_drops.blocks.fern", true);
        if (!settings.contains("better_drops.blocks.large_fern")) settings.set("better_drops.blocks.large_fern", true);
        if (!settings.contains("better_drops.tools.no_tool")) settings.set("better_drops.tools.no_tool", true);
        if (!settings.contains("better_drops.tools.hoe")) settings.set("better_drops.tools.hoe", true);
        if (!settings.contains("better_drops.fortune")) settings.set("better_drops.fortune", true);

        if (!settings.contains("sneaking")) setupPaths.add("sneaking");
        crops.forEach(crop -> {
            String name = crop.name().toLowerCase();
            if (!settings.contains("sneaking.blocks." + name)) settings.set("sneaking.blocks." + name, true);
        });
        saplings.forEach(sapling -> {
            String name = sapling.name().toLowerCase();
            if (!settings.contains("sneaking.blocks." + name)) settings.set("sneaking.blocks." + name, true);
        });
        if (!settings.contains("sneaking.range")) settings.set("sneaking.range", 3);
        if (!settings.contains("sneaking.chance")) settings.set("sneaking.chance", 10);

        // shears
        if (!settings.contains("shears.enabled")) settings.set("shears.enabled", false);
        if (!settings.contains("shears.permissions")) settings.set("shears.permissions", new ArrayList<>());
        if (!settings.contains("shears.fortune")) settings.set("shears.fortune", true);
        List.of(Material.WHEAT, Material.POTATOES, Material.CARROTS, Material.BEETROOTS, Material.MELON_STEM,
                Material.PUMPKIN_STEM, Material.COCOA, Material.NETHER_WART, Material.TALL_GRASS,
                Material.TALL_SEAGRASS, Material.LARGE_FERN, Material.ACACIA_SAPLING, Material.AZALEA,
                Material.BIRCH_SAPLING, Material.DARK_OAK_SAPLING, Material.FLOWERING_AZALEA, Material.JUNGLE_SAPLING,
                Material.OAK_SAPLING, Material.SPRUCE_SAPLING).forEach(block -> {
            if (!settings.contains("shears.blocks." + block.name().toLowerCase()))
                settings.set("shears.blocks." + block.name().toLowerCase(), true);
        });

        // bonemeal
        if (!settings.contains("bonemeal")) settings.set("bonemeal.enabled", false);
        if (!settings.contains("bonemeal.permissions")) settings.set("bonemeal.permissions", new ArrayList<>());
        if (!settings.contains("bonemeal.strength")) settings.set("bonemeal.strength", 1.0);
        List.of(Material.WHEAT, Material.POTATOES, Material.CARROTS, Material.BEETROOTS, Material.COCOA, Material.NETHER_WART,
                Material.MELON_STEM, Material.PUMPKIN_STEM, Material.SUGAR_CANE, Material.CACTUS, Material.VINE, Material.DEAD_BUSH,
                Material.DIRT, Material.NETHERRACK, Material.NETHER_SPROUTS, Material.AZALEA_LEAVES, Material.GRASS, Material.FERN,
                Material.SEAGRASS, Material.POPPY,  Material.DANDELION, Material.ALLIUM, Material.AZURE_BLUET, Material.RED_TULIP,
                Material.ORANGE_TULIP,  Material.WHITE_TULIP, Material.PINK_TULIP, Material.LILY_OF_THE_VALLEY, Material.CORNFLOWER,
                Material.OXEYE_DAISY,  Material.BLUE_ORCHID, Material.SUNFLOWER, Material.LILAC, Material.ROSE_BUSH, Material.PEONY,
                Material.BAMBOO,  Material.ACACIA_SAPLING, Material.AZALEA, Material.BIRCH_SAPLING, Material.DARK_OAK_SAPLING,
                Material.FLOWERING_AZALEA,  Material.JUNGLE_SAPLING, Material.OAK_SAPLING, Material.SPRUCE_SAPLING, Material.RED_MUSHROOM,
                Material.BROWN_MUSHROOM, Material.SWEET_BERRY_BUSH, Material.SEA_PICKLE, Material.KELP, Material.WITHER_ROSE,
                Material.CRIMSON_FUNGUS, Material.WARPED_FUNGUS, Material.WEEPING_VINES, Material.TWISTING_VINES).forEach(block -> {
            if (!settings.contains("bonemeal.blocks." + block.name().toLowerCase()))
                settings.set("bonemeal.blocks." + block.name().toLowerCase(), true);
        });

        if (!settings.contains("experimental")) {
            settings.set("experimental.enabled", false);
            settings.set("experimental.permissions", new ArrayList<>());
        }
        if (!settings.contains("experimental.settings.mega_trees"))
            settings.set("experimental.settings.mega_trees", false);

        // CUSTOM DROPS
        List<Material> custom_drops_list = List.of(Material.WHEAT, Material.POTATOES, Material.CARROTS, Material.COCOA,
                Material.BEETROOTS, Material.NETHER_WART, Material.POPPY, Material.DANDELION, Material.BLUE_ORCHID,
                Material.ALLIUM, Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP,
                Material.PINK_TULIP, Material.OXEYE_DAISY, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY,
                Material.NETHER_SPROUTS, Material.AZALEA_LEAVES, Material.SUGAR_CANE, Material.CACTUS, Material.VINE,
                Material.DEAD_BUSH);
        for (Material material : custom_drops_list) {
            if (settings.contains("custom_drops." + material.name().toLowerCase())) continue;
            settings.set("custom_drops." + material.name().toLowerCase() + ".default", false);
            settings.set("custom_drops." + material.name().toLowerCase() + ".creative", false);
            settings.set("custom_drops." + material.name().toLowerCase() + ".fortune", true);
            settings.set("custom_drops." + material.name().toLowerCase() + ".xp.chance", 0.33);
            settings.set("custom_drops." + material.name().toLowerCase() + ".xp.amount", 3);
            settings.set("custom_drops." + material.name().toLowerCase() + ".drops", new ArrayList<>());
        }
        if (!settings.contains("farmland_requires_water")) settings.set("farmland_requires_water", true);
        if(!settings.contains("farmland_trampeling")) settings.set("farmland_trampeling", true);
        if (!settings.contains("gui_volume")) settings.set("gui_volume", 0.5F);

        setupPaths.forEach(path -> {
            settings.set(path + ".enabled", false);
            settings.set(path + ".permissions", new ArrayList<>());
        });

        Bukkit.getOnlinePlayers().forEach(this::setUp);
    }

    public void setUp(Player player) {
        history.put(player, new History(player));
        page.put(player, new HashMap<>());
    }

    public CFC getCFCConfig() {
        config.reload();
        return config;
    }

    public CFC getCFCSettings() {
        settings.reload();
        return settings;
    }

    public FileConfiguration getLanguageFile(String lang) {
        InputStream inputStream = getResource("lang/" + lang + ".yml");
        return YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));
    }

    public History getHistory(Player player) {
        return history.get(player);
    }

    public Map<String, Integer> getPage(Player player) {
        return page.get(player);
    }
}
