//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.marlongrazek.betterharvesting;

import java.util.ArrayList;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

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
        return this.hitBlock;
    }

    public boolean isPoisonous() {
        return !this.potion.getEffects().isEmpty();
    }

    public ArrayList<Block> getAffectedBlocks() {
        ArrayList<Block> affectedBlocks = new ArrayList();

        for(double x = this.hitBlock.getLocation().getX() - 1.0D; x < this.hitBlock.getLocation().getX() + 2.0D; ++x) {
            for(double z = this.hitBlock.getLocation().getZ() - 1.0D; z < this.hitBlock.getLocation().getZ() + 2.0D; ++z) {
                Location location = new Location(this.hitBlock.getLocation().getWorld(), x, this.hitBlock.getLocation().getY(), z);
                Random random = new Random();
                int randomInt = random.nextInt(100);
                if (randomInt < 30) {
                    affectedBlocks.add(location.getBlock());
                }
            }
        }

        return affectedBlocks;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
