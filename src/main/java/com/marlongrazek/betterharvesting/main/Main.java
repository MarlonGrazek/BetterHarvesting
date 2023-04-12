package com.marlongrazek.betterharvesting.main;

import com.marlongrazek.betterharvesting.commands.CMDsettings;
import com.marlongrazek.datafile.DataFile;
import com.marlongrazek.betterharvesting.Recipes;
import com.marlongrazek.betterharvesting.events.*;
import com.marlongrazek.ui.History;
import de.jeff_media.updatechecker.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends JavaPlugin {

    private static Main plugin;

    private final Map<Player, History> history = new HashMap<>();
    private final Map<Player, Map<String, Integer>> page = new HashMap<>();

    @Override
    public void onEnable() {

        plugin = this;

        setUp();
        registerRecipes();
        registerCommands();
        registerEvents();

        checkForUpdates();

        Bukkit.getServer().getConsoleSender().sendMessage(getDataFile("config").get("prefix") + " §fsuccessfully enabled");
    }

    private void registerRecipes() {
        Recipes recipes = new Recipes(plugin);
        recipes.setUp();
    }

    private void registerCommands() {
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
        pm.registerEvents(new EVNwaterPlants(), this);
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

        DataFile config = new DataFile("config", plugin.getDataFolder().getAbsolutePath());
        DataFile settings = new DataFile("settings", plugin.getDataFolder().getAbsolutePath());

        if (!config.contains("prefix")) config.set("prefix", "§eBetterHarvesting");
        if (!config.contains("no_permission"))
            config.set("no_permission", "§cYou don't have the permission to do that");

        List<Material> saplings = List.of(Material.ACACIA_SAPLING, Material.AZALEA, Material.BIRCH_SAPLING, Material.DARK_OAK_SAPLING,
                Material.FLOWERING_AZALEA, Material.JUNGLE_SAPLING, Material.OAK_SAPLING, Material.SPRUCE_SAPLING);

        List<Material> flowers = List.of(Material.POPPY, Material.DANDELION, Material.ALLIUM, Material.AZURE_BLUET,
                Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP, Material.LILY_OF_THE_VALLEY,
                Material.CORNFLOWER, Material.OXEYE_DAISY, Material.BLUE_ORCHID);

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

        if (!settings.contains("watering")) setupPaths.add("watering");
        if (!settings.contains("poisoning")) setupPaths.add("poisoning");

        if (!settings.contains("crop_harvesting")) setupPaths.add("crop_harvesting");
        crops.forEach(crop -> {
            String name = crop.name().toLowerCase();
            if (!settings.contains("crop_harvesting.crops." + name))
                settings.set("crop_harvesting.crops." + name, true);
        });
        if (!settings.contains("crop_harvesting.tools.no_tool")) settings.set("crop_harvesting.tools.no_tool", true);
        if (!settings.contains("crop_harvesting.tools.hoe")) settings.set("crop_harvesting.tools.hoe", true);
        if (!settings.contains("crop_harvesting.fortune")) settings.set("crop_harvesting.fortune", true);

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

        if (!settings.contains("shearing")) setupPaths.add("shearing");
        saplings.forEach(sapling -> {
            String name = sapling.name().toLowerCase();
            if (!settings.contains("shearing.blocks." + name)) settings.set("shearing.blocks." + name, true);
        });
        if (!settings.contains("shearing.blocks.tall_grass")) settings.set("shearing.blocks.tall_grass", true);
        if (!settings.contains("shearing.blocks.tall_seagrass")) settings.set("shearing.blocks.tall_seagrass", true);
        if (!settings.contains("shearing.blocks.large_fern")) settings.set("shearing.blocks.large_fern", true);

        if (!settings.contains("bonemealing")) setupPaths.add("bonemealing");
        if (!settings.contains("bonemealing.blocks.sugar_cane")) settings.set("bonemealing.blocks.sugar_cane", true);
        if (!settings.contains("bonemealing.blocks.cactus")) settings.set("bonemealing.blocks.cactus", true);
        if (!settings.contains("bonemealing.blocks.vine")) settings.set("bonemealing.blocks.vine", true);
        if (!settings.contains("bonemealing.blocks.dead_bush")) settings.set("bonemealing.blocks.dead_bush", true);
        if (!settings.contains("bonemealing.blocks.nether_wart"))
            settings.set("bonemealing.blocks.nether_wart", true);
        if (!settings.contains("bonemealing.blocks.dirt")) settings.set("bonemealing.blocks.dirt", true);
        if (!settings.contains("bonemealing.blocks.netherrack")) settings.set("bonemealing.blocks.netherrack", true);
        if (!settings.contains("bonemealing.blocks.nether_sprouts"))
            settings.set("bonemealing.blocks.nether_sprouts", true);
        if (!settings.contains("bonemealing.blocks.azalea_leaves"))
            settings.set("bonemealing.blocks.azalea_leaves", true);
        flowers.forEach(flower -> {
            String name = flower.name().toLowerCase();
            if (!settings.contains("bonemealing.blocks." + name)) settings.set("bonemealing.blocks." + name, true);
        });

        if (!settings.contains("experimental")) {
            settings.set("experimental.enabled", false);
            settings.set("experimental.permissions", new ArrayList<>());
        }
        if (!settings.contains("experimental.settings.mega_trees"))
            settings.set("experimental.settings.mega_trees", false);

        // CUSTOM DROPS
        List<Material> custom_drops_list = List.of(Material.WHEAT, Material.POTATOES, Material.CARROTS,
                Material.BEETROOTS, Material.NETHER_WART);
        for (Material material : custom_drops_list) {
            if (settings.contains("custom_drops." + material.name().toLowerCase())) continue;
            settings.set("custom_drops." + material.name().toLowerCase() + ".default", true);
            settings.set("custom_drops." + material.name().toLowerCase() + ".creative", false);
            settings.set("custom_drops." + material.name().toLowerCase() + ".fortune", true);
            settings.set("custom_drops." + material.name().toLowerCase() + ".xp.chance", 0.33);
            settings.set("custom_drops." + material.name().toLowerCase() + ".xp.amount", 3);
            settings.set("custom_drops." + material.name().toLowerCase() + ".drops", new ArrayList<>());
            settings.set("custom_drops." + material.name().toLowerCase() + ".bonemeal_strength", 1.0);
        }

        setupPaths.forEach(path -> {
            settings.set(path + ".enabled", true);
            settings.set(path + ".permissions", new ArrayList<>());
        });

        Bukkit.getOnlinePlayers().forEach(this::setUp);
    }

    public void setUp(Player player) {
        history.put(player, new History(player));
        page.put(player, new HashMap<>());
    }

    public DataFile getDataFile(String name) {

        DataFile dataFile = null;
        for (File file : plugin.getDataFolder().listFiles())
            if (file.getName().equalsIgnoreCase(name + ".yml")) dataFile = new DataFile(file);
        return dataFile;
    }

    public History getHistory(Player player) {
        return history.get(player);
    }

    public Map<String, Integer> getPage(Player player) {
        return page.get(player);
    }
}
