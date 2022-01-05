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

    private static final Map<Player, History> history = new HashMap<>();
    private static final Map<Player, Map<String, Integer>> page = new HashMap<>();

    @Override
    public void onEnable() {

        plugin = this;
        Recipes recipes = new Recipes();
        recipes.setUp();

        setUp();

        getCommand("bhsettings").setExecutor(new CMDsettings());

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new EVNsetupPlayer(), this);
        pm.registerEvents(new EVNmodifyBlocks(), this);
        //pm.registerEvents(new EVNdispenserUse(), this);
        pm.registerEvents(new EVNbonemealPlants(), this);
        pm.registerEvents(new EVNplayerSneak(), this);
        pm.registerEvents(new EVNwaterCrops(), this);
        pm.registerEvents(new EVNhoeHarvesting(), this);
        pm.registerEvents(new EVNwaterCrops2(), this);
        pm.registerEvents(new EVNprojectileHit(), this);
        pm.registerEvents(new EVNshearPlants(), this);
        pm.registerEvents(new EVNprepareCrafting(), this);

        Bukkit.getServer().getConsoleSender().sendMessage(getDataFile("config").get("prefix") + " §fsuccessfully enabled");

        int spigotID = 98816;
        UpdateChecker.init(this, spigotID)
                .setDownloadLink("https://www.spigotmc.org/resources/better-harvesting.98816/")
                .setDonationLink("https://www.paypal.com/donate?hosted_button_id=X7CSZTZE3K9LW")
                .setChangelogLink(spigotID)
                .checkEveryXHours(24)
                .checkNow();
    }

    public void setUp() {

        DataFile config = new DataFile("config", plugin.getDataFolder().getAbsolutePath());
        DataFile settings = new DataFile("settings", plugin.getDataFolder().getAbsolutePath());

        if (!config.contains("prefix")) config.set("prefix", "§eBetterHarvesting");
        if (!config.contains("no_permission"))
            config.set("no_permission", "§cYou don't have the permission to do that");

        List<Material> saplings = List.of(Material.ACACIA_SAPLING, Material.AZALEA, Material.BIRCH_SAPLING, Material.DARK_OAK_SAPLING,
                Material.FLOWERING_AZALEA, Material.JUNGLE_SAPLING, Material.OAK_SAPLING, Material.SPRUCE_SAPLING);

        List<Material> flowers = List.of(Material.POPPY, Material.DANDELION, Material.ALLIUM, Material.AZURE_BLUET,
                Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP, Material.LILY_OF_THE_VALLEY,
                Material.CORNFLOWER, Material.OXEYE_DAISY);

        List<Material> crops = List.of(Material.WHEAT_SEEDS, Material.BEETROOT_SEEDS, Material.POTATO, Material.CARROT,
                Material.COCOA_BEANS, Material.PUMPKIN_SEEDS, Material.MELON_SEEDS);

        List<Material> leaves = List.of(Material.ACACIA_LEAVES, Material.AZALEA_LEAVES, Material.BIRCH_LEAVES, Material.DARK_OAK_LEAVES,
                Material.FLOWERING_AZALEA_LEAVES, Material.JUNGLE_LEAVES, Material.OAK_LEAVES, Material.SPRUCE_LEAVES);

        List<String> setupPaths = new ArrayList<>();

        if (!settings.contains("crafting")) setupPaths.add("crafting");
        saplings.forEach(sapling -> {
            String name = sapling.name().toLowerCase();
            if (!settings.contains("crafting." + name)) setupPaths.add("crafting." + name);
        });
        if (!settings.contains("crafting.potion")) setupPaths.add("crafting.potion");

        if (!settings.contains("bonemeal")) setupPaths.add("bonemeal");
        if (!settings.contains("bonemeal.flowers")) setupPaths.add("bonemeal.flowers");
        flowers.forEach(flower -> {
            String name = flower.name().toLowerCase();
            if (!settings.contains("bonemeal.flowers." + name)) setupPaths.add("bonemeal.flowers." + name);
        });
        if (!settings.contains("bonemeal.dead_bush")) setupPaths.add("bonemeal.dead_bush");
        if (!settings.contains("bonemeal.azalea_leaves")) setupPaths.add("bonemeal.azalea_leaves");
        if (!settings.contains("bonemeal.nether_sprouts")) setupPaths.add("bonemeal.nether_sprouts");
        if (!settings.contains("bonemeal.sugar_cane")) setupPaths.add("bonemeal.sugar_cane");
        if (!settings.contains("bonemeal.cactus")) setupPaths.add("bonemeal.cactus");
        if (!settings.contains("bonemeal.vine")) setupPaths.add("bonemeal.vine");
        if (!settings.contains("bonemeal.nether_wart")) setupPaths.add("bonemeal.nether_wart");
        if (!settings.contains("bonemeal.dirt")) setupPaths.add("bonemeal.dirt");
        if (!settings.contains("bonemeal.netherrack")) setupPaths.add("bonemeal.netherrack");

        if (!settings.contains("shearing")) setupPaths.add("shearing");
        if (!settings.contains("shearing.saplings")) setupPaths.add("shearing.saplings");
        saplings.forEach(sapling -> {
            String name = sapling.name().toLowerCase();
            if (!settings.contains("shearing.saplings." + name)) setupPaths.add("shearing.saplings." + name);
        });
        if (!settings.contains("shearing.tall_blocks")) setupPaths.add("shearing.tall_blocks");
        if (!settings.contains("shearing.tall_blocks.tall_grass")) setupPaths.add("shearing.tall_blocks.tall_grass");
        if (!settings.contains("shearing.tall_blocks.tall_seagrass"))
            setupPaths.add("shearing.tall_blocks.tall_seagrass");
        if (!settings.contains("shearing.tall_blocks.large_fern")) setupPaths.add("shearing.tall_blocks.large_fern");
        if (!settings.contains("shearing.pumpkin")) setupPaths.add("shearing.pumpkin");

        if (!settings.contains("modify")) setupPaths.add("modify");
        if (!settings.contains("modify.crops")) setupPaths.add("modify.crops");
        crops.forEach(crop -> {
            String name = crop.name().toLowerCase();
            if (!settings.contains("modify.crops." + name)) setupPaths.add("modify.crops." + name);
        });
        if (!settings.contains("modify.carved_pumpkin")) setupPaths.add("modify.carved_pumpkin");
        if (!settings.contains("modify.candle")) setupPaths.add("modify.candle");
        if (!settings.contains("modify.sea_pickle")) setupPaths.add("modify.sea_pickle");
        if (!settings.contains("modify.hoe")) setupPaths.add("modify.hoe");
        if (!settings.contains("modify.hoe.leaves")) setupPaths.add("modify.hoe.leaves");
        leaves.forEach(leaf -> {
            String name = leaf.name().toLowerCase();
            if (!settings.contains("modify.hoe.leaves." + name)) setupPaths.add("modify.hoe.leaves." + name);
        });
        if (!settings.contains("modify.hoe.grasses")) setupPaths.add("modify.hoe.grasses");
        if (!settings.contains("modify.hoe.grasses.grass")) setupPaths.add("modify.hoe.grasses.grass");
        if (!settings.contains("modify.hoe.grasses.tall_grass")) setupPaths.add("modify.hoe.grasses.tall_grass");
        if (!settings.contains("modify.hoe.grasses.fern")) setupPaths.add("modify.hoe.grasses.fern");
        if (!settings.contains("modify.hoe.grasses.large_fern")) setupPaths.add("modify.hoe.grasses.large_fern");

        if (!settings.contains("sneaking")) setupPaths.add("sneaking");
        if (!settings.contains("sneaking.crops")) setupPaths.add("sneaking.crops");
        crops.forEach(crop -> {
            String name = crop.name().toLowerCase();
            if (!settings.contains("sneaking.crops." + name)) setupPaths.add("sneaking.crops." + name);
        });
        if (!settings.contains("sneaking.saplings")) setupPaths.add("sneaking.saplings");
        saplings.forEach(sapling -> {
            String name = sapling.name().toLowerCase();
            if (!settings.contains("sneaking.saplings." + name)) setupPaths.add("sneaking.saplings." + name);
        });

        if (!settings.contains("watering")) setupPaths.add("watering");

        if (!settings.contains("experimental")) setupPaths.add("experimental");
        if (!settings.contains("experimental.megatrees")) setupPaths.add("experimental.megatrees");

        setupPaths.forEach(path -> {
            settings.set(path + ".enabled", true);
            if (path.equals("experimental") || path.equals("experimental.megatrees"))
                settings.set(path + ".enabled", false);
            settings.set(path + ".permissions", new ArrayList<>());
        });

        Bukkit.getOnlinePlayers().forEach(this::setUp);
    }

    public void setUp(Player player) {
        history.put(player, new History(player));
        page.put(player, new HashMap<>());
    }

    public static Main getPlugin() {
        return plugin;
    }

    public static DataFile getDataFile(String name) {

        DataFile dataFile = null;
        for (File file : plugin.getDataFolder().listFiles())
            if (file.getName().equalsIgnoreCase(name + ".yml")) dataFile = new DataFile(file);
        return dataFile;
    }

    public static History getHistory(Player player) {
        return history.get(player);
    }

    public static Map<String, Integer> getPage(Player player) {
        return page.get(player);
    }
}
