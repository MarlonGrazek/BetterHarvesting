package com.marlongrazek.betterharvesting.utils;

import com.marlongrazek.betterharvesting.main.Main;
import com.marlongrazek.ui.History;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import com.marlongrazek.ui.UI;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Arrays;

public class GUI {

    private final Player player;

    private final UI.Item backgroundItem = new UI.Item(" ", Material.GRAY_STAINED_GLASS_PANE);

    public GUI(Player player) {
        this.player = player;
    }

    public void open(UI.Page page) {
        page.open(player);
    }

    public UI.Page menu() {

        UI.Page page = new UI.Page("Settings", 36, Main.getPlugin());

        History history = Main.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            // content
            UI.Section content = new UI.Section(7 ,1);

            // crafting recipes
            UI.Item recipes = new UI.Item("§eCrafting Recipes", Material.CRAFTING_TABLE);
            recipes.addLoreLine("§7Click to view the crafting recipes");
            content.addItem(recipes);

            // bone meal
            UI.Item bonemeal = new UI.Item("§eBonemeal", Material.BONE_MEAL);
            bonemeal.addLoreLine("§7Click to view the bonemeal settings");
            content.addItem(bonemeal);

            // dispenser
            UI.Item dispenser = new UI.Item("§eDispenser", Material.DISPENSER);
            dispenser.addLoreLine("§7Click to view the dispenser settings");
            content.addItem(dispenser);

            // hoe
            UI.Item hoe = new UI.Item("§eHoe", Material.IRON_HOE);
            hoe.addLoreLine("§7Click to view the hoe settings");
            content.addItem(hoe);

            // shears
            UI.Item shears = new UI.Item("§eShears", Material.SHEARS);
            shears.addLoreLine("§7Click to view the shears settings");
            content.addItem(shears);

            // watering
            UI.Item watering = new UI.Item("§eWatering", Material.POTION);
            PotionMeta meta = (PotionMeta) watering.toItemStack().getItemMeta();
            meta.setBasePotionData(new PotionData(PotionType.WATER));
            watering.setItemMeta(meta);
            watering.addLoreLine("§7Click to view the watering settings");
            watering.setItemFlags(new ArrayList<>(Arrays.asList(ItemFlag.values())));
            content.addItem(watering);

            // sneaking
            UI.Item sneaking = new UI.Item("§eSneaking", Material.PURPLE_CONCRETE);
            sneaking.addLoreLine("§7Click to view the sneaking settings");
            content.addItem(sneaking);

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);

            footer.setItem(getNavigationItem(), 4);

            page.setSection(content, 10);
            page.setSection(footer, 27);
        });

        return page;
    }

    public UI.Page history() {

        UI.Page page = new UI.Page("History", 54, Main.getPlugin());

        History history = Main.getHistory(player);
        history.addPage(page);

        page.onOpen(p -> {

            page.clear();

            // footer
            UI.Section footer = new UI.Section(9, 1);
            footer.fill(backgroundItem);

            footer.setItem(getNavigationItem(), 4);

            page.setSection(footer, 45);
        });

        return page;
    }

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
}
