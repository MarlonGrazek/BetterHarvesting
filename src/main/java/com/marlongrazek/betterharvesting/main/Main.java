package com.marlongrazek.betterharvesting.main;

import com.marlongrazek.betterharvesting.commands.CMDsettings;
import com.marlongrazek.datafile.DataFile;
import com.marlongrazek.betterharvesting.Recipes;
import com.marlongrazek.betterharvesting.events.*;
import com.marlongrazek.ui.History;
import de.jeff_media.updatechecker.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;

public class Main extends JavaPlugin {

    private static Main plugin;

    private static final HashMap<Player, History> history = new HashMap<>();

    @Override
    public void onEnable() {

        plugin = this;
        Recipes recipes = new Recipes();
        recipes.setUp();

        setUp();

        //getCommand("bhsettings").setExecutor(new CMDsettings());

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new EVNsetupPlayer(), this);
        pm.registerEvents(new EVNharvestCrops(), this);
        //pm.registerEvents(new EVNdispenserUse(), this);
        pm.registerEvents(new EVNbonemealPlants(), this);
        pm.registerEvents(new EVNplayerSneak(), this);
        pm.registerEvents(new EVNwaterCrops(), this);
        pm.registerEvents(new EVNharvestFromBlocks(), this);
        pm.registerEvents(new EVNwaterCrops2(), this);
        pm.registerEvents(new EVNprojectileHit(), this);
        pm.registerEvents(new EVNshearPlants(), this);

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

        if (!config.contains("prefix")) config.set("prefix", "§eBetterHarvesting");

        Bukkit.getOnlinePlayers().forEach(this::setUp);
    }

    public void setUp(Player player) {
        history.put(player, new History(player));
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
}
