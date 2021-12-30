package com.marlongrazek.betterharvesting;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.Random;

public class WateringEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Block hitBlock;
    private final ThrownPotion potion;
    private boolean isCancelled;

    public WateringEvent(ThrownPotion potion, Block hitBlock) {
        this.hitBlock = hitBlock;
        this.potion = potion;
        this.isCancelled = false;
    }

    public Block getHitBlock() {
        return hitBlock;
    }

    public boolean isPoisonous() {
        return !potion.getEffects().isEmpty();
    }

    public ArrayList<Block> getAffectedBlocks() {

        ArrayList<Block> affectedBlocks = new ArrayList<>();

        for(double x = hitBlock.getLocation().getX() - 1; x < hitBlock.getLocation().getX() + 2; x++) {
            for(double z = hitBlock.getLocation().getZ() - 1; z < hitBlock.getLocation().getZ() + 2; z++) {

                Location location = new Location(hitBlock.getLocation().getWorld(), x, hitBlock.getLocation().getY(), z);

                Random random = new Random();
                int randomInt = random.nextInt(100);

                if(randomInt < 30) affectedBlocks.add(location.getBlock());
            }
        }

        return affectedBlocks;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
