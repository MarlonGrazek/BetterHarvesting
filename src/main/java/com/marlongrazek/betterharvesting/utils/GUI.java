package com.marlongrazek.betterharvesting.utils;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.builder.StringBuilder;
import com.marlongrazek.datafile.DataFile;
import com.marlongrazek.ui.History;
import org.apache.commons.math3.util.Precision;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import com.marlongrazek.ui.UI;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.function.Consumer;

public class GUI {

    private final Player player;

    private final UI.Item backgroundItem = new UI.Item(" ", Material.GRAY_STAINED_GLASS_PANE);
    private final String line = "§7-----";

    public GUI(Player player) {
        this.player = player;
    }

    public void open(UI.Page page) {
        page.open(player);
    }

    public void reload() {
        Main.getHistory(player).openPage(0);
    }

    // PAGES
    public UI.Page confirmation(String reason, Consumer<ClickType> yesAction) {

        UI.Page page = new UI.Page("Confirmation", 36, Main.getPlugin());

        History history = Main.getHistory(player);
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

        UI.Page page = new UI.Page("Page History", 54, Main.getPlugin());

        History history = Main.getHistory(player);
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

        UI.Page page = new UI.Page("Pages", 54, Main.getPlugin());

        History history = Main.getHistory(player);
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

    public UI.Page settings(String path) {

        int size = 36;

        List<String> pathsAmount = new ArrayList<>(Main.getDataFile("settings").getConfigurationSection(path, false));
        pathsAmount.remove("enabled");
        pathsAmount.remove("permissions");

        if(pathsAmount.size() > 7) size = 45;

        String settingName = (String) getData(path)[0];
        if(settingName.isEmpty()) settingName = "Settings";

        UI.Page page = new UI.Page(settingName, size, Main.getPlugin());

        History history = Main.getHistory(player);
        history.addPage(page);

        int finalSize = size;
        page.onOpen(p -> {

            page.clear();
            DataFile config = Main.getDataFile("settings");

            List<String> pathSettings = new ArrayList<>(config.getConfigurationSection(path, false));
            pathSettings.remove("enabled");
            pathSettings.remove("permissions");

            int height = 1;
            if(finalSize == 45) height = 2;
            UI.Section content = new UI.Section(7, height);

            if (!pathSettings.isEmpty()) {
                for (int i = 0; i < pathSettings.size() && i < content.getWidth() * content.getHeight(); i++) {

                    String setting = pathSettings.get(i);
                    String pathDot = path + ".";
                    if (path.isEmpty()) pathDot = "";
                    String settingPath = pathDot + setting;

                    List<String> clickedPathSettings = new ArrayList<>(config.getConfigurationSection(settingPath, false));
                    clickedPathSettings.remove("enabled");
                    clickedPathSettings.remove("permissions");

                    boolean enabled = config.getBoolean(settingPath + ".enabled", true);
                    boolean hasSubSettings = !clickedPathSettings.isEmpty();

                    Object[] data = getData(settingPath);

                    UI.Item item = UI.Item.fromItemStack((ItemStack) data[1]);
                    item.setName((String) data[3] + data[0]);
                    item.addLoreLine("§7State: " + getData(enabled)[0] + getData(enabled)[1]);
                    if (!((List<String>) data[2]).isEmpty()) item.addLoreLine(line);
                    ((List<String>) data[2]).forEach(item::addLoreLine);
                    item.addItemFlags(Arrays.asList(ItemFlag.values()));

                    if (!settingPath.equals("watering")) {
                        item.addLoreLines(line, "§7Click to " + getData(enabled)[2], "§7Right-click to view the permissions");
                        if (hasSubSettings && enabled) item.addLoreLine("§7Middle-click to customize");
                    } else {
                        item.addLoreLines(line, "§7Click to " + getData(enabled)[2], "§7Right-click to view the permissions (Coming Soon)");
                        if (enabled) item.addLoreLine("§7Middle-click to customize (Coming Soon)");
                    }

                    item.onClick(clickType -> {

                        // right
                        if (clickType == ClickType.RIGHT) {
                            if (!settingPath.equals("watering")) open(settings_permissions(settingPath));
                        }

                        // middle
                        else if (clickType == ClickType.MIDDLE && hasSubSettings && enabled) {
                            if (!settingPath.equals("watering")) open(settings(settingPath));
                        }

                        // others
                        else {
                            config.set(settingPath + ".enabled", !enabled);
                            reload();
                        }
                    });

                    content.addItem(item);
                }
            } else {
                UI.Item error = new UI.Item("§c404 - Page not found", Material.RED_STAINED_GLASS_PANE);
                error.addLoreLine("§7The page you are looking for could not be found");
                page.setItem(error, 13);
            }

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);
            footer.setItem(getNavigationItem(), 4);

            page.setSection(content, 10);
            page.setSection(footer, finalSize - 9);
        });

        return page;
    }

    public UI.Page settings_permissions(String setting) {

        UI.Page page = new UI.Page("Permissions", 54, Main.getPlugin());

        History history = Main.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            int currentPage = getCurrentPage(page);
            DataFile config = Main.getDataFile("settings");
            List<String> permissions = new ArrayList<>(config.getStringList(setting + ".permissions"));

            // content
            UI.Section content = new UI.Section(9, 5);

            if (!permissions.isEmpty()) {
                for (int i = (currentPage - 1) * 45; i < currentPage * 45 && i < permissions.size(); i++) {

                    String permission = permissions.get(i);
                    UI.Item permissionItem = new UI.Item("§a" + permission, Material.FILLED_MAP);
                    permissionItem.addLoreLines("§7Click to edit (not supported yet)", "§7Shift-right-click to remove");
                    permissionItem.onClick(clickType -> {
                        if (clickType == ClickType.SHIFT_RIGHT)
                            open(confirmation("Do you really want to remove " + permission, ct -> {
                                permissions.remove(permission);
                                config.set(setting + ".permissions", permissions);
                                history.openPage(1);
                            }));
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

            // add
            UI.Item add = new UI.Item("§aAdd", Material.SLIME_BALL);
            add.addLoreLine("§7AnvilGUI is not supporting 1.18.1 yet");
            add.addLoreLines("§7Therefore you need to set the", "§7permissions in the settings file");
            /*
            add.addLoreLine("§7Click to add a new permission");
            add.onClick(clickType -> {
                AnvilGUI.Builder builder = new AnvilGUI.Builder();
                builder.plugin(Main.getPlugin());
                builder.title("Enter a permission");
                builder.text("permission");
                builder.itemLeft(new ItemStack(Material.FILLED_MAP));
                builder.onLeftInputClick(p2 -> history.openPage(0));
                builder.onClose(p2 -> history.openPage(0));
                builder.onComplete((p2, text) -> {
                    if(!permissions.contains(text)) permissions.add(text);
                    config.set(setting + ".permissions", permissions);
                    return AnvilGUI.Response.close();
                });
                builder.open(player);
            });*/
            footer.setItem(add, 8);

            page.setSection(content, 0);
            page.setSection(footer, 45);
        });

        return page;
    }

    // ITEMS
    private UI.Item getNavigationItem() {
        History history = Main.getHistory(player);

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
                    case RIGHT -> open(history());
                    case MIDDLE -> player.closeInventory();
                    default -> history.openPage(1);
                }
            });
        } else {
            navigation.setName("§eClose");
            navigation.clearLore();
            navigation.addLoreLine("§7Click to close");
            navigation.onClick(clickType -> player.closeInventory());
        }
        return navigation;
    }

    private UI.Item getPreviousPageItem(int amount) {
        UI.Page page = Main.getHistory(player).getPage(0);

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
        UI.Page page = Main.getHistory(player).getPage(0);

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
        if (Main.getPage(player).containsKey(page.getTitle())) currentPage = Main.getPage(player).get(page.getTitle());
        return currentPage;
    }

    public void setCurrentPage(UI.Page page, Integer index) {
        Main.getPage(player).put(page.getTitle(), index);
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

    public Object[] getData(String path) {

        Object[] data = new Object[4];

        if(path.isEmpty()) {
            data[0] = "Settings";
            return data;
        }

        DataFile dataFile = Main.getDataFile("settings");
        boolean enabled = dataFile.getBoolean(path + ".enabled", true);

        String setting = path.split("\\.")[path.split("\\.").length - 1];

        ChatColor color = ChatColor.GREEN;
        if (!enabled) color = ChatColor.RED;

        StringBuilder name = new StringBuilder();
        for (String s : setting.split("_")) name.add(" " + new StringBuilder(s).toUpperCase(0, 1));
        name.replaceFirst(" ", "");

        data[0] = name.toString();
        data[3] = color.toString();

        try {
            data[1] = new ItemStack(Material.valueOf(setting.toUpperCase()));
        } catch (Exception ignored) {
            data[1] = new ItemStack(Material.FILLED_MAP);
        }

        data[2] = new ArrayList<>();

        switch (setting) {
            case "crafting" -> {
                data[0] = "Crafting Recipes";
                data[1] = new ItemStack(Material.CRAFTING_TABLE);
            }
            case "bonemeal" -> {
                data[0] = "Bone Mealing";
                data[1] = new ItemStack(Material.BONE_MEAL);
            }
            case "hoe" -> {
                data[0] = "Hoe Harvesting";
                data[1] = new ItemStack(Material.IRON_HOE);
            }
            case "shearing" -> data[1] = new ItemStack(Material.SHEARS);
            case "watering" -> {
                ItemStack potion = new ItemStack(Material.SPLASH_POTION);
                PotionMeta meta = (PotionMeta) potion.getItemMeta();
                meta.setBasePotionData(new PotionData(PotionType.WATER));
                potion.setItemMeta(meta);
                data[1] = potion;
                data[2] = List.of("§7Throw water bottles or potions", "§7on crops to water or poison them");
            }
            case "modify" -> data[1] = new ItemStack(Material.WRITABLE_BOOK);
            case "sneaking" -> data[2] = List.of("§7Sneak around crops and saplings", "§7to make them grow faster");
            case "experimental" -> data[1] = new ItemStack(Material.NETHER_STAR);
            case "saplings" -> {
                data[1] = new ItemStack(Material.OAK_SAPLING);

                if (path.equals("shearing.saplings"))
                    data[2] = List.of("§7Use shears on any sapling to turn it", "§7into a dead bush and drop the leaves");
                else if (path.equals("sneaking.saplings"))
                    data[2] = List.of("§7Sneak around any sapling", "§7to make it grow faster");
            }
            case "crops" -> {
                data[1] = new ItemStack(Material.WHEAT_SEEDS);

                if (path.equals("modify.crops"))
                    data[2] = List.of("§7Right click fully grown crops to", "§7harvest them and replant the seed");
                else if (path.equals("sneaking.crops"))
                    data[2] = List.of("§7Sneak around crops to", "§7make them grow faster");
            }
            case "carved_pumpkin" -> data[2] = List.of("§7- Right click a carved pumpkin with a", "§7   torch to turn it into a jack o lantern",
                    "§7- Right click a jack o lantern with empty", "§7   hands to turn it into a carved pumpkin");
            case "candle" -> data[2] = List.of("§7Right click candles to reduce", "§7their amount by 1");
            case "sea_pickle" -> data[2] = List.of("§7Right click sea pickles to", "§7reduce their amount by 1");
            case "tall_blocks" -> {
                data[1] = new ItemStack(Material.TALL_GRASS);
                data[2] = List.of("§7Use shears on two block high plants", "§7to cut them into one block high plants");
            }
            case "pumpkin" -> data[2] = List.of("§7Shear a pumpkin to turn it into a", "§7Carved pumpkin and get pumpkin seeds");
            case "megatrees" -> {
                data[1] = new ItemStack(Material.SPRUCE_SAPLING);
                data[2] = List.of("§7Sneak around 4 spruce or jungle", "§7saplings to grow a mega tree faster", line,
                        "§7Problems:", "§7- sometimes saplings disappear", "§7- sometimes regular trees grow");
            }
            case "flowers" -> {
                data[1] = new ItemStack(Material.POPPY);
                data[2] = List.of("§7Bone meal any small flower to duplicate", "§7the flower (such as tall flowers in vanilla)");
            }
            case "leaves" -> {
                data[1] = new ItemStack(Material.OAK_LEAVES);
                data[2] = List.of("§7Use a hoe on any leaves to", "§7get better drops from it");
            }
            case "grasses" -> {
                data[1] = new ItemStack(Material.GRASS);
                data[2] = List.of("§7Use a hoe on grasses to", "§7get better drops from it");
            }
            case "tall_seagrass" -> data[1] = new ItemStack(Material.SEAGRASS);
            case "nether_wart" -> data[2] = List.of("§7Bone meal nether warts to make", "§7them grow (like crops in vanilla)");
            case "vine" -> data[2] = List.of("§7Bone meal a vine to make it grow", "§7(like weeping vines in vanilla)");
            case "cactus" -> data[2] = List.of("§7Bone meal a cactus to make", "§7it grow (like bamboo in vanilla)");
            case "sugar_cane" -> data[2] = List.of("§7Bone meal sugar cane to make", "§7it grow (like bamboo in vanilla)");
            case "dirt" -> data[2] = List.of("§7Bone meal dirt to turn it and", "§7surrounding dirt into grass blocks");
            case "netherrack" -> data[2] = List.of("§7Bone meal netherrack to turn it and", "§7surrounding netherrack into nylium");
            case "nether_sprouts" -> data[2] = List.of("§7Bone meal nether sprouts to turn", "§7them into warped roots");
            case "azalea_leaves" -> data[2] = List.of("§7Bone meal azalea leaves to turn", "§7them into flowering azalea leaves");
            case "dead_bush" -> data[2] = List.of("§7Bone meal a dead bush to turn", "§7it into a random sapling");
            case "potion" -> {
                data[0] = "Water Bottle";
                ItemStack waterBottle = new ItemStack(Material.POTION);
                PotionMeta meta = (PotionMeta) waterBottle.getItemMeta();
                meta.setBasePotionData(new PotionData(PotionType.WATER));
                waterBottle.setItemMeta(meta);
                data[1] = waterBottle;
            }
        }

        return data;
    }
}
