package com.marlongrazek.betterharvesting.utils;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.customfileconfiguration.CFC;
import com.marlongrazek.ui.History;
import net.wesjd.anvilgui.AnvilGUI;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.math3.util.Precision;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.marlongrazek.ui.UI;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class GUI {

    private final Main plugin;
    private final Player player;

    private final UI.Item backgroundItem = new UI.Item(" ", Material.GRAY_STAINED_GLASS_PANE);
    private final String line = "§7-----";
    private final float volume;

    FileConfiguration languageFile;

    public GUI(Player player, Main plugin) {
        this.player = player;
        this.plugin = plugin;
        this.volume = Float.parseFloat(plugin.getCFCSettings().getString("gui_volume"));

        String lang = plugin.getCFCConfig().getString("language");
        languageFile = plugin.getLanguageFile(lang);
    }

    public void open(UI.Page page) {
        page.open(player);
    }

    public void reload() {
        plugin.getHistory(player).openPage(0);
    }

    // PAGES
    public UI.Page confirmation(String reason, Consumer<ClickType> yesAction) {

        UI.Page page = new UI.Page("Confirmation", 36, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);

            footer.setItem(getNavigationItem(), 4);

            // no
            UI.Item no = new UI.Item("§cCancel", Material.RED_STAINED_GLASS_PANE);
            no.addLoreLine("§7Click to cancel");
            no.onClick(clickType -> history.openPage(1));
            page.setItem(no, 11);

            if (!reason.isEmpty()) {
                UI.Item reasonItem = new UI.Item("§f" + reason, Material.FILLED_MAP);
                page.setItem(reasonItem, 13);
            }

            // yes
            UI.Item yes = new UI.Item("§aConfirm", Material.LIME_STAINED_GLASS_PANE);
            yes.addLoreLine("§7Click to confirm");
            yes.onClick(yesAction);
            page.setItem(yes, 15);

            page.setSection(footer, 27);
        });

        return page;
    }

    public UI.Page history() {

        UI.Page page = new UI.Page("Page History", 54, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            int currentPage = getCurrentPage(page);
            ArrayList<UI.Page> pages = history.list();

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);

            // previous page
            if (currentPage > 1) footer.setItem(getPreviousPageItem(pages.size()), 3);

            // navigation
            UI.Item navigation = new UI.Item();
            navigation.setName("§eNavigation");
            navigation.addLoreLine("§7Click to go back");
            navigation.addLoreLine("§7Middle-click to close");
            navigation.onClick(clickType -> {
                if (clickType == ClickType.MIDDLE) p.closeInventory();
                else history.openPage(1);
            });
            navigation.setMaterial(Material.BARRIER);
            footer.setItem(navigation, 4);

            // next page
            if (pages.size() > currentPage * 45) footer.setItem(getNextPageItem(pages.size()), 5);

            // pages
            UI.Section content = new UI.Section(9, 5);
            for (int i = (currentPage - 1) * 45; i < currentPage * 45 && i < pages.size() - 1; i++) {

                UI.Page pageI = pages.get(i);
                if (pageI != null) {

                    UI.Item pageItem = new UI.Item("§a" + pageI.getTitle(), Material.FILLED_MAP);
                    pageItem.addLoreLine("§7Click to return to that page");
                    int selectedPage = pages.size() - (i + 1);
                    pageItem.onClick(clickType -> history.openPage(selectedPage));
                    page.addItem(pageItem);
                }
            }

            page.setSection(content, 0);
            page.setSection(footer, 45);
        });

        return page;
    }

    public UI.Page pages(int amount) {

        UI.Page page = new UI.Page("Pages", 54, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            int currentPage = getCurrentPage(page);
            int previousCurrentPage = getCurrentPage(history.getPage(1));

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);

            // previous page
            if (currentPage > 1) {
                UI.Item previousPage = new UI.Item();
                previousPage.setName("§ePrevious page");
                previousPage.setMaterial(Material.ARROW);
                previousPage.addLoreLine("§7Click to go to the previous page");
                previousPage.addLoreLine("§7Middle-click to jump to the beginning");
                previousPage.onClick(clickType -> {
                    if (clickType == ClickType.MIDDLE) setCurrentPage(page, 1);
                    else setCurrentPage(page, currentPage - 1);
                    reload();
                });
                footer.setItem(previousPage, 3);
            }

            // navigation
            footer.setItem(getNavigationItem(), 4);

            // next page
            if (amount > currentPage * 45) {
                UI.Item nextPage = new UI.Item();
                nextPage.setName("§eNext page");
                nextPage.setMaterial(Material.ARROW);
                nextPage.addLoreLine("§7Click to go to the next page");
                nextPage.addLoreLine("§7Middle-click to jump to the end");
                nextPage.onClick(clickType -> {
                    if (clickType == ClickType.MIDDLE)
                        setCurrentPage(page, (int) Precision.round((float) amount / 45, 0, 0));
                    else setCurrentPage(page, currentPage + 1);
                    reload();
                });
                footer.setItem(nextPage, 5);
            }

            // pages
            UI.Section content = new UI.Section(9, 5);
            for (int i = (currentPage - 1) * 45; i < currentPage * 45 && i < amount; i++) {

                UI.Item pageItem = new UI.Item();
                pageItem.addLoreLine("§7Click to view this page");

                // current page
                if (i + 1 == previousCurrentPage) {
                    pageItem.setName("§aPage " + (i + 1) + " [current]");
                    pageItem.setMaterial(Material.FILLED_MAP);
                }

                // other pages
                else {
                    pageItem.setName("§aPage " + (i + 1));
                    pageItem.setMaterial(Material.MAP);
                }

                int newPage = i + 1;
                pageItem.onClick(clickType -> {
                    setCurrentPage(history.getPage(1), newPage);
                    history.openPage(1);
                });
                page.addItem(pageItem);
            }

            page.setSection(content, 0);
            page.setSection(footer, 45);
        });

        return page;
    }

    public UI.Page settings(World world) {

        String title = languageFile.getString("gui.settings.start.title");
        if (world != null) title = "World Settings";
        UI.Page page = new UI.Page(title, 54, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();
            CFC settings = plugin.getCFCSettings();

            // header
            UI.Section header = new UI.Section(9, 1);
            header.fill(backgroundItem);

            // sidebar
            UI.Section sidebar = new UI.Section(1, 4);
            sidebar.fill(backgroundItem);

            // content
            UI.Section content = new UI.Section(5, 2);

            // crop harvesting
            boolean crop_harvesting_enabled = settings.getBoolean("crop_harvesting.enabled", true);

            UI.Item crop_harvesting = new UI.Item("§e" + languageFile.getString("gui.settings.harvest.title"), Material.WHEAT_SEEDS);

            if (crop_harvesting_enabled) {
                crop_harvesting.addLoreLines("§7" + String.format(languageFile.getString("gui.settings.start.item.enabled"), "§a"), line);
                crop_harvesting.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.disable"));
            } else {
                crop_harvesting.addLoreLines("§7" + String.format(languageFile.getString("gui.settings.start.item.disabled"), "§c"), line);
                crop_harvesting.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.enable"));
            }
            crop_harvesting.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.customize"));

            crop_harvesting.onClick(clickType -> {
                if (clickType == ClickType.RIGHT) {
                    open(settings_cropharvesting());
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
                } else {
                    settings.set("crop_harvesting.enabled", !crop_harvesting_enabled);
                    reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, volume, 0.7F);
                }
            });
            content.addItem(crop_harvesting);

            // better drops
            boolean better_drops_enabled = settings.getBoolean("better_drops.enabled", true);

            UI.Item better_drops = new UI.Item("§eBetter Drops", Material.IRON_HOE);
            better_drops.addItemFlags(Arrays.asList(ItemFlag.values()));

            if(better_drops_enabled) {
                better_drops.addLoreLines("§7" + String.format(languageFile.getString("gui.settings.start.item.enabled"), "§a"), line);
                better_drops.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.disable"));
            } else {
                better_drops.addLoreLines("§7" + String.format(languageFile.getString("gui.settings.start.item.disabled"), "§c"), line);
                better_drops.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.enable"));
            }
            better_drops.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.customize"));

            better_drops.onClick(clickType -> {
                if (clickType == ClickType.RIGHT) {
                    open(settings_betterdrops());
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
                }
                else {
                    settings.set("better_drops.enabled", !better_drops_enabled);
                    reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, volume, 0.7F);
                }
            });
            content.addItem(better_drops);

            // sneaking
            boolean sneaking_enabled = settings.getBoolean("sneaking.enabled", true);

            UI.Item sneaking = new UI.Item("§eSneaking", Material.FARMLAND);

            if(sneaking_enabled) {
                sneaking.addLoreLines("§7" + String.format(languageFile.getString("gui.settings.start.item.enabled"), "§a"), line);
                sneaking.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.disable"));
            } else {
                sneaking.addLoreLines("§7" + String.format(languageFile.getString("gui.settings.start.item.disabled"), "§c"), line);
                sneaking.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.enable"));
            }
            sneaking.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.customize"));

            sneaking.onClick(clickType -> {
                if (clickType == ClickType.RIGHT) {
                    open(settings_sneaking());
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
                } else {
                    settings.set("sneaking.enabled", !sneaking_enabled);
                    reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, volume, 0.7F);
                }
            });
            content.addItem(sneaking);

            // shears
            boolean shearing_enabled = settings.getBoolean("shears.enabled", true);

            UI.Item shearing = new UI.Item("§e" + languageFile.getString("gui.settings.shears.title"), Material.SHEARS);

            if(shearing_enabled) {
                shearing.addLoreLines("§7" + String.format(languageFile.getString("gui.settings.start.item.enabled"), "§a"), line);
                shearing.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.disable"));
            } else {
                shearing.addLoreLines("§7" + String.format(languageFile.getString("gui.settings.start.item.disabled"), "§c"), line);
                shearing.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.enable"));
            }
            shearing.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.customize"));

            shearing.onClick(clickType -> {
                if (clickType == ClickType.RIGHT) {
                    open(settings_shears());
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
                }
                else {
                    settings.set("shears.enabled", !shearing_enabled);
                    reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, volume, 0.7F);
                }
            });
            content.addItem(shearing);

            // crafting
            boolean crafting_enabled = settings.getBoolean("crafting.enabled", true);

            UI.Item crafting = new UI.Item("§e" + languageFile.getString("gui.settings.craft.title"), Material.CRAFTING_TABLE);

            if(crafting_enabled) {
                crafting.addLoreLines("§7" + String.format(languageFile.getString("gui.settings.start.item.enabled"), "§a"), line);
                crafting.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.disable"));
            } else {
                crafting.addLoreLines("§7" + String.format(languageFile.getString("gui.settings.start.item.disabled"), "§c"), line);
                crafting.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.enable"));
            }
            crafting.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.customize"));

            crafting.onClick(clickType -> {
                if (clickType == ClickType.RIGHT) {
                    open(settings_crafting());
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
                }
                else {
                    settings.set("crafting.enabled", !crafting_enabled);
                    reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, volume, 0.7F);
                }
            });
            content.addItem(crafting);

            // bone meal
            boolean bonemeal_enabled = settings.getBoolean("bonemeal.enabled", true);

            UI.Item bonemeal = new UI.Item("§e" + languageFile.getString("gui.settings.bonemeal.title"), Material.BONE_MEAL);

            if(bonemeal_enabled) {
                bonemeal.addLoreLines("§7" + String.format(languageFile.getString("gui.settings.start.item.enabled"), "§a"), line);
                bonemeal.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.disable"));
            } else {
                bonemeal.addLoreLines("§7" + String.format(languageFile.getString("gui.settings.start.item.disabled"), "§c"), line);
                bonemeal.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.enable"));
            }
            bonemeal.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.customize"));

            bonemeal.onClick(clickType -> {
                if (clickType == ClickType.RIGHT) {
                    open(settings_bone_meal());
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
                } else {
                    settings.set("bonemeal.enabled", !bonemeal_enabled);
                    reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, volume, 0.7F);
                }
            });
            content.addItem(bonemeal);

            // right click
            boolean right_click_enabled = settings.getBoolean("right_clicking.enabled", true);

            UI.Item right_click = new UI.Item("§eRight Click", Material.FILLED_MAP);
            right_click.addItemFlags(Arrays.asList(ItemFlag.values()));

            if(right_click_enabled) {
                right_click.addLoreLines("§7" + String.format(languageFile.getString("gui.settings.start.item.enabled"), "§a"), line);
                right_click.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.disable"));
            } else {
                right_click.addLoreLines("§7" + String.format(languageFile.getString("gui.settings.start.item.disabled"), "§c"), line);
                right_click.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.enable"));
            }
            right_click.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.customize"));

            right_click.onClick(clickType -> {
                if (clickType == ClickType.RIGHT) {
                    open(settings_right_clicking());
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
                }
                else {
                    settings.set("right_clicking.enabled", !right_click_enabled);
                    reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, volume, 0.7F);
                }
            });
            content.addItem(right_click);

            // watering
            boolean watering_enabled = settings.getBoolean("watering.enabled", true);

            ItemStack bottle = new ItemStack(Material.SPLASH_POTION);
            PotionMeta meta = (PotionMeta) bottle.getItemMeta();
            meta.setBasePotionData(new PotionData(PotionType.WATER));
            bottle.setItemMeta(meta);

            UI.Item watering = UI.Item.fromItemStack(bottle);
            watering.setName("§e" + languageFile.getString("gui.settings.water.title"));
            watering.addItemFlag(ItemFlag.HIDE_POTION_EFFECTS);

            if(watering_enabled) {
                watering.addLoreLines("§7" + String.format(languageFile.getString("gui.settings.start.item.enabled"), "§a"), line);
                watering.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.disable"));
            } else {
                watering.addLoreLines("§7" + String.format(languageFile.getString("gui.settings.start.item.disabled"), "§c"), line);
                watering.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.enable"));
            }
            watering.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.customize"));

            watering.onClick(clickType -> {
                if (clickType == ClickType.RIGHT) {
                    open(settings_watering());
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
                }
                else {
                    settings.set("watering.enabled", !watering_enabled);
                    reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, volume, 0.7F);
                }
            });
            content.addItem(watering);

            // poisoning
            boolean poisoning_enabled = settings.getBoolean("poisoning.enabled", true);

            ItemStack poison = new ItemStack(Material.SPLASH_POTION);
            PotionMeta poison_meta = (PotionMeta) poison.getItemMeta();
            poison_meta.setColor(Color.fromRGB(80, 150, 60));
            poison.setItemMeta(poison_meta);

            UI.Item poisoning = UI.Item.fromItemStack(poison);
            poisoning.setName("§e" + languageFile.getString("gui.settings.poison.title"));
            poisoning.addItemFlag(ItemFlag.HIDE_POTION_EFFECTS);

            if(poisoning_enabled) {
                poisoning.addLoreLines("§7" + String.format(languageFile.getString("gui.settings.start.item.enabled"), "§a"), line);
                poisoning.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.disable"));
            } else {
                poisoning.addLoreLines("§7" + String.format(languageFile.getString("gui.settings.start.item.disabled"), "§c"), line);
                poisoning.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.enable"));
            }
            poisoning.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.customize"));

            poisoning.onClick(clickType -> {
                if (clickType == ClickType.RIGHT) {
                    open(settings_poisoning());
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
                }
                else {
                    settings.set("poisoning.enabled", !poisoning_enabled);
                    reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, volume, 0.7F);
                }
            });
            content.addItem(poisoning);

            // experimental
            boolean experimental_enabled = settings.getBoolean("experimental.enabled", true);

            UI.Item experimental = new UI.Item("§eExperimental", Material.NETHER_STAR);

            if(experimental_enabled) {
                experimental.addLoreLines("§7" + String.format(languageFile.getString("gui.settings.start.item.enabled"), "§a"), line);
                experimental.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.disable"));
            } else {
                experimental.addLoreLines("§7" + String.format(languageFile.getString("gui.settings.start.item.disabled"), "§c"), line);
                experimental.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.enable"));
            }
            experimental.addLoreLines("§7" + languageFile.getString("gui.settings.start.item.customize"));

            experimental.onClick(clickType -> {
                if (clickType == ClickType.RIGHT) {
                    open(settings_experimental());
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
                }
                else {
                    settings.set("experimental.enabled", !experimental_enabled);
                    reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, volume, 0.7F);
                }
            });
            content.addItem(experimental);

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            // world settings
            if (world == null) {
                UI.Item worldSettings = UI.Item.Skull.fromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODc5ZTU0Y2JlODc4NjdkMTRiMmZiZGYzZjE4NzA4OTQzNTIwNDhkZmVjZDk2Mjg0NmRlYTg5M2IyMTU0Yzg1In19fQ==");
                worldSettings.setName("§eWorld Settings");
                worldSettings.addLoreLines("§7Change settings specific for this world", line);
                worldSettings.addLoreLines("§7Click to customize", "§7Right-click to reset");
                worldSettings.onClick(clickType -> {
                    if (clickType == ClickType.RIGHT) {

                    } else open(settings(p.getWorld()));
                });
                footer.setItem(worldSettings, 8);
            }

            page.setSection(header, 0);
            page.setSection(sidebar, 9);
            page.setSection(sidebar, 17);
            page.setSection(content, 20);
            page.setSection(footer, 45);
        });

        return page;
    }

    public UI.Page settings_cropharvesting() {

        UI.Page page = new UI.Page("Crop Harvesting", 36, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();
            CFC settings = plugin.getCFCSettings();

            UI.Section content = new UI.Section(7, 1);

            // crops
            UI.Item crops = new UI.Item("§eCrops", Material.WHEAT_SEEDS);
            crops.addLoreLines("§7Choose the crops that can be harvested", line);
            crops.addLoreLine("§7Click to view the crops settings");
            crops.onClick(clickType -> open(settings_cropharvesting_crops()));
            content.setItem(crops, 0);

            // tool
            UI.Item tools = new UI.Item("§eTools", Material.IRON_HOE);
            tools.addLoreLines("§7Choose the tools that", "§7can be used to harvest", line);
            tools.addLoreLine("§7Click to view the tools settings");
            tools.addItemFlag(ItemFlag.HIDE_ATTRIBUTES);
            tools.onClick(clickType -> open(settings_cropharvesting_tools()));
            content.setItem(tools, 2);

            String fortune_path = "crop_harvesting.fortune";
            boolean fortune_enabled = settings.getBoolean(fortune_path, true);
            String[] data = getData(fortune_enabled);

            // fortune
            UI.Item fortune = new UI.Item(data[0] + "Fortune", Material.ENCHANTED_BOOK);
            fortune.addLoreLines("§7State: " + data[0] + data[1], line);
            fortune.addLoreLines("§7Decide whether the fortune enchantment", "§7will affect the drops or not", line);
            fortune.addLoreLine("§7Click to " + data[2]);
            fortune.onClick(clickType -> {
                settings.set(fortune_path, !fortune_enabled);
                reload();
            });
            content.setItem(fortune, 4);

            // permissions
            UI.Item permissions = new UI.Item("§ePermissions", Material.FILLED_MAP);
            permissions.addLoreLines("§7Set permissions to allow only", "§7certain players to harvest", line);
            permissions.addLoreLine("§7Click to view the permissions");
            permissions.onClick(clickType -> open(settings_permissions("crop_harvesting")));
            content.setItem(permissions, 6);

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            page.setSection(content, 10);
            page.setSection(footer, 27);
        });

        return page;
    }

    public UI.Page settings_cropharvesting_crops() {

        UI.Page page = new UI.Page("Crop Harvesting - Crops", 45, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            CFC settings = plugin.getCFCSettings();
            List<String> crops = List.of("Wheat Seeds", "Beetroot Seeds", "Potato", "Carrot", "Cocoa Beans", "Pumpkin Seeds", "Melon Seeds", "Nether Wart");

            // content
            UI.Section content = new UI.Section(7, 2);

            crops.forEach(name -> {

                Material material = Material.valueOf(name.toUpperCase().replaceAll(" ", "_"));
                boolean enabled = settings.getBoolean("crop_harvesting.crops." + material.name().toLowerCase(), true);
                String[] data = getData(enabled);

                UI.Item item = new UI.Item(data[0] + name, material);
                item.addLoreLines("§7State: " + data[0] + data[1], line);
                item.addLoreLine("§7Click to " + data[2]);
                item.onClick(clickType -> {
                    settings.set("crop_harvesting.crops." + material.name().toLowerCase(), !enabled);
                    reload();
                });
                content.addItem(item);
            });

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            // all on
            UI.Item all_on = new UI.Item("§aAll On", Material.LIME_DYE);
            all_on.addLoreLine("§7Click to turn all crops on");
            all_on.onClick(clickType -> {
                crops.forEach(crop -> {
                    String path = "crop_harvesting.crops." + crop.toLowerCase().replaceAll(" ", "_");
                    settings.set(path, true);
                });
                reload();
            });
            footer.setItem(all_on, 6);

            // all off
            UI.Item all_off = new UI.Item("§cAll Off", Material.RED_DYE);
            all_off.addLoreLine("§7Click to turn all crops off");
            all_off.onClick(clickType -> {
                crops.forEach(crop -> {
                    String path = "crop_harvesting.crops." + crop.toLowerCase().replaceAll(" ", "_");
                    settings.set(path, false);
                });
                reload();
            });
            footer.setItem(all_off, 7);

            page.setSection(content, 10);
            page.setSection(footer, 36);
        });

        return page;
    }

    public UI.Page settings_cropharvesting_tools() {

        UI.Page page = new UI.Page("Crop Harvesting - Tools", 36, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            // content
            UI.Section content = new UI.Section(3, 1);

            CFC settings = plugin.getCFCSettings();

            boolean no_tool_enabled = settings.getBoolean("crop_harvesting.tools.no_tool", true);
            String[] no_tool_data = getData(no_tool_enabled);

            UI.Item no_tool = new UI.Item(no_tool_data[0] + "No Tool", Material.BARRIER);
            no_tool.addLoreLines("§7State: " + no_tool_data[0] + no_tool_data[1], line, "§7Click to " + no_tool_data[2]);
            no_tool.onClick(clickType -> {
                settings.set("crop_harvesting.tools.no_tool", !no_tool_enabled);
                reload();
            });
            content.setItem(no_tool, 0);

            boolean hoe_enabled = settings.getBoolean("crop_harvesting.tools.hoe", true);
            String[] hoe_data = getData(hoe_enabled);

            UI.Item hoe = new UI.Item(hoe_data[0] + "Hoe", Material.IRON_HOE);
            hoe.addItemFlag(ItemFlag.HIDE_ATTRIBUTES);
            hoe.addLoreLines("§7State: " + hoe_data[0] + hoe_data[1], line, "§7Click to " + hoe_data[2]);
            hoe.onClick(clickType -> {
                settings.set("crop_harvesting.tools.hoe", !hoe_enabled);
                reload();
            });
            content.setItem(hoe, 2);

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            page.setSection(content, 12);
            page.setSection(footer, 27);
        });

        return page;
    }

    public UI.Page settings_betterdrops() {

        UI.Page page = new UI.Page("Better Drops", 36, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();
            CFC settings = plugin.getCFCSettings();

            // content
            UI.Section content = new UI.Section(7, 1);

            UI.Item blocks = new UI.Item("§eBlocks", Material.GRASS);
            blocks.addLoreLines("§7Choose the blocks that give better drops", line);
            blocks.addLoreLine("§7Click to view the block settings");
            blocks.onClick(clickType -> open(settings_betterdrops_blocks()));
            content.setItem(blocks, 0);

            UI.Item tools = new UI.Item("§eTools", Material.IRON_HOE);
            tools.addLoreLines("§7Choose the tools that can", "§7be used to get better drops", line);
            tools.addLoreLine("§7Click to view the tools settings");
            tools.addItemFlag(ItemFlag.HIDE_ATTRIBUTES);
            tools.onClick(clickType -> open(settings_betterdrops_tools()));
            content.setItem(tools, 2);

            String fortune_path = "better_drops.fortune";
            boolean fortune_enabled = settings.getBoolean(fortune_path, true);
            String[] data = getData(fortune_enabled);

            UI.Item fortune = new UI.Item(data[0] + "Fortune", Material.ENCHANTED_BOOK);
            fortune.addLoreLines("§7State: " + data[0] + data[1], line);
            fortune.addLoreLines("§7Decide whether the fortune enchantment", "§7will affect the drops or not", line);
            fortune.addLoreLine("§7Click to " + data[2]);
            fortune.onClick(clickType -> {
                settings.set(fortune_path, !fortune_enabled);
                reload();
            });
            content.setItem(fortune, 4);

            UI.Item permisisons = new UI.Item("§ePermissions", Material.FILLED_MAP);
            permisisons.addLoreLines("§7Set permissions to allow only", "§7certain players to get better drops", line);
            permisisons.addLoreLine("§7Click to view the permissions");
            permisisons.onClick(clickType -> open(settings_permissions("better_drops")));
            content.setItem(permisisons, 6);

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            page.setSection(content, 10);
            page.setSection(footer, 27);
        });

        return page;
    }

    public UI.Page settings_betterdrops_blocks() {

        UI.Page page = new UI.Page("Better Drops - Blocks", 45, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            CFC settings = plugin.getCFCSettings();
            List<String> blocks = List.of("Acacia Leaves", "Azalea Leaves", "Birch Leaves", "Dark Oak Leaves", "Flowering Azalea Leaves",
                    "Jungle Leaves", "Oak Leaves", "Spruce Leaves", "Grass", "Tall Grass", "Fern", "Large Fern");

            // content
            UI.Section content = new UI.Section(7, 2);

            blocks.forEach(name -> {

                Material material = Material.valueOf(name.toUpperCase().replaceAll(" ", "_"));
                boolean enabled = settings.getBoolean("better_drops.blocks." + material.name().toLowerCase(), true);
                String[] data = getData(enabled);

                UI.Item item = new UI.Item(data[0] + name, material);
                item.addLoreLines("§7State: " + data[0] + data[1], line);
                item.addLoreLine("§7Click to " + data[2]);
                item.onClick(clickType -> {
                    settings.set("better_drops.blocks." + material.name().toLowerCase(), !enabled);
                    reload();
                });
                content.addItem(item);
            });

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            // all on
            UI.Item all_on = new UI.Item("§aAll On", Material.LIME_DYE);
            all_on.addLoreLine("§7Click to turn all blocks on");
            all_on.onClick(clickType -> {
                blocks.forEach(block -> {
                    String path = "better_drops.blocks." + block.toLowerCase().replaceAll(" ", "_");
                    settings.set(path, true);
                });
                reload();
            });
            footer.setItem(all_on, 6);

            // all off
            UI.Item all_off = new UI.Item("§cAll Off", Material.RED_DYE);
            all_off.addLoreLine("§7Click to turn all blocks off");
            all_off.onClick(clickType -> {
                blocks.forEach(block -> {
                    String path = "better_drops.blocks." + block.toLowerCase().replaceAll(" ", "_");
                    settings.set(path, false);
                });
                reload();
            });
            footer.setItem(all_off, 7);

            page.setSection(content, 10);
            page.setSection(footer, 36);
        });

        return page;
    }

    public UI.Page settings_betterdrops_tools() {

        UI.Page page = new UI.Page("Better Drops - Tools", 36, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            // content
            UI.Section content = new UI.Section(3, 1);

            CFC settings = plugin.getCFCSettings();

            boolean no_tool_enabled = settings.getBoolean("better_drops.tools.no_tool", true);
            String[] no_tool_data = getData(no_tool_enabled);

            UI.Item no_tool = new UI.Item(no_tool_data[0] + "No Tool", Material.BARRIER);
            no_tool.addLoreLines("§7State: " + no_tool_data[0] + no_tool_data[1], line, "§7Click to " + no_tool_data[2]);
            no_tool.onClick(clickType -> {
                settings.set("better_drops.tools.no_tool", !no_tool_enabled);
                reload();
            });
            content.setItem(no_tool, 0);

            boolean hoe_enabled = settings.getBoolean("better_drops.tools.hoe", true);
            String[] hoe_data = getData(hoe_enabled);

            UI.Item hoe = new UI.Item(hoe_data[0] + "Hoe", Material.IRON_HOE);
            hoe.addItemFlag(ItemFlag.HIDE_ATTRIBUTES);
            hoe.addLoreLines("§7State: " + hoe_data[0] + hoe_data[1], line, "§7Click to " + hoe_data[2]);
            hoe.onClick(clickType -> {
                settings.set("better_drops.tools.hoe", !hoe_enabled);
                reload();
            });
            content.setItem(hoe, 2);

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            page.setSection(content, 12);
            page.setSection(footer, 27);
        });

        return page;
    }

    public UI.Page settings_sneaking() {

        UI.Page page = new UI.Page("Sneaking", 36, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();
            CFC settings = plugin.getCFCSettings();

            UI.Section content = new UI.Section(7, 1);

            // blocks
            UI.Item blocks = new UI.Item("§eBlocks", Material.WHEAT_SEEDS);
            blocks.addLoreLines("§7Choose the blocks that will grow", "§7faster when a player is sneaking", line);
            blocks.addLoreLine("§7Click to view the crops settings");
            blocks.onClick(clickType -> open(settings_sneaking_blocks()));
            content.setItem(blocks, 0);

            int range = settings.getInt("sneaking.range");

            // range
            UI.Item rangeItem = new UI.Item("§eRange", Material.MAP);
            rangeItem.addLoreLines("§7Players sneaking in a range of §6" + range, "§7blocks will increase the growth speed", line);
            rangeItem.addLoreLines("§cA too high range could cause", "§cthe server to lag or crash", line);
            rangeItem.addLoreLine("§7Click to change");
            rangeItem.onClick(clickType -> {
                AnvilGUI.Builder builder = new AnvilGUI.Builder();
                builder.plugin(plugin);
                builder.title("Enter a number");
                builder.text(range + "");
                builder.itemLeft(new ItemStack(Material.MAP));
                builder.onLeftInputClick(p2 -> history.openPage(0));
                builder.onClose(p2 -> history.openPage(0));
                builder.onComplete((p2, text) -> {
                    if (!StringUtils.isNumeric(text)) return AnvilGUI.Response.text("not a number");
                    settings.set("sneaking.range", Integer.parseInt(text));
                    return AnvilGUI.Response.close();
                });
                builder.open(p);
            });
            content.setItem(rangeItem, 2);

            int chance = settings.getInt("sneaking.chance");

            // chance
            UI.Item chanceItem = new UI.Item("§eChance", Material.MAP);
            chanceItem.addLoreLines("§7The chance of a crop growing is", "§6" + chance + "% §7every 5 ticks (0.25 seconds)", line);
            chanceItem.addLoreLine("§7Click to change");
            chanceItem.onClick(clickType -> {
                AnvilGUI.Builder builder = new AnvilGUI.Builder();
                builder.plugin(plugin);
                builder.title("Enter a number");
                builder.text(chance + "");
                builder.itemLeft(new ItemStack(Material.MAP));
                builder.onLeftInputClick(p2 -> history.openPage(0));
                builder.onClose(p2 -> history.openPage(0));
                builder.onComplete((p2, text) -> {
                    if (!StringUtils.isNumeric(text)) return AnvilGUI.Response.text("not a number");
                    if (Integer.parseInt(text) > 100) return AnvilGUI.Response.text("can't be > 100");
                    settings.set("sneaking.chance", Integer.parseInt(text));
                    return AnvilGUI.Response.close();
                });
                builder.open(p);
            });
            content.setItem(chanceItem, 4);

            // permissions
            UI.Item permissions = new UI.Item("§ePermissions", Material.FILLED_MAP);
            permissions.addLoreLines("§7Set permissions to allow only certain players", "§7sneaking to increase the growth speed", line);
            permissions.addLoreLine("§7Click to view the permissions");
            permissions.onClick(clickType -> open(settings_permissions("sneaking")));
            content.setItem(permissions, 6);

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            page.setSection(content, 10);
            page.setSection(footer, 27);
        });

        return page;
    }

    public UI.Page settings_sneaking_blocks() {

        UI.Page page = new UI.Page("Sneaking - Blocks", 54, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            CFC settings = plugin.getCFCSettings();
            List<String> blocks = List.of("Wheat Seeds", "Beetroot Seeds", "Potato", "Carrot", "Cocoa Beans", "Pumpkin Seeds",
                    "Melon Seeds", "Nether Wart", "Acacia Sapling", "Azalea", "Birch Sapling", "Dark Oak Sapling",
                    "Flowering Azalea", "Jungle Sapling", "Oak Sapling", "Spruce Sapling");

            // content
            UI.Section content = new UI.Section(7, 3);

            blocks.forEach(name -> {

                Material material = Material.valueOf(name.toUpperCase().replaceAll(" ", "_"));
                boolean enabled = settings.getBoolean("sneaking.blocks." + material.name().toLowerCase(), true);
                String[] data = getData(enabled);

                UI.Item item = new UI.Item(data[0] + name, material);
                item.addLoreLines("§7State: " + data[0] + data[1], line);
                item.addLoreLine("§7Click to " + data[2]);
                item.onClick(clickType -> {
                    settings.set("sneaking.blocks." + material.name().toLowerCase(), !enabled);
                    reload();
                });
                content.addItem(item);
            });

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            // all on
            UI.Item all_on = new UI.Item("§aAll On", Material.LIME_DYE);
            all_on.addLoreLine("§7Click to turn all blocks on");
            all_on.onClick(clickType -> {
                blocks.forEach(block -> {
                    String path = "sneaking.blocks." + block.toLowerCase().replaceAll(" ", "_");
                    settings.set(path, true);
                });
                reload();
            });
            footer.setItem(all_on, 6);

            // all off
            UI.Item all_off = new UI.Item("§cAll Off", Material.RED_DYE);
            all_off.addLoreLine("§7Click to turn all blocks off");
            all_off.onClick(clickType -> {
                blocks.forEach(block -> {
                    String path = "sneaking.blocks." + block.toLowerCase().replaceAll(" ", "_");
                    settings.set(path, false);
                });
                reload();
            });
            footer.setItem(all_off, 7);

            page.setSection(content, 10);
            page.setSection(footer, 45);
        });

        return page;
    }

    public UI.Page settings_shears() {

        UI.Page page = new UI.Page("Shears", 36, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            UI.Section content = new UI.Section(5, 1);

            // blocks
            UI.Item blocks = new UI.Item("§eBlocks", Material.TALL_GRASS);
            blocks.addLoreLines("§7Choose the blocks that can be sheared", line);
            blocks.addLoreLine("§7Click to view the blocks settings");
            blocks.onClick(clickType -> open(settings_shears_blocks()));
            content.setItem(blocks, 0);

            CFC settings = plugin.getCFCSettings();
            String fortune_path = "shears.fortune";
            boolean fortune_enabled = settings.getBoolean(fortune_path, true);
            String[] data = getData(fortune_enabled);

            // fortune
            UI.Item fortune = new UI.Item(data[0] + "Fortune", Material.ENCHANTED_BOOK);
            fortune.addLoreLines("§7State: " + data[0] + data[1], line);
            fortune.addLoreLines("§7Decide whether the fortune enchantment", "§7will affect the drops or not", line);
            fortune.addLoreLine("§7Click to " + data[2]);
            fortune.onClick(clickType -> {
                settings.set(fortune_path, !fortune_enabled);
                reload();
            });
            content.setItem(fortune, 2);

            // permissions
            UI.Item permissions = new UI.Item("§ePermissions", Material.FILLED_MAP);
            permissions.addLoreLines("§7Set permissions to allow only", "§7certain players to shear blocks", line);
            permissions.addLoreLine("§7Click to view the permissions");
            permissions.onClick(clickType -> open(settings_permissions("shears")));
            content.setItem(permissions, 4);

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            page.setSection(content, 11);
            page.setSection(footer, 27);
        });

        return page;
    }

    public UI.Page settings_shears_blocks() {

        UI.Page page = new UI.Page("Shears - Blocks", 45, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            CFC settings = plugin.getCFCSettings();
            List<String> blocks = List.of("Acacia Sapling", "Azalea", "Birch Sapling", "Dark Oak Sapling",
                    "Flowering Azalea", "Jungle Sapling", "Oak Sapling", "Spruce Sapling", "Tall Grass", "Seagrass",
                    "Large Fern");

            // content
            UI.Section content = new UI.Section(7, 3);

            blocks.forEach(name -> {

                Material material = Material.valueOf(name.toUpperCase().replaceAll(" ", "_"));
                boolean enabled = settings.getBoolean("shears.blocks." + material.name().toLowerCase(), true);
                String[] data = getData(enabled);

                if (name.equals("Seagrass")) name = "Tall Seagrass";
                UI.Item item = new UI.Item(data[0] + name, material);
                item.addLoreLines("§7State: " + data[0] + data[1], line);
                item.addLoreLine("§7Click to " + data[2]);
                item.onClick(clickType -> {
                    settings.set("shears.blocks." + material.name().toLowerCase(), !enabled);
                    reload();
                });
                content.addItem(item);
            });

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            // all on
            UI.Item all_on = new UI.Item("§aAll On", Material.LIME_DYE);
            all_on.addLoreLine("§7Click to turn all blocks on");
            all_on.onClick(clickType -> {
                blocks.forEach(block -> {
                    String path = "shears.blocks." + block.toLowerCase().replaceAll(" ", "_");
                    settings.set(path, true);
                });
                reload();
            });
            footer.setItem(all_on, 6);

            // all off
            UI.Item all_off = new UI.Item("§cAll Off", Material.RED_DYE);
            all_off.addLoreLine("§7Click to turn all blocks off");
            all_off.onClick(clickType -> {
                blocks.forEach(block -> {
                    String path = "shears.blocks." + block.toLowerCase().replaceAll(" ", "_");
                    settings.set(path, false);
                });
                reload();
            });
            footer.setItem(all_off, 7);

            page.setSection(content, 10);
            page.setSection(footer, 36);
        });

        return page;
    }

    public UI.Page settings_crafting() {

        UI.Page page = new UI.Page("Crafting", 36, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            // content
            UI.Section content = new UI.Section(3, 1);

            // recipes
            UI.Item recipes = new UI.Item("§eRecipes", Material.CRAFTING_TABLE);
            recipes.addLoreLines("§7Choose the recipes that can be used", line);
            recipes.addLoreLine("§7Click to view the recipe settings");
            recipes.onClick(clickType -> open(settings_crafting_recipes()));
            content.setItem(recipes, 0);

            // permissions
            UI.Item permissions = new UI.Item("§ePermissions", Material.FILLED_MAP);
            permissions.addLoreLines("§7Set permissions to allow only certain", "§7players to use the crafting recipes", line);
            permissions.addLoreLine("§7Click to view the permissions");
            permissions.onClick(clickType -> open(settings_permissions("crafting")));
            content.setItem(permissions, 2);

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            page.setSection(content, 12);
            page.setSection(footer, 27);
        });

        return page;
    }

    public UI.Page settings_crafting_recipes() {

        UI.Page page = new UI.Page("Crafting - Recipes", 45, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            CFC settings = plugin.getCFCSettings();
            List<String> recipes = List.of("Acacia Sapling", "Azalea", "Birch Sapling", "Dark Oak Sapling",
                    "Flowering Azalea", "Jungle Sapling", "Oak Sapling", "Spruce Sapling");

            // content
            UI.Section content = new UI.Section(7, 3);

            recipes.forEach(name -> {

                Material material = Material.valueOf(name.toUpperCase().replaceAll(" ", "_"));
                boolean enabled = settings.getBoolean("crafting.recipes." + material.name().toLowerCase(), true);
                String[] data = getData(enabled);

                UI.Item item = new UI.Item(data[0] + name, material);
                item.addLoreLines("§7State: " + data[0] + data[1], line);
                item.addLoreLine("§7Click to " + data[2]);
                item.onClick(clickType -> {
                    settings.set("crafting.recipes." + material.name().toLowerCase(), !enabled);
                    reload();
                });
                content.addItem(item);
            });

            ItemStack potion = new ItemStack(Material.POTION);
            PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();
            potionMeta.setBasePotionData(new PotionData(PotionType.WATER));
            potion.setItemMeta(potionMeta);

            boolean water_bottle_enabled = settings.getBoolean("crafting.recipes.potion", true);
            String[] water_bottle_data = getData(water_bottle_enabled);

            UI.Item water_bottle = UI.Item.fromItemStack(potion);
            water_bottle.setName(water_bottle_data[0] + "Water Bottle");
            water_bottle.addItemFlag(ItemFlag.HIDE_POTION_EFFECTS);
            water_bottle.addLoreLines("§7State: " + water_bottle_data[0] + water_bottle_data[1], line);
            water_bottle.addLoreLine("§7Click to " + water_bottle_data[2]);
            water_bottle.onClick(clickType -> {
                settings.set("crafting.recipes.potion", !water_bottle_enabled);
                reload();
            });
            content.addItem(water_bottle);

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            // all on
            UI.Item all_on = new UI.Item("§aAll On", Material.LIME_DYE);
            all_on.addLoreLine("§7Click to turn all recipes on");
            all_on.onClick(clickType -> {
                recipes.forEach(recipe -> {
                    String path = "crafting.recipes." + recipe.toLowerCase().replaceAll(" ", "_");
                    settings.set(path, true);
                });
                settings.set("crafting.recipes.potion", true);
                reload();
            });
            footer.setItem(all_on, 6);

            // all off
            UI.Item all_off = new UI.Item("§cAll Off", Material.RED_DYE);
            all_off.addLoreLine("§7Click to turn all recipes off");
            all_off.onClick(clickType -> {
                recipes.forEach(recipe -> {
                    String path = "crafting.recipes." + recipe.toLowerCase().replaceAll(" ", "_");
                    settings.set(path, false);
                });
                settings.set("crafting.recipes.potion", false);
                reload();
            });
            footer.setItem(all_off, 7);

            page.setSection(content, 10);
            page.setSection(footer, 36);
        });

        return page;
    }

    public UI.Page settings_bone_meal() {

        UI.Page page = new UI.Page("Bone Meal", 36, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            // content
            UI.Section content = new UI.Section(5, 1);

            // blocks
            UI.Item blocks = new UI.Item("§eBlocks", Material.DIRT);
            blocks.addLoreLines("§7Choose the blocks that bone", "§7meal can be used on", line);
            blocks.addLoreLines("§7Click to view the blocks settings");
            blocks.onClick(clickType -> {
                open(settings_bone_meal_blocks());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
            });
            content.setItem(blocks, 0);

            // strength
            UI.Item strength = new UI.Item("§eStrength", Material.BONE_MEAL);
            strength.addLoreLines("§7Changes the strength of", "§7bonemeal for all plants", line);
            strength.addLoreLines("§7Click to change the strength");
            content.setItem(strength, 2);

            // permissions
            UI.Item permissions = new UI.Item("§ePermissions", Material.FILLED_MAP);
            permissions.addLoreLines("§7Set permissions to allow only certain", "§7players to use bone mealing", line);
            permissions.addLoreLine("§7Click to view the permissions");
            permissions.onClick(clickType -> {
                open(settings_permissions("bonemealing"));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
            });
            content.setItem(permissions, 4);

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            page.setSection(content, 11);
            page.setSection(footer, 27);
        });

        return page;
    }

    public UI.Page settings_bone_meal_blocks() {

        UI.Page page = new UI.Page("Bone Meal - Blocks", 54, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        AtomicBoolean selection_view = new AtomicBoolean(false);
        AtomicInteger current_page = new AtomicInteger();

        page.onOpen(p -> {

            page.clear();

            CFC settings = plugin.getCFCSettings();

            List<String> blocks = new ArrayList<>(settings.getConfigurationSection("bonemeal.blocks").getKeys(false));

            // content
            UI.Section content = new UI.Section(7, 3);
            int pageSize = content.getWidth() * content.getHeight();

            for (int i = current_page.get() * pageSize; i < (current_page.get() + 1) * pageSize && i < blocks.size(); i++) {

                String name = blocks.get(i);

                String materialName = name.toUpperCase();

                switch (name) {
                    case "wheat" -> materialName = "WHEAT_SEEDS";
                    case "beetroots" -> materialName = "BEETROOT_SEEDS";
                    case "carrots" -> materialName = "CARROT";
                    case "potatoes" -> materialName = "POTATO";
                    case "cocoa" -> materialName = "COCOA_BEANS";
                    case "melon_stem" -> materialName = "MELON_SEEDS";
                    case "pumpkin_stem" -> materialName = "PUMPKIN_SEEDS";
                    case "sweet_berry_bush" -> materialName = "SWEET_BERRIES";
                }

                boolean enabled = settings.getBoolean("bonemeal.blocks." + name, true);
                String[] data = getData(enabled);

                Material material;
                if (selection_view.get()) {
                    if (enabled) material = Material.LIME_DYE;
                    else material = Material.RED_DYE;
                } else material = Material.valueOf(materialName);

                UI.Item item = new UI.Item("§e" + WordUtils.capitalize(materialName.toLowerCase().replaceAll("_", " ")), material);
                item.addLoreLines("§7State: " + data[0] + data[1]);
                item.addLoreLines(line, "§7Click to " + data[2]);
                item.onClick(clickType -> {
                    settings.set("bonemeal.blocks." + name, !enabled);
                    reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, volume, 0.7F);
                });
                content.addItem(item);
            }

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            // previous page
            if (current_page.get() > 0) {
                UI.Item previous_page = new UI.Item("§ePrevious Page", Material.ARROW);
                previous_page.addLoreLines("§7Left-Click to go to the previous page", "§7Shift-Left-Click to go to the first page");
                previous_page.onClick(clickType -> {
                    if (clickType == ClickType.LEFT) current_page.getAndDecrement();
                    else if (clickType == ClickType.SHIFT_LEFT) current_page.set(0);
                    reload();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
                });
                footer.setItem(previous_page, 3);
            }

            // next page
            if ((current_page.get() + 1) * pageSize < blocks.size()) {
                UI.Item next_page = new UI.Item("§eNext Page", Material.ARROW);
                next_page.addLoreLines("§7Left-Click to go to the next page", "§7Shift-Left-Click to go to the last page");
                next_page.onClick(clickType -> {
                    if (clickType == ClickType.LEFT) current_page.getAndIncrement();
                    else if (clickType == ClickType.SHIFT_LEFT)
                        current_page.set((int) Math.ceil((float) (blocks.size() - 1) / pageSize));
                    reload();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
                });
                footer.setItem(next_page, 5);
            }

            // all on
            UI.Item all_on = new UI.Item("§aAll On", Material.LIME_DYE);
            all_on.addLoreLine("§7Click to turn all blocks on");
            all_on.onClick(clickType -> {
                blocks.forEach(block -> {
                    String path = "bonemeal.blocks." + block.toLowerCase().replaceAll(" ", "_");
                    settings.set(path, true);
                });
                reload();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
            });
            footer.setItem(all_on, 6);

            // all off
            UI.Item all_off = new UI.Item("§cAll Off", Material.RED_DYE);
            all_off.addLoreLine("§7Click to turn all blocks off");
            all_off.onClick(clickType -> {
                blocks.forEach(block -> {
                    String path = "bonemeal.blocks." + block.toLowerCase().replaceAll(" ", "_");
                    settings.set(path, false);
                });
                reload();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
            });
            footer.setItem(all_off, 7);

            // toggle view
            UI.Item toggle_view = new UI.Item("§eToggle View", Material.COMPASS);
            if (selection_view.get()) toggle_view.addLoreLines("§7View: §6Selection");
            else toggle_view.addLoreLines("§7View: §6Item");
            toggle_view.addLoreLines(line, "§7Click to change");
            toggle_view.onClick(clickType -> {
                selection_view.set(!selection_view.get());
                reload();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
            });
            footer.setItem(toggle_view, 1);

            page.setSection(content, 10);
            page.setSection(footer, 45);
        });

        return page;

    }

    public UI.Page settings_right_clicking() {

        UI.Page page = new UI.Page("Right Clicking", 36, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            // content
            UI.Section content = new UI.Section(3, 1);

            // recipes
            UI.Item blocks = new UI.Item("§eBlocks", Material.CANDLE);
            blocks.addLoreLines("§7Choose the blocks that can be right clicked", line);
            blocks.addLoreLine("§7Click to view the blocks settings");
            blocks.onClick(clickType -> open(settings_right_clicking_blocks()));
            content.setItem(blocks, 0);

            // permissions
            UI.Item permissions = new UI.Item("§ePermissions", Material.FILLED_MAP);
            permissions.addLoreLines("§7Set permissions to allow only certain", "§7players to right click blocks", line);
            permissions.addLoreLine("§7Click to view the permissions");
            permissions.onClick(clickType -> open(settings_permissions("right_clicking")));
            content.setItem(permissions, 2);

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            page.setSection(content, 12);
            page.setSection(footer, 27);
        });

        return page;
    }

    public UI.Page settings_right_clicking_blocks() {

        UI.Page page = new UI.Page("Right Clicking - Blocks", 36, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            CFC settings = plugin.getCFCSettings();
            List<String> blocks = List.of("Candle", "Sea Pickle", "Carved Pumpkin", "Jack o Lantern");

            // content
            UI.Section content = new UI.Section(7, 3);

            blocks.forEach(name -> {

                Material material = Material.valueOf(name.toUpperCase().replaceAll(" ", "_"));
                boolean enabled = settings.getBoolean("right_clicking.blocks." + material.name().toLowerCase(), true);
                String[] data = getData(enabled);

                UI.Item item = new UI.Item(data[0] + name.replace("o ", "o'"), material);
                item.addLoreLines("§7State: " + data[0] + data[1], line);
                item.addLoreLine("§7Click to " + data[2]);
                item.onClick(clickType -> {
                    settings.set("right_clicking.blocks." + material.name().toLowerCase(), !enabled);
                    reload();
                });
                content.addItem(item);
            });

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            // all on
            UI.Item all_on = new UI.Item("§aAll On", Material.LIME_DYE);
            all_on.addLoreLine("§7Click to turn all blocks on");
            all_on.onClick(clickType -> {
                blocks.forEach(block -> {
                    String path = "right_clicking.blocks." + block.toLowerCase().replaceAll(" ", "_");
                    settings.set(path, true);
                });
                reload();
            });
            footer.setItem(all_on, 6);

            // all off
            UI.Item all_off = new UI.Item("§cAll Off", Material.RED_DYE);
            all_off.addLoreLine("§7Click to turn all blocks off");
            all_off.onClick(clickType -> {
                blocks.forEach(block -> {
                    String path = "right_clicking.blocks." + block.toLowerCase().replaceAll(" ", "_");
                    settings.set(path, false);
                });
                reload();
            });
            footer.setItem(all_off, 7);

            page.setSection(content, 10);
            page.setSection(footer, 27);
        });

        return page;

    }

    public UI.Page settings_experimental() {

        UI.Page page = new UI.Page("Experimental", 36, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            // content
            UI.Section content = new UI.Section(3, 1);

            // settings
            UI.Item settings = new UI.Item("§eSettings", Material.SPRUCE_SAPLING);
            settings.addLoreLines("§7Choose the settings that should be activated", line);
            settings.addLoreLine("§7Click to view the settings");
            settings.onClick(clickType -> open(settings_experimental_settings()));
            content.setItem(settings, 0);

            // permissions
            UI.Item permissions = new UI.Item("§ePermissions", Material.FILLED_MAP);
            permissions.addLoreLines("§7Set permissions to allow only certain", "§7players to use experimental features", line);
            permissions.addLoreLine("§7Click to view the permissions");
            permissions.onClick(clickType -> open(settings_permissions("experimental")));
            content.setItem(permissions, 2);

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            page.setSection(content, 12);
            page.setSection(footer, 27);
        });

        return page;
    }

    public UI.Page settings_experimental_settings() {

        UI.Page page = new UI.Page("Experimental - Settings", 36, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            CFC settings = plugin.getCFCSettings();

            // content
            UI.Section content = new UI.Section(7, 3);

            boolean enabled = settings.getBoolean("experimental.settings.mega_trees", true);
            String[] data = getData(enabled);

            UI.Item mega_trees = new UI.Item(data[0] + "Mega Trees", Material.SPRUCE_SAPLING);
            mega_trees.addLoreLines("§7State: " + data[0] + data[1], line);
            mega_trees.addLoreLine("§7Click to " + data[2]);
            mega_trees.onClick(clickType -> {
                settings.set("experimental.settings.mega_trees", !enabled);
                reload();
            });
            content.addItem(mega_trees);

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            // all on
            UI.Item all_on = new UI.Item("§aAll On", Material.LIME_DYE);
            all_on.addLoreLine("§7Click to turn all blocks on");
            all_on.onClick(clickType -> {
                settings.set("experimental.settings.mega_trees", true);
                reload();
            });
            footer.setItem(all_on, 6);

            // all off
            UI.Item all_off = new UI.Item("§cAll Off", Material.RED_DYE);
            all_off.addLoreLine("§7Click to turn all blocks off");
            all_off.onClick(clickType -> {
                settings.set("experimental.settings.mega_trees", false);
                reload();
            });
            footer.setItem(all_off, 7);

            page.setSection(content, 10);
            page.setSection(footer, 27);
        });

        return page;

    }

    public UI.Page settings_watering() {

        UI.Page page = new UI.Page("Watering", 54, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            // content
            UI.Section content = new UI.Section(3, 1);
            UI.Section content2 = new UI.Section(7, 1);

            // blocks
            UI.Item blocks = new UI.Item("§eBlocks", Material.GRASS_BLOCK);
            blocks.addLoreLines("§7Choose the blocks that can be watered", line);
            blocks.addLoreLines("§7Click to view the blocks settings");
            blocks.onClick(clickType -> open(settings_watering_blocks()));
            content.setItem(blocks, 0);

            // permissions
            UI.Item permissions = new UI.Item("§ePermissions", Material.FILLED_MAP);
            permissions.addLoreLines("§7Set permissions to allow only", "§7certain players to water blocks", line);
            permissions.addLoreLine("§7Click to view the permissions");
            permissions.onClick(clickType -> open(settings_permissions("watering")));
            content.setItem(permissions, 2);

            CFC settings = plugin.getCFCSettings();
            int range = settings.getInt("watering.range", 2);
            int strength = settings.getInt("watering.strength", 1);
            int duration = settings.getInt("watering.duration", 5);
            double chance = settings.getDouble("watering.chance", 0.33);

            // range
            UI.Item rangeItem = new UI.Item("§eRange", Material.FARMLAND);
            rangeItem.addLoreLines("§7Amount of blocks from center", "§7Range: §6" + range + " blocks", line, "§7Click to change");
            content2.setItem(rangeItem, 0);

            // duration
            UI.Item durationItem = new UI.Item("§eDuration", Material.CLOCK);
            durationItem.addLoreLines("§7Amount of seconds the effect lasts", "§7Duration: §6" + duration + " seconds", line, "§7Click to change");
            content2.setItem(durationItem, 2);

            // strength
            UI.Item strengthItem = new UI.Item("§eStrength", Material.NETHERITE_SWORD);
            strengthItem.addLoreLines("§7Amount of stages the crop grows", "§7Strength: §6" + strength + " stage(s)", line, "§7Click to change");
            strengthItem.addItemFlags(Arrays.asList(ItemFlag.values()));
            content2.setItem(strengthItem, 4);

            // chance
            UI.Item chanceItem = new UI.Item("§eChance", Material.SUNFLOWER);
            chanceItem.addLoreLines("§7Chance for each block in range to grow", "§7Chance: §6" + (chance * 100) + "%", line, "§7Click to change");
            content2.setItem(chanceItem, 6);

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            page.setSection(content, 12);
            page.setSection(content2, 28);
            page.setSection(footer, 45);
        });

        return page;
    }

    public UI.Page settings_watering_blocks() {

        UI.Page page = new UI.Page("Watering - Blocks", 36, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            page.setSection(footer, 27);
        });

        return page;
    }

    public UI.Page settings_poisoning() {

        UI.Page page = new UI.Page("Poisoning", 36, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            // content
            UI.Section content = new UI.Section(5, 1);

            // blocks
            UI.Item blocks = new UI.Item("§eBlocks", Material.GRASS_BLOCK);
            blocks.addLoreLines("§7Choose the blocks that can be poisoned", line);
            blocks.addLoreLines("§7Click to view the blocks settings");
            blocks.onClick(clickType -> open(settings_poisoning_blocks()));
            content.setItem(blocks, 0);

            // range
            UI.Item range = new UI.Item("§eRange", Material.MAP);
            range.addLoreLines("§7In a range of 2 blocks of the impact", "§7location blocks can be poisoned", line);
            range.addLoreLine("§7Click to change");
            content.setItem(range, 2);

            // permissions
            UI.Item permissions = new UI.Item("§ePermissions", Material.FILLED_MAP);
            permissions.addLoreLines("§7Set permissions to allow only", "§7certain players to poison blocks", line);
            permissions.addLoreLine("§7Click to view the permissions");
            permissions.onClick(clickType -> open(settings_permissions("poisoning")));
            content.setItem(permissions, 4);

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            page.setSection(content, 11);
            page.setSection(footer, 27);
        });

        return page;
    }

    public UI.Page settings_poisoning_blocks() {

        UI.Page page = new UI.Page("Poisoning - Blocks", 36, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            page.setSection(footer, 27);
        });

        return page;
    }

    public UI.Page settings_permissions(String setting) {

        UI.Page page = new UI.Page("Permissions", 54, plugin);

        History history = plugin.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            int currentPage = getCurrentPage(page);
            CFC settings = plugin.getCFCSettings();
            List<String> permissions = new ArrayList<>(settings.getStringList(setting + ".permissions"));

            // content
            UI.Section content = new UI.Section(9, 5);

            if (!permissions.isEmpty()) {
                for (int i = (currentPage - 1) * 45; i < currentPage * 45 && i < permissions.size(); i++) {

                    String permission = permissions.get(i);
                    UI.Item permissionItem = new UI.Item("§a" + permission, Material.FILLED_MAP);
                    permissionItem.addLoreLines("§7Click to edit", "§7Shift-right-click to remove");
                    permissionItem.onClick(clickType -> {
                        if (clickType == ClickType.SHIFT_RIGHT)
                            open(confirmation("Do you really want to remove " + permission, ct -> {
                                permissions.remove(permission);
                                settings.set(setting + ".permissions", permissions);
                                history.openPage(1);
                            }));
                        else {
                            AnvilGUI.Builder builder = new AnvilGUI.Builder();
                            builder.plugin(plugin);
                            builder.title("Enter a permission");
                            builder.text(permission);
                            builder.itemLeft(new ItemStack(Material.FILLED_MAP));
                            builder.onLeftInputClick(p2 -> history.openPage(0));
                            builder.onClose(p2 -> history.openPage(0));
                            builder.onComplete((p2, text) -> {
                                if (permissions.contains(text)) return AnvilGUI.Response.text("already existing");
                                permissions.remove(permission);
                                permissions.add(text);
                                settings.set(setting + ".permissions", permissions);
                                return AnvilGUI.Response.close();
                            });
                            builder.open(player);
                        }
                    });

                    content.addItem(permissionItem);
                }
            }

            // no permissions
            else {
                UI.Item noPermissions = new UI.Item("§cNo Permissions", Material.RED_STAINED_GLASS_PANE);
                noPermissions.addLoreLines("§7You can add permissions by", "§7clicking the \"Add\" button");
                content.setItem(noPermissions, 22);
            }

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            if (currentPage > 1) footer.setItem(getPreviousPageItem(permissions.size()), 3);
            footer.setItem(getNavigationItem(), 4);
            if (currentPage * 45 < permissions.size()) footer.setItem(getNextPageItem(permissions.size()), 5);

            // delete all
            UI.Item delete_all = new UI.Item("§cDelete All", Material.RED_DYE);
            delete_all.addLoreLine("§7Click to delete all permisisons");
            delete_all.onClick(clickType -> open(confirmation("Do you really want to delete all permissions?", ct -> {
                settings.set(setting + ".permissions", new ArrayList<>());
                history.openPage(1);
            })));
            footer.setItem(delete_all, 7);

            // add
            UI.Item add = new UI.Item("§aAdd", Material.SLIME_BALL);
            add.addLoreLine("§7Click to add a new permission");
            add.onClick(clickType -> {
                AnvilGUI.Builder builder = new AnvilGUI.Builder();
                builder.plugin(plugin);
                builder.title("Enter a permission");
                builder.text("permission");
                builder.itemLeft(new ItemStack(Material.FILLED_MAP));
                builder.onLeftInputClick(p2 -> history.openPage(0));
                builder.onClose(p2 -> history.openPage(0));
                builder.onComplete((p2, text) -> {
                    if (permissions.contains(text)) return AnvilGUI.Response.text("already existing");
                    permissions.add(text);
                    settings.set(setting + ".permissions", permissions);
                    return AnvilGUI.Response.close();
                });
                builder.open(player);
            });
            footer.setItem(add, 8);

            page.setSection(content, 0);
            page.setSection(footer, 45);
        });

        return page;
    }

    // ITEMS
    private UI.Item getNavigationItem() {
        History history = plugin.getHistory(player);

        UI.Item navigation = new UI.Item();
        navigation.setMaterial(Material.BARRIER);
        if (history.getPage(1) != null) {
            navigation.setName("§eNavigation");
            navigation.clearLore();
            navigation.addLoreLine("§7Click to return to the previous page");
            navigation.addLoreLine("§7Right-click view the history");
            navigation.addLoreLine("§7Middle-click to close");
            navigation.onClick(clickType -> {
                switch (clickType) {
                    case RIGHT -> {
                        open(history());
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
                    }
                    case MIDDLE -> {
                        player.closeInventory();
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
                    }
                    default -> {
                        history.openPage(1);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
                    }
                }
            });
        } else {
            navigation.setName("§eClose");
            navigation.clearLore();
            navigation.addLoreLine("§7Click to close");
            navigation.onClick(clickType -> {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, volume, 1);
            });
        }
        return navigation;
    }

    private UI.Item getPreviousPageItem(int amount) {
        UI.Page page = plugin.getHistory(player).getPage(0);

        UI.Item previousPage = new UI.Item();
        previousPage.addLoreLine("§7Click to go to the previous page");
        previousPage.addLoreLine("§7Right-click to open the page list");
        previousPage.addLoreLine("§7Middle-click to jump to the beginning");
        previousPage.setName("§ePrevious page");
        previousPage.setMaterial(Material.ARROW);
        previousPage.onClick(clickType -> {
            switch (clickType) {
                case RIGHT -> open(pages((int) Precision.round((float) amount / 45, 0, 0)));
                case MIDDLE -> {
                    setCurrentPage(page, 1);
                    reload();
                }
                default -> {
                    setCurrentPage(page, getCurrentPage(page) - 1);
                    reload();
                }
            }
        });
        return previousPage;
    }

    private UI.Item getNextPageItem(int amount) {
        UI.Page page = plugin.getHistory(player).getPage(0);

        UI.Item nextPage = new UI.Item();
        nextPage.addLoreLine("§7Click to go to the next page");
        nextPage.addLoreLine("§7Right-click to open the page list");
        nextPage.addLoreLine("§7Middle-click to jump to the end");
        nextPage.setName("§eNext page");
        nextPage.setMaterial(Material.ARROW);
        nextPage.onClick(clickType -> {
            switch (clickType) {
                case RIGHT -> open(pages((int) Precision.round((float) amount / 45, 0, 0)));
                case MIDDLE -> {
                    setCurrentPage(page, (int) Precision.round((float) amount / 45, 0, 0));
                    reload();
                }
                default -> {
                    setCurrentPage(page, getCurrentPage(page) + 1);
                    reload();
                }
            }
        });
        return nextPage;
    }

    // CURRENT PAGE
    public Integer getCurrentPage(UI.Page page) {
        int currentPage = 1;
        if (plugin.getPage(player).containsKey(page.getTitle()))
            currentPage = plugin.getPage(player).get(page.getTitle());
        return currentPage;
    }

    public void setCurrentPage(UI.Page page, Integer index) {
        plugin.getPage(player).put(page.getTitle(), index);
    }


    public String[] getData(boolean enabled) {
        String[] data = new String[3];

        // enabled
        if (enabled) {
            data[0] = "§a";
            data[1] = "On";
            data[2] = "disable";
        }

        // disabled
        else {
            data[0] = "§c";
            data[1] = "Off";
            data[2] = "enable";
        }
        return data;
    }
}
