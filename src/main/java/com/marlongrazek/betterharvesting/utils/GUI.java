//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.marlongrazek.betterharvesting.utils;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.datafile.DataFile;
import com.marlongrazek.ui.History;
import com.marlongrazek.ui.UI.Item;
import com.marlongrazek.ui.UI.Page;
import com.marlongrazek.ui.UI.Section;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.wesjd.anvilgui.AnvilGUI.Builder;
import net.wesjd.anvilgui.AnvilGUI.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class GUI {
    private final Main plugin;
    private final Player player;
    private final Item backgroundItem;
    private final String line;

    public GUI(Player player, Main plugin) {
        this.backgroundItem = new Item(" ", Material.GRAY_STAINED_GLASS_PANE);
        this.line = "§7-----";
        this.player = player;
        this.plugin = plugin;
    }

    public void open(Page page) {
        page.open(this.player);
    }

    public void reload() {
        this.plugin.getHistory(this.player).openPage(0);
    }

    public Page confirmation(String reason, Consumer<ClickType> yesAction) {
        Page page = new Page("Confirmation", 36, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            footer.setItem(this.getNavigationItem(), 4);
            Item no = new Item("§cCancel", Material.RED_STAINED_GLASS_PANE);
            no.addLoreLine("§7Click to cancel");
            no.onClick((clickType) -> {
                history.openPage(1);
            });
            page.setItem(no, 11);
            Item yes;
            if (!reason.isEmpty()) {
                yes = new Item("§f" + reason, Material.FILLED_MAP);
                page.setItem(yes, 13);
            }

            yes = new Item("§aConfirm", Material.LIME_STAINED_GLASS_PANE);
            yes.addLoreLine("§7Click to confirm");
            yes.onClick(yesAction);
            page.setItem(yes, 15);
            page.setSection(footer, 27);
        });
        return page;
    }

    public Page history() {
        Page page = new Page("Page History", 54, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            int currentPage = this.getCurrentPage(page);
            ArrayList<Page> pages = history.list();
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            if (currentPage > 1) {
                footer.setItem(this.getPreviousPageItem(pages.size()), 3);
            }

            Item navigation = new Item();
            navigation.setName("§eNavigation");
            navigation.addLoreLine("§7Click to go back");
            navigation.addLoreLine("§7Middle-click to close");
            navigation.onClick((clickType) -> {
                if (clickType == ClickType.MIDDLE) {
                    p.closeInventory();
                } else {
                    history.openPage(1);
                }

            });
            navigation.setMaterial(Material.BARRIER);
            footer.setItem(navigation, 4);
            if (pages.size() > currentPage * 45) {
                footer.setItem(this.getNextPageItem(pages.size()), 5);
            }

            Section content = new Section(9, 5);

            for(int i = (currentPage - 1) * 45; i < currentPage * 45 && i < pages.size() - 1; ++i) {
                Page pageI = (Page)pages.get(i);
                if (pageI != null) {
                    Item pageItem = new Item("§a" + pageI.getTitle(), Material.FILLED_MAP);
                    pageItem.addLoreLine("§7Click to return to that page");
                    int selectedPage = pages.size() - (i + 1);
                    pageItem.onClick((clickType) -> {
                        history.openPage(selectedPage);
                    });
                    page.addItem(pageItem);
                }
            }

            page.setSection(content, 0);
            page.setSection(footer, 45);
        });
        return page;
    }

    public Page pages(int amount) {
        Page page = new Page("Pages", 54, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            int currentPage = this.getCurrentPage(page);
            int previousCurrentPage = this.getCurrentPage(history.getPage(1));
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            Item nextPage;
            if (currentPage > 1) {
                nextPage = new Item();
                nextPage.setName("§ePrevious page");
                nextPage.setMaterial(Material.ARROW);
                nextPage.addLoreLine("§7Click to go to the previous page");
                nextPage.addLoreLine("§7Middle-click to jump to the beginning");
                nextPage.onClick((clickType) -> {
                    if (clickType == ClickType.MIDDLE) {
                        this.setCurrentPage(page, 1);
                    } else {
                        this.setCurrentPage(page, currentPage - 1);
                    }

                    this.reload();
                });
                footer.setItem(nextPage, 3);
            }

            footer.setItem(this.getNavigationItem(), 4);
            if (amount > currentPage * 45) {
                nextPage = new Item();
                nextPage.setName("§eNext page");
                nextPage.setMaterial(Material.ARROW);
                nextPage.addLoreLine("§7Click to go to the next page");
                nextPage.addLoreLine("§7Middle-click to jump to the end");
                nextPage.onClick((clickType) -> {
                    if (clickType == ClickType.MIDDLE) {
                        this.setCurrentPage(page, (int)Precision.round((float)amount / 45.0F, 0, 0));
                    } else {
                        this.setCurrentPage(page, currentPage + 1);
                    }

                    this.reload();
                });
                footer.setItem(nextPage, 5);
            }

            Section content = new Section(9, 5);

            for(int i = (currentPage - 1) * 45; i < currentPage * 45 && i < amount; ++i) {
                Item pageItem = new Item();
                pageItem.addLoreLine("§7Click to view this page");
                if (i + 1 == previousCurrentPage) {
                    pageItem.setName("§aPage " + (i + 1) + " [current]");
                    pageItem.setMaterial(Material.FILLED_MAP);
                } else {
                    pageItem.setName("§aPage " + (i + 1));
                    pageItem.setMaterial(Material.MAP);
                }

                int newPage = i + 1;
                pageItem.onClick((clickType) -> {
                    this.setCurrentPage(history.getPage(1), newPage);
                    history.openPage(1);
                });
                page.addItem(pageItem);
            }

            page.setSection(content, 0);
            page.setSection(footer, 45);
        });
        return page;
    }

    public Page settings() {
        Page page = new Page("Settings", 54, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            DataFile settings = this.plugin.getDataFile("settings");
            Section header = new Section(9, 1);
            header.fill(this.backgroundItem);
            Section sidebar = new Section(1, 4);
            sidebar.fill(this.backgroundItem);
            Section content = new Section(5, 2);
            String crop_harvesting_path = "crop_harvesting.enabled";
            boolean crop_harvesting_enabled = settings.getBoolean(crop_harvesting_path, true);
            String[] crop_harvesting_data = this.getData(crop_harvesting_enabled);
            Item crop_harvesting = new Item(crop_harvesting_data[0] + "Crop Harvesting", Material.WHEAT_SEEDS);
            crop_harvesting.addLoreLines(new String[]{"§7Current: " + crop_harvesting_data[0] + crop_harvesting_data[1], "§7-----"});
            crop_harvesting.addLoreLine("§7Click to " + crop_harvesting_data[2]);
            if (crop_harvesting_enabled) {
                crop_harvesting.addLoreLine("§7Right-click to customize");
            }

            crop_harvesting.onClick((clickType) -> {
                if (clickType == ClickType.RIGHT && crop_harvesting_enabled) {
                    this.open(this.settings_cropharvesting());
                } else {
                    settings.set(crop_harvesting_path, !crop_harvesting_enabled);
                    this.reload();
                }

            });
            content.addItem(crop_harvesting);
            String better_drops_path = "better_drops.enabled";
            boolean better_drops_enabled = settings.getBoolean(better_drops_path, true);
            String[] better_drops_data = this.getData(better_drops_enabled);
            Item better_drops = new Item(better_drops_data[0] + "Better Drops", Material.IRON_HOE);
            better_drops.addItemFlag(ItemFlag.HIDE_ATTRIBUTES);
            better_drops.addLoreLines(new String[]{"§7Current: " + better_drops_data[0] + better_drops_data[1], "§7-----"});
            better_drops.addLoreLine("§7Click to " + better_drops_data[2]);
            if (better_drops_enabled) {
                better_drops.addLoreLine("§7Right-click to customize");
            }

            better_drops.onClick((clickType) -> {
                if (clickType == ClickType.RIGHT && better_drops_enabled) {
                    this.open(this.settings_betterdrops());
                } else {
                    settings.set(better_drops_path, !better_drops_enabled);
                    this.reload();
                }

            });
            content.addItem(better_drops);
            String sneaking_path = "sneaking.enabled";
            boolean sneaking_enabled = settings.getBoolean(sneaking_path, true);
            String[] sneaking_data = this.getData(sneaking_enabled);
            Item sneaking = new Item(sneaking_data[0] + "Sneaking", Material.FILLED_MAP);
            sneaking.addLoreLines(new String[]{"§7Current: " + sneaking_data[0] + sneaking_data[1], "§7-----"});
            sneaking.addLoreLine("§7Click to " + sneaking_data[2]);
            if (sneaking_enabled) {
                sneaking.addLoreLine("§7Right-click to customize");
            }

            sneaking.onClick((clickType) -> {
                if (clickType == ClickType.RIGHT && sneaking_enabled) {
                    this.open(this.settings_sneaking());
                } else {
                    settings.set(sneaking_path, !sneaking_enabled);
                    this.reload();
                }

            });
            content.addItem(sneaking);
            String shearing_path = "shearing.enabled";
            boolean shearing_enabled = settings.getBoolean(shearing_path, true);
            String[] shearing_data = this.getData(shearing_enabled);
            Item shearing = new Item(shearing_data[0] + "Shearing", Material.SHEARS);
            shearing.addLoreLines(new String[]{"§7Current: " + shearing_data[0] + shearing_data[1], "§7-----"});
            shearing.addLoreLine("§7Click to " + shearing_data[2]);
            if (shearing_enabled) {
                shearing.addLoreLine("§7Right-click to customize");
            }

            shearing.onClick((clickType) -> {
                if (clickType == ClickType.RIGHT && shearing_enabled) {
                    this.open(this.settings_shearing());
                } else {
                    settings.set(shearing_path, !shearing_enabled);
                    this.reload();
                }

            });
            content.addItem(shearing);
            String crafting_path = "crafting.enabled";
            boolean crafting_enabled = settings.getBoolean(crafting_path, true);
            String[] crafting_data = this.getData(crafting_enabled);
            Item crafting = new Item(crafting_data[0] + "Crafting Recipes", Material.CRAFTING_TABLE);
            crafting.addLoreLines(new String[]{"§7Current: " + crafting_data[0] + crafting_data[1], "§7-----"});
            crafting.addLoreLine("§7Click to " + crafting_data[2]);
            if (crafting_enabled) {
                crafting.addLoreLine("§7Right-click to customize");
            }

            crafting.onClick((clickType) -> {
                if (clickType == ClickType.RIGHT && crafting_enabled) {
                    this.open(this.settings_crafting());
                } else {
                    settings.set(crafting_path, !crafting_enabled);
                    this.reload();
                }

            });
            content.addItem(crafting);
            String bonemeal_path = "bonemeal.enabled";
            boolean bonemeal_enabled = settings.getBoolean(bonemeal_path, true);
            String[] bonemeal_data = this.getData(bonemeal_enabled);
            Item bonemeal = new Item(bonemeal_data[0] + "Bone Mealing", Material.BONE_MEAL);
            bonemeal.addLoreLines(new String[]{"§7Current: " + bonemeal_data[0] + bonemeal_data[1], "§7-----"});
            bonemeal.addLoreLine("§7Click to " + bonemeal_data[2]);
            if (bonemeal_enabled) {
                bonemeal.addLoreLine("§7Right-click to customize");
            }

            bonemeal.onClick((clickType) -> {
                if (clickType == ClickType.RIGHT && bonemeal_enabled) {
                    this.open(this.settings_bonemealing());
                } else {
                    settings.set(bonemeal_path, !bonemeal_enabled);
                    this.reload();
                }

            });
            content.addItem(bonemeal);
            String right_click_path = "right_clicking.enabled";
            boolean right_click_enabled = settings.getBoolean(right_click_path, true);
            String[] right_click_data = this.getData(right_click_enabled);
            Item right_click = new Item(right_click_data[0] + "Right Click", Material.FILLED_MAP);
            right_click.addLoreLines(new String[]{"§7Current: " + right_click_data[0] + right_click_data[1], "§7-----"});
            right_click.addLoreLine("§7Click to " + right_click_data[2]);
            if (right_click_enabled) {
                right_click.addLoreLine("§7Right-click to customize");
            }

            right_click.onClick((clickType) -> {
                if (clickType == ClickType.RIGHT && right_click_enabled) {
                    this.open(this.settings_right_clicking());
                } else {
                    settings.set(right_click_path, !right_click_enabled);
                    this.reload();
                }

            });
            content.addItem(right_click);
            String watering_path = "watering.enabled";
            boolean watering_enabled = settings.getBoolean(watering_path, true);
            String[] watering_data = this.getData(watering_enabled);
            ItemStack bottle = new ItemStack(Material.SPLASH_POTION);
            PotionMeta meta = (PotionMeta)bottle.getItemMeta();
            meta.setBasePotionData(new PotionData(PotionType.WATER));
            bottle.setItemMeta(meta);
            Item watering = Item.fromItemStack(bottle);
            watering.setName(watering_data[0] + "Watering");
            watering.addItemFlag(ItemFlag.HIDE_POTION_EFFECTS);
            watering.addLoreLines(new String[]{"§7Current: " + watering_data[0] + watering_data[1], "§7-----"});
            watering.addLoreLine("§7Click to " + watering_data[2]);
            if (watering_enabled) {
                watering.addLoreLine("§7Right-click to customize (coming soon)");
            }

            watering.onClick((clickType) -> {
                settings.set(watering_path, !watering_enabled);
                this.reload();
            });
            content.addItem(watering);
            String experimental_path = "experimental.enabled";
            boolean experimental_enabled = settings.getBoolean(experimental_path, true);
            String[] experimental_data = this.getData(experimental_enabled);
            Item experimental = new Item(experimental_data[0] + "Experimental", Material.NETHER_STAR);
            experimental.addLoreLines(new String[]{"§7Current: " + experimental_data[0] + experimental_data[1], "§7-----"});
            experimental.addLoreLine("§7Click to " + experimental_data[2]);
            if (experimental_enabled) {
                experimental.addLoreLine("§7Right-click to customize");
            }

            experimental.onClick((clickType) -> {
                if (clickType == ClickType.RIGHT && experimental_enabled) {
                    this.open(this.settings_experimental());
                } else {
                    settings.set(experimental_path, !experimental_enabled);
                    this.reload();
                }

            });
            content.addItem(experimental);
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            footer.setItem(this.getNavigationItem(), 4);
            page.setSection(header, 0);
            page.setSection(sidebar, 9);
            page.setSection(sidebar, 17);
            page.setSection(content, 20);
            page.setSection(footer, 45);
        });
        return page;
    }

    public Page settings_cropharvesting() {
        Page page = new Page("Crop Harvesting", 36, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            DataFile settings = this.plugin.getDataFile("settings");
            Section content = new Section(7, 1);
            Item crops = new Item("§eCrops", Material.WHEAT_SEEDS);
            crops.addLoreLines(new String[]{"§7Choose the crops that can be harvested", "§7-----"});
            crops.addLoreLine("§7Click to view the crops settings");
            crops.onClick((clickType) -> {
                this.open(this.settings_cropharvesting_crops());
            });
            content.setItem(crops, 0);
            Item tools = new Item("§eTools", Material.IRON_HOE);
            tools.addLoreLines(new String[]{"§7Choose the tools that", "§7can be used to harvest", "§7-----"});
            tools.addLoreLine("§7Click to view the tools settings");
            tools.addItemFlag(ItemFlag.HIDE_ATTRIBUTES);
            tools.onClick((clickType) -> {
                this.open(this.settings_cropharvesting_tools());
            });
            content.setItem(tools, 2);
            String fortune_path = "crop_harvesting.fortune";
            boolean fortune_enabled = settings.getBoolean(fortune_path, true);
            String[] data = this.getData(fortune_enabled);
            Item fortune = new Item(data[0] + "Fortune", Material.ENCHANTED_BOOK);
            fortune.addLoreLines(new String[]{"§7State: " + data[0] + data[1], "§7-----"});
            fortune.addLoreLines(new String[]{"§7Decide whether the fortune enchantment", "§7will affect the drops or not", "§7-----"});
            fortune.addLoreLine("§7Click to " + data[2]);
            fortune.onClick((clickType) -> {
                settings.set(fortune_path, !fortune_enabled);
                this.reload();
            });
            content.setItem(fortune, 4);
            Item permissions = new Item("§ePermissions", Material.FILLED_MAP);
            permissions.addLoreLines(new String[]{"§7Set permissions to allow only", "§7certain players to harvest", "§7-----"});
            permissions.addLoreLine("§7Click to view the permissions");
            permissions.onClick((clickType) -> {
                this.open(this.settings_permissions("crop_harvesting"));
            });
            content.setItem(permissions, 6);
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            footer.setItem(this.getNavigationItem(), 4);
            page.setSection(content, 10);
            page.setSection(footer, 27);
        });
        return page;
    }

    public Page settings_cropharvesting_crops() {
        Page page = new Page("Crop Harvesting - Crops", 36, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            DataFile settings = this.plugin.getDataFile("settings");
            List<String> crops = List.of("Wheat Seeds", "Beetroot Seeds", "Potato", "Carrot", "Cocoa Beans", "Pumpkin Seeds", "Melon Seeds");
            Section content = new Section(7, 1);
            crops.forEach((name) -> {
                Material material = Material.valueOf(name.toUpperCase().replaceAll(" ", "_"));
                boolean enabled = settings.getBoolean("crop_harvesting.crops." + material.name().toLowerCase(), true);
                String[] data = this.getData(enabled);
                Item item = new Item(data[0] + name, material);
                item.addLoreLines(new String[]{"§7State: " + data[0] + data[1], "§7-----"});
                item.addLoreLine("§7Click to " + data[2]);
                item.onClick((clickType) -> {
                    String var10001 = material.name().toLowerCase();
                    settings.set("crop_harvesting.crops." + var10001, !enabled);
                    this.reload();
                });
                content.addItem(item);
            });
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            footer.setItem(this.getNavigationItem(), 4);
            Item all_on = new Item("§aAll On", Material.LIME_DYE);
            all_on.addLoreLine("§7Click to turn all crops on");
            all_on.onClick((clickType) -> {
                crops.forEach((crop) -> {
                    String var10000 = crop.toLowerCase();
                    String path = "crop_harvesting.crops." + var10000.replaceAll(" ", "_");
                    settings.set(path, true);
                });
                this.reload();
            });
            footer.setItem(all_on, 6);
            Item all_off = new Item("§cAll Off", Material.RED_DYE);
            all_off.addLoreLine("§7Click to turn all crops off");
            all_off.onClick((clickType) -> {
                crops.forEach((crop) -> {
                    String var10000 = crop.toLowerCase();
                    String path = "crop_harvesting.crops." + var10000.replaceAll(" ", "_");
                    settings.set(path, false);
                });
                this.reload();
            });
            footer.setItem(all_off, 7);
            page.setSection(content, 10);
            page.setSection(footer, 27);
        });
        return page;
    }

    public Page settings_cropharvesting_tools() {
        Page page = new Page("Crop Harvesting - Tools", 36, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            Section content = new Section(3, 1);
            DataFile settings = this.plugin.getDataFile("settings");
            boolean no_tool_enabled = settings.getBoolean("crop_harvesting.tools.no_tool", true);
            String[] no_tool_data = this.getData(no_tool_enabled);
            Item no_tool = new Item(no_tool_data[0] + "No Tool", Material.BARRIER);
            no_tool.addLoreLines(new String[]{"§7State: " + no_tool_data[0] + no_tool_data[1], "§7-----", "§7Click to " + no_tool_data[2]});
            no_tool.onClick((clickType) -> {
                settings.set("crop_harvesting.tools.no_tool", !no_tool_enabled);
                this.reload();
            });
            content.setItem(no_tool, 0);
            boolean hoe_enabled = settings.getBoolean("crop_harvesting.tools.hoe", true);
            String[] hoe_data = this.getData(hoe_enabled);
            Item hoe = new Item(hoe_data[0] + "Hoe", Material.IRON_HOE);
            hoe.addItemFlag(ItemFlag.HIDE_ATTRIBUTES);
            hoe.addLoreLines(new String[]{"§7State: " + hoe_data[0] + hoe_data[1], "§7-----", "§7Click to " + hoe_data[2]});
            hoe.onClick((clickType) -> {
                settings.set("crop_harvesting.tools.hoe", !hoe_enabled);
                this.reload();
            });
            content.setItem(hoe, 2);
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            footer.setItem(this.getNavigationItem(), 4);
            page.setSection(content, 12);
            page.setSection(footer, 27);
        });
        return page;
    }

    public Page settings_betterdrops() {
        Page page = new Page("Better Drops", 36, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            DataFile settings = this.plugin.getDataFile("settings");
            Section content = new Section(7, 1);
            Item blocks = new Item("§eBlocks", Material.GRASS);
            blocks.addLoreLines(new String[]{"§7Choose the blocks that give better drops", "§7-----"});
            blocks.addLoreLine("§7Click to view the block settings");
            blocks.onClick((clickType) -> {
                this.open(this.settings_betterdrops_blocks());
            });
            content.setItem(blocks, 0);
            Item tools = new Item("§eTools", Material.IRON_HOE);
            tools.addLoreLines(new String[]{"§7Choose the tools that can", "§7be used to get better drops", "§7-----"});
            tools.addLoreLine("§7Click to view the tools settings");
            tools.addItemFlag(ItemFlag.HIDE_ATTRIBUTES);
            tools.onClick((clickType) -> {
                this.open(this.settings_betterdrops_tools());
            });
            content.setItem(tools, 2);
            String fortune_path = "better_drops.fortune";
            boolean fortune_enabled = settings.getBoolean(fortune_path, true);
            String[] data = this.getData(fortune_enabled);
            Item fortune = new Item(data[0] + "Fortune", Material.ENCHANTED_BOOK);
            fortune.addLoreLines(new String[]{"§7State: " + data[0] + data[1], "§7-----"});
            fortune.addLoreLines(new String[]{"§7Decide whether the fortune enchantment", "§7will affect the drops or not", "§7-----"});
            fortune.addLoreLine("§7Click to " + data[2]);
            fortune.onClick((clickType) -> {
                settings.set(fortune_path, !fortune_enabled);
                this.reload();
            });
            content.setItem(fortune, 4);
            Item permisisons = new Item("§ePermissions", Material.FILLED_MAP);
            permisisons.addLoreLines(new String[]{"§7Set permissions to allow only", "§7certain players to get better drops", "§7-----"});
            permisisons.addLoreLine("§7Click to view the permissions");
            permisisons.onClick((clickType) -> {
                this.open(this.settings_permissions("better_drops"));
            });
            content.setItem(permisisons, 6);
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            footer.setItem(this.getNavigationItem(), 4);
            page.setSection(content, 10);
            page.setSection(footer, 27);
        });
        return page;
    }

    public Page settings_betterdrops_blocks() {
        Page page = new Page("Better Drops - Blocks", 45, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            DataFile settings = this.plugin.getDataFile("settings");
            List<String> blocks = List.of("Acacia Leaves", "Azalea Leaves", "Birch Leaves", "Dark Oak Leaves", "Flowering Azalea Leaves", "Jungle Leaves", "Oak Leaves", "Spruce Leaves", "Grass", "Tall Grass", "Fern", "Large Fern");
            Section content = new Section(7, 2);
            blocks.forEach((name) -> {
                Material material = Material.valueOf(name.toUpperCase().replaceAll(" ", "_"));
                boolean enabled = settings.getBoolean("better_drops.blocks." + material.name().toLowerCase(), true);
                String[] data = this.getData(enabled);
                Item item = new Item(data[0] + name, material);
                item.addLoreLines(new String[]{"§7State: " + data[0] + data[1], "§7-----"});
                item.addLoreLine("§7Click to " + data[2]);
                item.onClick((clickType) -> {
                    String var10001 = material.name().toLowerCase();
                    settings.set("better_drops.blocks." + var10001, !enabled);
                    this.reload();
                });
                content.addItem(item);
            });
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            footer.setItem(this.getNavigationItem(), 4);
            Item all_on = new Item("§aAll On", Material.LIME_DYE);
            all_on.addLoreLine("§7Click to turn all blocks on");
            all_on.onClick((clickType) -> {
                blocks.forEach((block) -> {
                    String var10000 = block.toLowerCase();
                    String path = "better_drops.blocks." + var10000.replaceAll(" ", "_");
                    settings.set(path, true);
                });
                this.reload();
            });
            footer.setItem(all_on, 6);
            Item all_off = new Item("§cAll Off", Material.RED_DYE);
            all_off.addLoreLine("§7Click to turn all blocks off");
            all_off.onClick((clickType) -> {
                blocks.forEach((block) -> {
                    String var10000 = block.toLowerCase();
                    String path = "better_drops.blocks." + var10000.replaceAll(" ", "_");
                    settings.set(path, false);
                });
                this.reload();
            });
            footer.setItem(all_off, 7);
            page.setSection(content, 10);
            page.setSection(footer, 36);
        });
        return page;
    }

    public Page settings_betterdrops_tools() {
        Page page = new Page("Better Drops - Tools", 36, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            Section content = new Section(3, 1);
            DataFile settings = this.plugin.getDataFile("settings");
            boolean no_tool_enabled = settings.getBoolean("better_drops.tools.no_tool", true);
            String[] no_tool_data = this.getData(no_tool_enabled);
            Item no_tool = new Item(no_tool_data[0] + "No Tool", Material.BARRIER);
            no_tool.addLoreLines(new String[]{"§7State: " + no_tool_data[0] + no_tool_data[1], "§7-----", "§7Click to " + no_tool_data[2]});
            no_tool.onClick((clickType) -> {
                settings.set("better_drops.tools.no_tool", !no_tool_enabled);
                this.reload();
            });
            content.setItem(no_tool, 0);
            boolean hoe_enabled = settings.getBoolean("better_drops.tools.hoe", true);
            String[] hoe_data = this.getData(hoe_enabled);
            Item hoe = new Item(hoe_data[0] + "Hoe", Material.IRON_HOE);
            hoe.addItemFlag(ItemFlag.HIDE_ATTRIBUTES);
            hoe.addLoreLines(new String[]{"§7State: " + hoe_data[0] + hoe_data[1], "§7-----", "§7Click to " + hoe_data[2]});
            hoe.onClick((clickType) -> {
                settings.set("better_drops.tools.hoe", !hoe_enabled);
                this.reload();
            });
            content.setItem(hoe, 2);
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            footer.setItem(this.getNavigationItem(), 4);
            page.setSection(content, 12);
            page.setSection(footer, 27);
        });
        return page;
    }

    public Page settings_sneaking() {
        Page page = new Page("Sneaking", 36, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            DataFile settings = this.plugin.getDataFile("settings");
            Section content = new Section(7, 1);
            Item blocks = new Item("§eBlocks", Material.WHEAT_SEEDS);
            blocks.addLoreLines(new String[]{"§7Choose the blocks that will grow", "§7faster when a player is sneaking", "§7-----"});
            blocks.addLoreLine("§7Click to view the crops settings");
            blocks.onClick((clickType) -> {
                this.open(this.settings_sneaking_blocks());
            });
            content.setItem(blocks, 0);
            int range = settings.getInt("sneaking.range");
            Item rangeItem = new Item("§eRange", Material.MAP);
            rangeItem.addLoreLines(new String[]{"§7Players sneaking in a range of §6" + range, "§7blocks will increase the growth speed", "§7-----"});
            rangeItem.addLoreLines(new String[]{"§cA too high range could cause", "§cthe server to lag or crash", "§7-----"});
            rangeItem.addLoreLine("§7Click to change");
            rangeItem.onClick((clickType) -> {
                Builder builder = new Builder();
                builder.plugin(this.plugin);
                builder.title("Enter a number");
                builder.text(range + "");
                builder.itemLeft(new ItemStack(Material.MAP));
                builder.onLeftInputClick((p2) -> {
                    history.openPage(0);
                });
                builder.onClose((p2) -> {
                    history.openPage(0);
                });
                builder.onComplete((p2, text) -> {
                    if (!StringUtils.isNumeric(text)) {
                        return Response.text("not a number");
                    } else {
                        settings.set("sneaking.range", Integer.parseInt(text));
                        return Response.close();
                    }
                });
                builder.open(p);
            });
            content.setItem(rangeItem, 2);
            int chance = settings.getInt("sneaking.chance");
            Item chanceItem = new Item("§eChance", Material.MAP);
            chanceItem.addLoreLines(new String[]{"§7The chance of a crop growing is", "§6" + chance + "% §7every 5 ticks (0.25 seconds)", "§7-----"});
            chanceItem.addLoreLine("§7Click to change");
            chanceItem.onClick((clickType) -> {
                Builder builder = new Builder();
                builder.plugin(this.plugin);
                builder.title("Enter a number");
                builder.text(chance + "");
                builder.itemLeft(new ItemStack(Material.MAP));
                builder.onLeftInputClick((p2) -> {
                    history.openPage(0);
                });
                builder.onClose((p2) -> {
                    history.openPage(0);
                });
                builder.onComplete((p2, text) -> {
                    if (!StringUtils.isNumeric(text)) {
                        return Response.text("not a number");
                    } else if (Integer.parseInt(text) > 100) {
                        return Response.text("can't be > 100");
                    } else {
                        settings.set("sneaking.chance", Integer.parseInt(text));
                        return Response.close();
                    }
                });
                builder.open(p);
            });
            content.setItem(chanceItem, 4);
            Item permissions = new Item("§ePermissions", Material.FILLED_MAP);
            permissions.addLoreLines(new String[]{"§7Set permissions to allow only certain players", "§7sneaking to increase the growth speed", "§7-----"});
            permissions.addLoreLine("§7Click to view the permissions");
            permissions.onClick((clickType) -> {
                this.open(this.settings_permissions("sneaking"));
            });
            content.setItem(permissions, 6);
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            footer.setItem(this.getNavigationItem(), 4);
            page.setSection(content, 10);
            page.setSection(footer, 27);
        });
        return page;
    }

    public Page settings_sneaking_blocks() {
        Page page = new Page("Sneaking - Blocks", 54, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            DataFile settings = this.plugin.getDataFile("settings");
            List<String> blocks = List.of("Wheat Seeds", "Beetroot Seeds", "Potato", "Carrot", "Cocoa Beans", "Pumpkin Seeds", "Melon Seeds", "Acacia Sapling", "Azalea", "Birch Sapling", "Dark Oak Sapling", "Flowering Azalea", "Jungle Sapling", "Oak Sapling", "Spruce Sapling");
            Section content = new Section(7, 3);
            blocks.forEach((name) -> {
                Material material = Material.valueOf(name.toUpperCase().replaceAll(" ", "_"));
                boolean enabled = settings.getBoolean("sneaking.blocks." + material.name().toLowerCase(), true);
                String[] data = this.getData(enabled);
                Item item = new Item(data[0] + name, material);
                item.addLoreLines(new String[]{"§7State: " + data[0] + data[1], "§7-----"});
                item.addLoreLine("§7Click to " + data[2]);
                item.onClick((clickType) -> {
                    String var10001 = material.name().toLowerCase();
                    settings.set("sneaking.blocks." + var10001, !enabled);
                    this.reload();
                });
                content.addItem(item);
            });
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            footer.setItem(this.getNavigationItem(), 4);
            Item all_on = new Item("§aAll On", Material.LIME_DYE);
            all_on.addLoreLine("§7Click to turn all blocks on");
            all_on.onClick((clickType) -> {
                blocks.forEach((block) -> {
                    String var10000 = block.toLowerCase();
                    String path = "sneaking.blocks." + var10000.replaceAll(" ", "_");
                    settings.set(path, true);
                });
                this.reload();
            });
            footer.setItem(all_on, 6);
            Item all_off = new Item("§cAll Off", Material.RED_DYE);
            all_off.addLoreLine("§7Click to turn all blocks off");
            all_off.onClick((clickType) -> {
                blocks.forEach((block) -> {
                    String var10000 = block.toLowerCase();
                    String path = "sneaking.blocks." + var10000.replaceAll(" ", "_");
                    settings.set(path, false);
                });
                this.reload();
            });
            footer.setItem(all_off, 7);
            page.setSection(content, 10);
            page.setSection(footer, 45);
        });
        return page;
    }

    public Page settings_shearing() {
        Page page = new Page("Shearing", 36, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            Section content = new Section(3, 1);
            Item blocks = new Item("§eBlocks", Material.TALL_GRASS);
            blocks.addLoreLines(new String[]{"§7Choose the blocks that can be sheared", "§7-----"});
            blocks.addLoreLine("§7Click to view the blocks settings");
            blocks.onClick((clickType) -> {
                this.open(this.settings_shearing_blocks());
            });
            content.setItem(blocks, 0);
            Item permissions = new Item("§ePermissions", Material.FILLED_MAP);
            permissions.addLoreLines(new String[]{"§7Set permissions to allow only", "§7certain players to shear blocks", "§7-----"});
            permissions.addLoreLine("§7Click to view the permissions");
            permissions.onClick((clickType) -> {
                this.open(this.settings_permissions("shearing"));
            });
            content.setItem(permissions, 2);
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            footer.setItem(this.getNavigationItem(), 4);
            page.setSection(content, 12);
            page.setSection(footer, 27);
        });
        return page;
    }

    public Page settings_shearing_blocks() {
        Page page = new Page("Shearing - Blocks", 45, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            DataFile settings = this.plugin.getDataFile("settings");
            List<String> blocks = List.of("Acacia Sapling", "Azalea", "Birch Sapling", "Dark Oak Sapling", "Flowering Azalea", "Jungle Sapling", "Oak Sapling", "Spruce Sapling", "Tall Grass", "Seagrass", "Large Fern");
            Section content = new Section(7, 3);
            blocks.forEach((name) -> {
                Material material = Material.valueOf(name.toUpperCase().replaceAll(" ", "_"));
                boolean enabled = settings.getBoolean("shearing.blocks." + material.name().toLowerCase(), true);
                String[] data = this.getData(enabled);
                if (name.equals("Seagrass")) {
                    name = "Tall Seagrass";
                }

                Item item = new Item(data[0] + name, material);
                item.addLoreLines(new String[]{"§7State: " + data[0] + data[1], "§7-----"});
                item.addLoreLine("§7Click to " + data[2]);
                item.onClick((clickType) -> {
                    String var10001 = material.name().toLowerCase();
                    settings.set("shearing.blocks." + var10001, !enabled);
                    this.reload();
                });
                content.addItem(item);
            });
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            footer.setItem(this.getNavigationItem(), 4);
            Item all_on = new Item("§aAll On", Material.LIME_DYE);
            all_on.addLoreLine("§7Click to turn all blocks on");
            all_on.onClick((clickType) -> {
                blocks.forEach((block) -> {
                    String var10000 = block.toLowerCase();
                    String path = "shearing.blocks." + var10000.replaceAll(" ", "_");
                    settings.set(path, true);
                });
                this.reload();
            });
            footer.setItem(all_on, 6);
            Item all_off = new Item("§cAll Off", Material.RED_DYE);
            all_off.addLoreLine("§7Click to turn all blocks off");
            all_off.onClick((clickType) -> {
                blocks.forEach((block) -> {
                    String var10000 = block.toLowerCase();
                    String path = "shearing.blocks." + var10000.replaceAll(" ", "_");
                    settings.set(path, false);
                });
                this.reload();
            });
            footer.setItem(all_off, 7);
            page.setSection(content, 10);
            page.setSection(footer, 36);
        });
        return page;
    }

    public Page settings_crafting() {
        Page page = new Page("Crafting", 36, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            Section content = new Section(3, 1);
            Item recipes = new Item("§eRecipes", Material.CRAFTING_TABLE);
            recipes.addLoreLines(new String[]{"§7Choose the recipes that can be used", "§7-----"});
            recipes.addLoreLine("§7Click to view the recipe settings");
            recipes.onClick((clickType) -> {
                this.open(this.settings_crafting_recipes());
            });
            content.setItem(recipes, 0);
            Item permissions = new Item("§ePermissions", Material.FILLED_MAP);
            permissions.addLoreLines(new String[]{"§7Set permissions to allow only certain", "§7players to use the crafting recipes", "§7-----"});
            permissions.addLoreLine("§7Click to view the permissions");
            permissions.onClick((clickType) -> {
                this.open(this.settings_permissions("crafting"));
            });
            content.setItem(permissions, 2);
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            footer.setItem(this.getNavigationItem(), 4);
            page.setSection(content, 12);
            page.setSection(footer, 27);
        });
        return page;
    }

    public Page settings_crafting_recipes() {
        Page page = new Page("Crafting - Recipes", 45, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            DataFile settings = this.plugin.getDataFile("settings");
            List<String> recipes = List.of("Acacia Sapling", "Azalea", "Birch Sapling", "Dark Oak Sapling", "Flowering Azalea", "Jungle Sapling", "Oak Sapling", "Spruce Sapling");
            Section content = new Section(7, 3);
            recipes.forEach((name) -> {
                Material material = Material.valueOf(name.toUpperCase().replaceAll(" ", "_"));
                boolean enabled = settings.getBoolean("crafting.recipes." + material.name().toLowerCase(), true);
                String[] data = this.getData(enabled);
                Item item = new Item(data[0] + name, material);
                item.addLoreLines(new String[]{"§7State: " + data[0] + data[1], "§7-----"});
                item.addLoreLine("§7Click to " + data[2]);
                item.onClick((clickType) -> {
                    String var10001 = material.name().toLowerCase();
                    settings.set("crafting.recipes." + var10001, !enabled);
                    this.reload();
                });
                content.addItem(item);
            });
            ItemStack potion = new ItemStack(Material.POTION);
            PotionMeta potionMeta = (PotionMeta)potion.getItemMeta();
            potionMeta.setBasePotionData(new PotionData(PotionType.WATER));
            potion.setItemMeta(potionMeta);
            boolean water_bottle_enabled = settings.getBoolean("crafting.recipes.potion", true);
            String[] water_bottle_data = this.getData(water_bottle_enabled);
            Item water_bottle = Item.fromItemStack(potion);
            water_bottle.setName(water_bottle_data[0] + "Water Bottle");
            water_bottle.addItemFlag(ItemFlag.HIDE_POTION_EFFECTS);
            water_bottle.addLoreLines(new String[]{"§7State: " + water_bottle_data[0] + water_bottle_data[1], "§7-----"});
            water_bottle.addLoreLine("§7Click to " + water_bottle_data[2]);
            water_bottle.onClick((clickType) -> {
                settings.set("crafting.recipes.potion", !water_bottle_enabled);
                this.reload();
            });
            content.addItem(water_bottle);
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            footer.setItem(this.getNavigationItem(), 4);
            Item all_on = new Item("§aAll On", Material.LIME_DYE);
            all_on.addLoreLine("§7Click to turn all recipes on");
            all_on.onClick((clickType) -> {
                recipes.forEach((recipe) -> {
                    String var10000 = recipe.toLowerCase();
                    String path = "crafting.recipes." + var10000.replaceAll(" ", "_");
                    settings.set(path, true);
                });
                settings.set("crafting.recipes.potion", true);
                this.reload();
            });
            footer.setItem(all_on, 6);
            Item all_off = new Item("§cAll Off", Material.RED_DYE);
            all_off.addLoreLine("§7Click to turn all recipes off");
            all_off.onClick((clickType) -> {
                recipes.forEach((recipe) -> {
                    String var10000 = recipe.toLowerCase();
                    String path = "crafting.recipes." + var10000.replaceAll(" ", "_");
                    settings.set(path, false);
                });
                settings.set("crafting.recipes.potion", false);
                this.reload();
            });
            footer.setItem(all_off, 7);
            page.setSection(content, 10);
            page.setSection(footer, 36);
        });
        return page;
    }

    public Page settings_bonemealing() {
        Page page = new Page("Bone Mealing", 36, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            Section content = new Section(3, 1);
            Item blocks = new Item("§eBlocks", Material.DIRT);
            blocks.addLoreLines(new String[]{"§7Choose the blocks that can be bone mealed", "§7-----"});
            blocks.addLoreLines(new String[]{"§7Click to view the blocks settings"});
            blocks.onClick((clickType) -> {
                this.open(this.settings_bonemealing_blocks());
            });
            content.setItem(blocks, 0);
            Item permissions = new Item("§ePermissions", Material.FILLED_MAP);
            permissions.addLoreLines(new String[]{"§7Set permissions to allow only certain", "§7players to use bone mealing", "§7-----"});
            permissions.addLoreLine("§7Click to view the permissions");
            permissions.onClick((clickType) -> {
                this.open(this.settings_permissions("bonemealing"));
            });
            content.setItem(permissions, 2);
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            footer.setItem(this.getNavigationItem(), 4);
            page.setSection(content, 12);
            page.setSection(footer, 27);
        });
        return page;
    }

    public Page settings_bonemealing_blocks() {
        Page page = new Page("Bone Mealing - Blocks", 54, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            DataFile settings = this.plugin.getDataFile("settings");
            List<String> blocks = List.of("Sugar Cane", "Cactus", "Vine", "Dead Bush", "Nether Wart", "Dirt", "Netherrack", "Poppy", "Dandelion", "Blue Orchid", "Allium", "Azure Bluet", "Red Tulip", "Orange Tulip", "White Tulip", "Pink Tulip", "Oxeye Daisy", "Cornflower", "Lily Of The Valley", "Nether Sprouts", "Azalea Leaves");
            Section content = new Section(7, 3);
            blocks.forEach((name) -> {
                Material material = Material.valueOf(name.toUpperCase().replaceAll(" ", "_"));
                boolean enabled = settings.getBoolean("bonemealing.blocks." + material.name().toLowerCase(), true);
                String[] data = this.getData(enabled);
                Item item = new Item(data[0] + name, material);
                item.addLoreLines(new String[]{"§7State: " + data[0] + data[1], "§7-----"});
                item.addLoreLine("§7Click to " + data[2]);
                item.onClick((clickType) -> {
                    String var10001 = material.name().toLowerCase();
                    settings.set("bonemealing.blocks." + var10001, !enabled);
                    this.reload();
                });
                content.addItem(item);
            });
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            footer.setItem(this.getNavigationItem(), 4);
            Item all_on = new Item("§aAll On", Material.LIME_DYE);
            all_on.addLoreLine("§7Click to turn all blocks on");
            all_on.onClick((clickType) -> {
                blocks.forEach((block) -> {
                    String var10000 = block.toLowerCase();
                    String path = "bonemealing.blocks." + var10000.replaceAll(" ", "_");
                    settings.set(path, true);
                });
                this.reload();
            });
            footer.setItem(all_on, 6);
            Item all_off = new Item("§cAll Off", Material.RED_DYE);
            all_off.addLoreLine("§7Click to turn all blocks off");
            all_off.onClick((clickType) -> {
                blocks.forEach((block) -> {
                    String var10000 = block.toLowerCase();
                    String path = "bonemealing.blocks." + var10000.replaceAll(" ", "_");
                    settings.set(path, false);
                });
                this.reload();
            });
            footer.setItem(all_off, 7);
            page.setSection(content, 10);
            page.setSection(footer, 45);
        });
        return page;
    }

    public Page settings_right_clicking() {
        Page page = new Page("Right Clicking", 36, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            Section content = new Section(3, 1);
            Item blocks = new Item("§eBlocks", Material.CANDLE);
            blocks.addLoreLines(new String[]{"§7Choose the blocks that can be right clicked", "§7-----"});
            blocks.addLoreLine("§7Click to view the blocks settings");
            blocks.onClick((clickType) -> {
                this.open(this.settings_right_clicking_blocks());
            });
            content.setItem(blocks, 0);
            Item permissions = new Item("§ePermissions", Material.FILLED_MAP);
            permissions.addLoreLines(new String[]{"§7Set permissions to allow only certain", "§7players to right click blocks", "§7-----"});
            permissions.addLoreLine("§7Click to view the permissions");
            permissions.onClick((clickType) -> {
                this.open(this.settings_permissions("right_clicking"));
            });
            content.setItem(permissions, 2);
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            footer.setItem(this.getNavigationItem(), 4);
            page.setSection(content, 12);
            page.setSection(footer, 27);
        });
        return page;
    }

    public Page settings_right_clicking_blocks() {
        Page page = new Page("Right Clicking - Blocks", 36, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            DataFile settings = this.plugin.getDataFile("settings");
            List<String> blocks = List.of("Candle", "Sea Pickle", "Carved Pumpkin", "Jack o Lantern");
            Section content = new Section(7, 3);
            blocks.forEach((name) -> {
                Material material = Material.valueOf(name.toUpperCase().replaceAll(" ", "_"));
                boolean enabled = settings.getBoolean("right_clicking.blocks." + material.name().toLowerCase(), true);
                String[] data = this.getData(enabled);
                Item item = new Item(data[0] + name.replace("o ", "o'"), material);
                item.addLoreLines(new String[]{"§7State: " + data[0] + data[1], "§7-----"});
                item.addLoreLine("§7Click to " + data[2]);
                item.onClick((clickType) -> {
                    String var10001 = material.name().toLowerCase();
                    settings.set("right_clicking.blocks." + var10001, !enabled);
                    this.reload();
                });
                content.addItem(item);
            });
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            footer.setItem(this.getNavigationItem(), 4);
            Item all_on = new Item("§aAll On", Material.LIME_DYE);
            all_on.addLoreLine("§7Click to turn all blocks on");
            all_on.onClick((clickType) -> {
                blocks.forEach((block) -> {
                    String var10000 = block.toLowerCase();
                    String path = "right_clicking.blocks." + var10000.replaceAll(" ", "_");
                    settings.set(path, true);
                });
                this.reload();
            });
            footer.setItem(all_on, 6);
            Item all_off = new Item("§cAll Off", Material.RED_DYE);
            all_off.addLoreLine("§7Click to turn all blocks off");
            all_off.onClick((clickType) -> {
                blocks.forEach((block) -> {
                    String var10000 = block.toLowerCase();
                    String path = "right_clicking.blocks." + var10000.replaceAll(" ", "_");
                    settings.set(path, false);
                });
                this.reload();
            });
            footer.setItem(all_off, 7);
            page.setSection(content, 10);
            page.setSection(footer, 27);
        });
        return page;
    }

    public Page settings_experimental() {
        Page page = new Page("Experimental", 36, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            Section content = new Section(3, 1);
            Item settings = new Item("§eSettings", Material.SPRUCE_SAPLING);
            settings.addLoreLines(new String[]{"§7Choose the settings that should be activated", "§7-----"});
            settings.addLoreLine("§7Click to view the settings");
            settings.onClick((clickType) -> {
                this.open(this.settings_experimental_settings());
            });
            content.setItem(settings, 0);
            Item permissions = new Item("§ePermissions", Material.FILLED_MAP);
            permissions.addLoreLines(new String[]{"§7Set permissions to allow only certain", "§7players to use experimental features", "§7-----"});
            permissions.addLoreLine("§7Click to view the permissions");
            permissions.onClick((clickType) -> {
                this.open(this.settings_permissions("experimental"));
            });
            content.setItem(permissions, 2);
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            footer.setItem(this.getNavigationItem(), 4);
            page.setSection(content, 12);
            page.setSection(footer, 27);
        });
        return page;
    }

    public Page settings_experimental_settings() {
        Page page = new Page("Experimental - Settings", 36, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            DataFile settings = this.plugin.getDataFile("settings");
            Section content = new Section(7, 3);
            boolean enabled = settings.getBoolean("experimental.settings.mega_trees", true);
            String[] data = this.getData(enabled);
            Item mega_trees = new Item(data[0] + "Mega Trees", Material.SPRUCE_SAPLING);
            mega_trees.addLoreLines(new String[]{"§7State: " + data[0] + data[1], "§7-----"});
            mega_trees.addLoreLine("§7Click to " + data[2]);
            mega_trees.onClick((clickType) -> {
                settings.set("experimental.settings.mega_trees", !enabled);
                this.reload();
            });
            content.addItem(mega_trees);
            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            footer.setItem(this.getNavigationItem(), 4);
            Item all_on = new Item("§aAll On", Material.LIME_DYE);
            all_on.addLoreLine("§7Click to turn all blocks on");
            all_on.onClick((clickType) -> {
                settings.set("experimental.settings.mega_trees", true);
                this.reload();
            });
            footer.setItem(all_on, 6);
            Item all_off = new Item("§cAll Off", Material.RED_DYE);
            all_off.addLoreLine("§7Click to turn all blocks off");
            all_off.onClick((clickType) -> {
                settings.set("experimental.settings.mega_trees", false);
                this.reload();
            });
            footer.setItem(all_off, 7);
            page.setSection(content, 10);
            page.setSection(footer, 27);
        });
        return page;
    }

    public Page settings_permissions(String setting) {
        Page page = new Page("Permissions", 54, this.plugin);
        History history = this.plugin.getHistory(this.player);
        history.addPage(page);
        page.onOpen((p) -> {
            page.clear();
            int currentPage = this.getCurrentPage(page);
            DataFile config = this.plugin.getDataFile("settings");
            List<String> permissions = new ArrayList(config.getStringList(setting + ".permissions"));
            Section content = new Section(9, 5);
            Item add;
            if (!permissions.isEmpty()) {
                for(int i = (currentPage - 1) * 45; i < currentPage * 45 && i < permissions.size(); ++i) {
                    String permission = (String)permissions.get(i);
                    add = new Item("§a" + permission, Material.FILLED_MAP);
                    add.addLoreLines(new String[]{"§7Click to edit", "§7Shift-right-click to remove"});
                    add.onClick((clickType) -> {
                        if (clickType == ClickType.SHIFT_RIGHT) {
                            this.open(this.confirmation("Do you really want to remove " + permission, (ct) -> {
                                permissions.remove(permission);
                                config.set(setting + ".permissions", permissions);
                                history.openPage(1);
                            }));
                        } else {
                            Builder builder = new Builder();
                            builder.plugin(this.plugin);
                            builder.title("Enter a permission");
                            builder.text(permission);
                            builder.itemLeft(new ItemStack(Material.FILLED_MAP));
                            builder.onLeftInputClick((p2) -> {
                                history.openPage(0);
                            });
                            builder.onClose((p2) -> {
                                history.openPage(0);
                            });
                            builder.onComplete((p2, text) -> {
                                if (permissions.contains(text)) {
                                    return Response.text("already existing");
                                } else {
                                    permissions.remove(permission);
                                    permissions.add(text);
                                    config.set(setting + ".permissions", permissions);
                                    return Response.close();
                                }
                            });
                            builder.open(this.player);
                        }

                    });
                    content.addItem(add);
                }
            } else {
                Item noPermissions = new Item("§cNo Permissions", Material.RED_STAINED_GLASS_PANE);
                noPermissions.addLoreLines(new String[]{"§7You can add permissions by", "§7clicking the \"Add\" button"});
                content.setItem(noPermissions, 22);
            }

            Section footer = new Section(9, 1);
            footer.fill(this.backgroundItem);
            if (currentPage > 1) {
                footer.setItem(this.getPreviousPageItem(permissions.size()), 3);
            }

            footer.setItem(this.getNavigationItem(), 4);
            if (currentPage * 45 < permissions.size()) {
                footer.setItem(this.getNextPageItem(permissions.size()), 5);
            }

            Item delete_all = new Item("§cDelete All", Material.RED_DYE);
            delete_all.addLoreLine("§7Click to delete all permisisons");
            delete_all.onClick((clickType) -> {
                this.open(this.confirmation("Do you really want to delete all permissions?", (ct) -> {
                    config.set(setting + ".permissions", new ArrayList());
                    history.openPage(1);
                }));
            });
            footer.setItem(delete_all, 7);
            add = new Item("§aAdd", Material.SLIME_BALL);
            add.addLoreLine("§7Click to add a new permission");
            add.onClick((clickType) -> {
                Builder builder = new Builder();
                builder.plugin(this.plugin);
                builder.title("Enter a permission");
                builder.text("permission");
                builder.itemLeft(new ItemStack(Material.FILLED_MAP));
                builder.onLeftInputClick((p2) -> {
                    history.openPage(0);
                });
                builder.onClose((p2) -> {
                    history.openPage(0);
                });
                builder.onComplete((p2, text) -> {
                    if (permissions.contains(text)) {
                        return Response.text("already existing");
                    } else {
                        permissions.add(text);
                        config.set(setting + ".permissions", permissions);
                        return Response.close();
                    }
                });
                builder.open(this.player);
            });
            footer.setItem(add, 8);
            page.setSection(content, 0);
            page.setSection(footer, 45);
        });
        return page;
    }

    private Item getNavigationItem() {
        History history = this.plugin.getHistory(this.player);
        Item navigation = new Item();
        navigation.setMaterial(Material.BARRIER);
        if (history.getPage(1) != null) {
            navigation.setName("§eNavigation");
            navigation.clearLore();
            navigation.addLoreLine("§7Click to return to the previous page");
            navigation.addLoreLine("§7Right-click view the history");
            navigation.addLoreLine("§7Middle-click to close");
            navigation.onClick((clickType) -> {
                switch(clickType) {
                    case RIGHT:
                        this.open(this.history());
                        break;
                    case MIDDLE:
                        this.player.closeInventory();
                        break;
                    default:
                        history.openPage(1);
                }

            });
        } else {
            navigation.setName("§eClose");
            navigation.clearLore();
            navigation.addLoreLine("§7Click to close");
            navigation.onClick((clickType) -> {
                this.player.closeInventory();
            });
        }

        return navigation;
    }

    private Item getPreviousPageItem(int amount) {
        Page page = this.plugin.getHistory(this.player).getPage(0);
        Item previousPage = new Item();
        previousPage.addLoreLine("§7Click to go to the previous page");
        previousPage.addLoreLine("§7Right-click to open the page list");
        previousPage.addLoreLine("§7Middle-click to jump to the beginning");
        previousPage.setName("§ePrevious page");
        previousPage.setMaterial(Material.ARROW);
        previousPage.onClick((clickType) -> {
            switch(clickType) {
                case RIGHT:
                    this.open(this.pages((int)Precision.round((float)amount / 45.0F, 0, 0)));
                    break;
                case MIDDLE:
                    this.setCurrentPage(page, 1);
                    this.reload();
                    break;
                default:
                    this.setCurrentPage(page, this.getCurrentPage(page) - 1);
                    this.reload();
            }

        });
        return previousPage;
    }

    private Item getNextPageItem(int amount) {
        Page page = this.plugin.getHistory(this.player).getPage(0);
        Item nextPage = new Item();
        nextPage.addLoreLine("§7Click to go to the next page");
        nextPage.addLoreLine("§7Right-click to open the page list");
        nextPage.addLoreLine("§7Middle-click to jump to the end");
        nextPage.setName("§eNext page");
        nextPage.setMaterial(Material.ARROW);
        nextPage.onClick((clickType) -> {
            switch(clickType) {
                case RIGHT:
                    this.open(this.pages((int)Precision.round((float)amount / 45.0F, 0, 0)));
                    break;
                case MIDDLE:
                    this.setCurrentPage(page, (int)Precision.round((float)amount / 45.0F, 0, 0));
                    this.reload();
                    break;
                default:
                    this.setCurrentPage(page, this.getCurrentPage(page) + 1);
                    this.reload();
            }

        });
        return nextPage;
    }

    public Integer getCurrentPage(Page page) {
        int currentPage = 1;
        if (this.plugin.getPage(this.player).containsKey(page.getTitle())) {
            currentPage = (Integer)this.plugin.getPage(this.player).get(page.getTitle());
        }

        return currentPage;
    }

    public void setCurrentPage(Page page, Integer index) {
        this.plugin.getPage(this.player).put(page.getTitle(), index);
    }

    public String[] getData(boolean enabled) {
        String[] data = new String[3];
        if (enabled) {
            data[0] = "§a";
            data[1] = "On";
            data[2] = "disable";
        } else {
            data[0] = "§c";
            data[1] = "Off";
            data[2] = "enable";
        }

        return data;
    }
}
